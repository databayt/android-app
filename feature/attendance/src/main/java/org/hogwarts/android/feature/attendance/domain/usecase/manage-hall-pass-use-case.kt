package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.DestinationType
import org.hogwarts.android.feature.attendance.domain.model.HallPass
import org.hogwarts.android.feature.attendance.domain.model.HallPassStatus
import javax.inject.Inject

/**
 * Use case to request a new hall pass (student action).
 */
class RequestHallPassUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(
        studentId: String,
        destination: DestinationType,
        reason: String
    ): HallPass = repository.requestHallPass(studentId, destination, reason)
}

/**
 * Use case to approve or deny a hall pass (teacher/admin action).
 */
class ApproveHallPassUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(hallPassId: String, approved: Boolean): HallPass =
        repository.approveHallPass(hallPassId, approved)
}

/**
 * Use case to get hall passes with optional filters.
 */
class GetHallPassesUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(
        status: HallPassStatus? = null,
        studentId: String? = null
    ): List<HallPass> = repository.getHallPasses(status, studentId)
}
