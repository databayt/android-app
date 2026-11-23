package org.hogwarts.android.core.push

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the server's copy of this device's FCM token current.
 *
 * Called after sign-in, whenever FCM rotates the token, and on sign-out (which
 * deletes the token so the server's copy stops receiving pushes).
 */
@Singleton
class DeviceTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: PushStore,
) {

    /** The installed app's versionCode, the key for "register again after an update". */
    fun currentVersionCode(): Long = installedVersionCode(context)

    /**
     * App start: send the token only if this app version has not registered it yet. Failures leave
     * the version unrecorded, so the next start tries again. Signed-out callers never reach the server
     * (the worker also refuses without tokens).
     */
    fun registerIfVersionChanged(signedIn: Boolean) {
        if (NotificationPolicy.shouldRegisterOnStart(signedIn, store.registeredVersionCode, currentVersionCode())) {
            registerCurrentToken()
        }
    }

    /** Sign-in, a rotated token and a fresh permission grant: always send. */
    fun registerCurrentToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener(::enqueue)
            .addOnFailureListener { Timber.w(it, "FCM token unavailable") }
    }

    fun enqueue(deviceToken: String) {
        val request = OneTimeWorkRequestBuilder<RegisterDeviceTokenWorker>()
            .setInputData(workDataOf(RegisterDeviceTokenWorker.KEY_DEVICE_TOKEN to deviceToken))
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            RegisterDeviceTokenWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun unregister() {
        store.registeredVersionCode = null
        WorkManager.getInstance(context).cancelUniqueWork(RegisterDeviceTokenWorker.WORK_NAME)
        FirebaseMessaging.getInstance().deleteToken()
            .addOnFailureListener { Timber.w(it, "FCM token delete failed") }
    }
}
