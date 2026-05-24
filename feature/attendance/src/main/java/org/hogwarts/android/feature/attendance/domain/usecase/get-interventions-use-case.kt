package org.hogwarts.android.feature.attendance.domain.usecase

import org.hogwarts.android.feature.attendance.data.repository.AdvancedAttendanceRepository
import org.hogwarts.android.feature.attendance.domain.model.AttendanceIntervention
import org.hogwarts.android.feature.attendance.domain.model.InterventionStatus
import javax.inject.Inject

/**
 * Use case to retrieve attendance interventions for students below threshold.
 *
 * Used by teachers and admins to monitor at-risk students.
 */
class GetInterventionsUseCase @Inject constructor(
    private val repository: AdvancedAttendanceRepository
) {
    suspend operator fun invoke(
        status: InterventionStatus? = null
    ): List<AttendanceIntervention> =
        repository.getInterventions(status)
}
