package org.hogwarts.android.feature.attendance.ui

import org.hogwarts.android.feature.attendance.domain.model.AttendanceIntervention
import org.hogwarts.android.feature.attendance.domain.model.InterventionStatus

/**
 * UI state for the Interventions screen.
 */
data class InterventionsUiState(
    val isLoading: Boolean = true,
    val interventions: List<AttendanceIntervention> = emptyList(),
    val error: String? = null,
    val selectedFilter: InterventionFilter = InterventionFilter.ALL
) {
    val filteredInterventions: List<AttendanceIntervention>
        get() = when (selectedFilter) {
            InterventionFilter.ALL -> interventions
            InterventionFilter.PENDING -> interventions.filter { it.status == InterventionStatus.PENDING }
            InterventionFilter.IN_PROGRESS -> interventions.filter { it.status == InterventionStatus.IN_PROGRESS }
            InterventionFilter.RESOLVED -> interventions.filter { it.status == InterventionStatus.RESOLVED }
        }

    val criticalCount: Int
        get() = interventions.count { it.deficit > 15f }
}

enum class InterventionFilter {
    ALL, PENDING, IN_PROGRESS, RESOLVED
}
