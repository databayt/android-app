package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.AttendanceBadge
import javax.inject.Inject

/**
 * Use case to retrieve attendance badges for gamification.
 *
 * Returns all available badges with earned status for the given student.
 */
class GetBadgesUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(studentId: String? = null): List<AttendanceBadge> =
        repository.getBadges(studentId)
}
