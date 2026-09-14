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
    @ApplicationContext private val context: Context
) {

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
        WorkManager.getInstance(context).cancelUniqueWork(RegisterDeviceTokenWorker.WORK_NAME)
        FirebaseMessaging.getInstance().deleteToken()
            .addOnFailureListener { Timber.w(it, "FCM token delete failed") }
    }
}
