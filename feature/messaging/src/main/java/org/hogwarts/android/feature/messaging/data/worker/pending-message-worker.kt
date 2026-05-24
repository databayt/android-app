package org.hogwarts.android.feature.messaging.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import org.hogwarts.android.core.database.dao.PendingMessageDao
import org.hogwarts.android.feature.messaging.data.remote.MessagingApi
import org.hogwarts.android.feature.messaging.data.remote.dto.SendMessageDto
import timber.log.Timber
import java.time.Instant

@HiltWorker
class PendingMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val pendingMessageDao: PendingMessageDao,
    private val api: MessagingApi,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Timber.d("PendingMessageWorker started")

        val pending = pendingMessageDao.getAllPending()
        if (pending.isEmpty()) {
            Timber.d("No pending messages to retry")
            return Result.success()
        }

        Timber.d("Retrying ${pending.size} pending messages")
        var hasFailures = false

        for (message in pending) {
            if (message.retryCount >= MAX_RETRIES) {
                Timber.w("Message ${message.id} exceeded max retries, marking failed")
                pendingMessageDao.updateStatus(message.id, "FAILED", Instant.now().toEpochMilli())
                continue
            }

            try {
                val dto = SendMessageDto(
                    content = message.content,
                    contentType = message.contentType,
                    replyToId = message.replyToId,
                    nonce = message.id,
                )
                api.sendMessage(message.conversationId, dto)
                pendingMessageDao.delete(message.id)
                Timber.d("Successfully sent pending message ${message.id}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to send pending message ${message.id}")
                pendingMessageDao.incrementRetry(message.id, "FAILED", Instant.now().toEpochMilli())
                hasFailures = true
                // Backoff between retries within same run
                delay(1000L * (message.retryCount + 1))
            }
        }

        return if (hasFailures && runAttemptCount < 3) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "pending_message_sync"
        const val MAX_RETRIES = 5
    }
}
