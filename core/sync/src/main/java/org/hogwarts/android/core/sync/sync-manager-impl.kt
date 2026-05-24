package org.hogwarts.android.core.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManagerImpl @Inject constructor(
    private val workManager: WorkManager
) : SyncManager {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()

    companion object {
        const val PERIODIC_SYNC_WORK = "hogwarts_periodic_sync"
        const val ONE_TIME_SYNC_WORK = "hogwarts_one_time_sync"
        const val FULL_SYNC_WORK = "hogwarts_full_sync"
        private const val SYNC_INTERVAL_MINUTES = 15L
    }

    init {
        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Timber.d("Scheduled periodic sync every $SYNC_INTERVAL_MINUTES minutes")
    }

    override suspend fun requestSync() {
        _syncStatus.value = SyncStatus.Syncing

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            ONE_TIME_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        Timber.d("Requested one-time sync")
    }

    override suspend fun requestFullSync() {
        _syncStatus.value = SyncStatus.Syncing

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("full_sync")
            .build()

        workManager.enqueueUniqueWork(
            FULL_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        Timber.d("Requested full sync")
    }

    override fun cancelSync() {
        workManager.cancelUniqueWork(ONE_TIME_SYNC_WORK)
        workManager.cancelUniqueWork(FULL_SYNC_WORK)
        _syncStatus.value = SyncStatus.Idle
        Timber.d("Cancelled sync")
    }

    fun updateStatus(status: SyncStatus) {
        _syncStatus.value = status
    }
}
