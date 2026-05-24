package org.hogwarts.android.startup

import android.content.Context
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStartupInitializer @Inject constructor(
    private val context: Context
) {
    fun initialize() {
        Timber.d("AppStartupInitializer: Beginning app initialization")
        initializeTimber()
        initializeCoil()
        Timber.d("AppStartupInitializer: App initialization complete")
    }

    private fun initializeTimber() {
        // Timber is already initialized in HogwartsApp, this is for future enhancements
        Timber.d("Timber initialized")
    }

    private fun initializeCoil() {
        // Coil uses default ImageLoader, customize for disk cache size if needed
        Timber.d("Coil image loader ready")
    }
}
