package org.hogwarts.android.core.push

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.hogwarts.android.core.common.api.TokenProvider
import timber.log.Timber
import java.io.IOException

/** Sends the FCM token to the server, retrying with backoff until it lands. */
@HiltWorker
class RegisterDeviceTokenWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val api: DeviceTokenApi,
    private val tokenProvider: TokenProvider
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val deviceToken = inputData.getString(KEY_DEVICE_TOKEN) ?: return Result.failure()
        // Signed out since this was queued; the next login registers again.
        if (!tokenProvider.hasTokens) return Result.success()

        return try {
            val response = api.register(RegisterDeviceTokenRequest(deviceToken))
            when {
                response.isSuccessful -> Result.success()
                response.code() in 400..499 -> {
                    Timber.w("Device token rejected: ${response.code()}")
                    Result.failure()
                }
                else -> Result.retry()
            }
        } catch (e: IOException) {
            Timber.w(e, "Device token registration deferred")
            Result.retry()
        }
    }

    companion object {
        const val KEY_DEVICE_TOKEN = "device_token"
        const val WORK_NAME = "register_device_token"
    }
}
