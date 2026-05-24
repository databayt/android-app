package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import org.hogwarts.android.feature.attendance.domain.model.BulkAttendanceRequest
import org.hogwarts.android.feature.attendance.domain.model.MarkAttendanceRequest
import javax.inject.Inject

/**
 * Use case to mark attendance for a single student.
 */
class MarkAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(request: MarkAttendanceRequest): AttendanceRecord =
        repository.markAttendance(request)
}

/**
 * Use case to mark attendance for an entire class (bulk).
 */
class MarkBulkAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(request: BulkAttendanceRequest): List<AttendanceRecord> =
        repository.markBulkAttendance(request)
}
