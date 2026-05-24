package org.hogwarts.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
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
import org.hogwarts.android.core.data.preferences.AppPreferences
import timber.log.Timber
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

        // AppLocalesMetadataHolderService (manifest, autoStoreLocales=true) handles
        // locale persistence on Android <13; the platform LocaleManager handles 13+.
        // We just warm the DataStore cache off-main so settings reads stay snappy.
        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.IO) {
            appPreferences.language.first()
            appPreferences.themeMode.first()
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            enableStrictMode()
        }

        createNotificationChannels()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build()
        )
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
