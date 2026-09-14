package org.hogwarts.android.feature.attendance.data.outbox

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
import org.hogwarts.android.feature.attendance.data.remote.AttendanceApi
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Delivers parked quick-attendance marks once a connection is back. */
@HiltWorker
class AttendanceSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val outbox: AttendanceOutbox,
    private val api: AttendanceApi,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = when (outbox.drain(api)) {
        DrainResult.Done -> Result.success()
        DrainResult.RetryLater -> Result.retry()
    }

    companion object {
        const val WORK_NAME = "attendance_outbox_drain"
    }
}

/** Unique, network-bound, exponential backoff; a new mark joins a running drain. */
class WorkManagerAttendanceSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AttendanceSyncScheduler {

    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<AttendanceSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(AttendanceSyncWorker.WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}
