package org.hogwarts.android.feature.attendance.data.local

import java.time.Instant
import java.time.LocalDate

data class PendingAttendance(
    val id: String,
    val studentId: String,
    val classId: String,
    val date: LocalDate,
    val status: String,
    val note: String? = null,
    val createdAt: Instant = Instant.now(),
    val retryCount: Int = 0,
    val syncStatus: PendingAttendanceSyncStatus = PendingAttendanceSyncStatus.QUEUED
)

enum class PendingAttendanceSyncStatus {
    QUEUED, SYNCING, SYNCED, FAILED
}
