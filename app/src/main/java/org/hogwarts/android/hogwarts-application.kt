package org.hogwarts.android

import android.app.Application
import android.app.NotificationChannel
import android.app.LocaleManager
import android.app.NotificationManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hogwarts.android.core.data.preferences.AppPreferences
import org.hogwarts.android.core.data.preferences.DefaultAppLocale
import org.hogwarts.android.startup.AppStartupInitializer
import javax.inject.Inject

/**
 * Main Application class for Hogwarts Android.
 *
 * Initializes Hilt dependency injection and WorkManager.
 */
@HiltAndroidApp
class HogwartsApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var appStartupInitializer: AppStartupInitializer

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // Register SVG decoder so Coil can render Contentful/Webflow SVG assets
    // used by the stream home (curriculum icons, teaching hero illustration).
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()

    override fun onCreate() {
        super.onCreate()

        // All startup hooks (Timber, StrictMode, WorkManager + Firebase
        // init confirmation) funnel through AppStartupInitializer so the
        // boot sequence stays in one place.
        appStartupInitializer.initialize()

        applyDefaultLocale()

        // AppLocalesMetadataHolderService (manifest, autoStoreLocales=true) handles
        // locale persistence on Android <13; the platform LocaleManager handles 13+.
        // We just warm the DataStore cache off-main so settings reads stay snappy.
        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.IO) {
            appPreferences.language.first()
            appPreferences.themeMode.first()
        }

        createNotificationChannels()
    }

    /**
     * Arabic by default, once, before the first Activity inflates — so the first
     * frame is already RTL. The DataStore read only blocks while no per-app
     * language is set, which after the first launch is rare.
     *
     * On API 33+ this talks to the platform LocaleManager directly: AppCompat
     * only reaches it through an Activity delegate, and none exists yet.
     * Below 33 AppCompat holds the request and persists it (autoStoreLocales)
     * when MainActivity attaches.
     */
    private fun applyDefaultLocale() {
        if (!appLocalesEmpty()) return
        // runBlocking on the main thread: DataStore does its IO on its own dispatcher.
        runBlocking {
            DefaultAppLocale(
                appLocalesEmpty = ::appLocalesEmpty,
                setAppLocale = { tag ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(tag)
                    } else {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                    }
                },
                alreadyDefaulted = { appPreferences.localeDefaulted.first() },
                markDefaulted = { appPreferences.markLocaleDefaulted(it) },
            ).apply()
        }
    }

    private fun appLocalesEmpty(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager::class.java).applicationLocales.isEmpty
        } else {
            AppCompatDelegate.getApplicationLocales().isEmpty
        }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Attendance notifications channel
            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE,
                getString(R.string.notification_channel_attendance),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_attendance_desc)
            }

            // Grades notifications channel
            val gradesChannel = NotificationChannel(
                CHANNEL_GRADES,
                getString(R.string.notification_channel_grades),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_grades_desc)
            }

            // Fees notifications channel
            val feesChannel = NotificationChannel(
                CHANNEL_FEES,
                getString(R.string.notification_channel_fees),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_fees_desc)
            }

            // Messages notifications channel
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                getString(R.string.notification_channel_messages),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_messages_desc)
            }

            // Announcements notifications channel
            val announcementsChannel = NotificationChannel(
                CHANNEL_ANNOUNCEMENTS,
                getString(R.string.notification_channel_announcements),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_announcements_desc)
            }

            notificationManager.createNotificationChannels(
                listOf(
                    attendanceChannel,
                    gradesChannel,
                    feesChannel,
                    messagesChannel,
                    announcementsChannel
                )
            )
        }
    }

    companion object {
        const val CHANNEL_ATTENDANCE = "attendance"
        const val CHANNEL_GRADES = "grades"
        const val CHANNEL_FEES = "fees"
        const val CHANNEL_MESSAGES = "messages"
        const val CHANNEL_ANNOUNCEMENTS = "announcements"
    }
}
