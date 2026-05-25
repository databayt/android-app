package org.hogwarts.android.startup

import android.content.Context
import android.os.StrictMode
import android.util.Log
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import org.hogwarts.android.BuildConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for all startup hooks invoked from
 * `HogwartsApplication.onCreate()`. Adding a new hook here keeps `onCreate`
 * legible and makes the boot sequence testable.
 */
@Singleton
class AppStartupInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun initialize() {
        // Why: Timber must plant before any other startup log statements so
        // both debug console output and release Crashlytics breadcrumbs catch
        // the boot sequence.
        plantTimber()

        if (BuildConfig.DEBUG) {
            // Why: StrictMode surfaces accidental main-thread disk/network IO
            // and leaked Closeables during development. Release payloads never
            // include the policy so users don't pay the perf hit.
            enableStrictMode()
        }

        // Why: WorkManager is created lazily on the first getInstance() call.
        // Touching it at boot confirms HiltWorkerFactory wiring and surfaces
        // any misconfiguration as a startup crash instead of a silent runtime
        // failure when the first periodic sync would have fired.
        confirmWorkManager()

        // Why: Firebase auto-inits via FirebaseInitProvider in the manifest.
        // Verifying the instance exists at boot means a missing
        // google-services.json (E01.S01) shows up as one loud log line on
        // every launch instead of as a confusing failure deep inside the FCM
        // token upload path.
        confirmFirebase()

        Timber.i("AppStartupInitializer complete")
    }

    private fun plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
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

    private fun confirmWorkManager() {
        val wm = WorkManager.getInstance(context)
        Timber.i("WorkManager initialized: ${wm.javaClass.simpleName}")
    }

    private fun confirmFirebase() {
        try {
            val app = FirebaseApp.getInstance()
            Timber.i("Firebase initialized: project=${app.options.projectId}")
        } catch (e: IllegalStateException) {
            // E01.S01 (commit google-services.json) hasn't landed yet — log
            // loud so the gap is obvious, but don't crash the app.
            Timber.w("Firebase not initialized: ${e.message}")
        }
    }
}

/**
 * Release-build Timber tree that funnels warnings + errors to Crashlytics.
 * Below WARN is dropped to keep the breadcrumb retention focused on signals
 * a release user would actually want to debug.
 */
private class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(if (tag != null) "$tag: $message" else message)
        if (t != null && priority >= Log.ERROR) {
            crashlytics.recordException(t)
        }
    }
}
