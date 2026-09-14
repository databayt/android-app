package org.hogwarts.android.feature.messaging.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import org.hogwarts.android.core.database.dao.PendingMessageDao
import org.hogwarts.android.feature.messaging.data.repository.MessageSender
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sends what the thread could not: messages written offline or refused by a
 * failing server. Runs only with a network, backs off exponentially, and asks
 * to run again while anything is still queued. Rows that ran out of attempts
 * stay `failed` on the bubble, where a tap re-sends them.
 */
@HiltWorker
class PendingMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val pendingMessageDao: PendingMessageDao,
    private val sender: MessageSender,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val pending = pendingMessageDao.getAllPending()
        var stillQueued = false
        for (message in pending) {
            if (!sender.deliver(message)) stillQueued = true
        }
        return if (stillQueued) Result.retry() else Result.success()
    }

    companion object {
        const val WORK_NAME = "pending_message_sync"
    }
}

/** Seam over WorkManager so the repository stays unit-testable. */
fun interface PendingSendScheduler {
    fun schedule()
}

@Singleton
class WorkManagerPendingSendScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : PendingSendScheduler {
    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<PendingMessageWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(PendingMessageWorker.WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}
