package org.hogwarts.android.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.Instant

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: SyncManagerImpl,
    private val mutationQueue: MutationQueue
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val isFullSync = tags.contains("full_sync")
        Timber.d("Starting sync (full=$isFullSync)")

        return try {
            syncManager.updateStatus(SyncStatus.Syncing)

            // Process pending mutations first (FIFO)
            mutationQueue.processPending()

            syncManager.updateStatus(SyncStatus.Success(Instant.now()))
            Timber.d("Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            syncManager.updateStatus(SyncStatus.Error(e.message ?: "Sync failed", null))

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
