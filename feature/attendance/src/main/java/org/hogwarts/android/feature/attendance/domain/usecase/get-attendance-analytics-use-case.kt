package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.AdvancedAttendanceAnalytics
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case to retrieve advanced attendance analytics.
 *
 * Returns heatmap data, day-of-week patterns, and subject correlations.
 */
class GetAttendanceAnalyticsUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        studentId: String? = null
    ): AdvancedAttendanceAnalytics =
        repository.getAnalytics(startDate, endDate, studentId)
}
