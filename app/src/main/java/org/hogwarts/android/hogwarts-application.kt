package org.hogwarts.android

import org.hogwarts.android.core.push.NotificationChannels
import android.app.Application
import android.app.LocaleManager
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

        // The push channels (incl. the high-importance live-class one) exist before the first push, so
        // the system settings page and the dashboard's "open channel settings" can always find them.
        NotificationChannels.ensure(this)
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

    companion object {
    }
}
