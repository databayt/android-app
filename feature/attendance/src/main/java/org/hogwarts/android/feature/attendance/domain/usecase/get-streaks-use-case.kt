package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStreak
import javax.inject.Inject

/**
 * Use case to retrieve attendance streak data.
 *
 * Returns current streak, longest streak, and streak start date.
 */
class GetStreaksUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(studentId: String? = null): AttendanceStreak =
        repository.getStreaks(studentId)
}
