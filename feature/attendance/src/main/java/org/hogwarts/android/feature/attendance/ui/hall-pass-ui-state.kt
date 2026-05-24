package org.hogwarts.android.feature.attendance.ui

import org.hogwarts.android.feature.attendance.domain.model.DestinationType
import org.hogwarts.android.feature.attendance.domain.model.HallPass
import org.hogwarts.android.feature.attendance.domain.model.HallPassStatus

/**
 * UI state for the Hall Pass screen.
 */
data class HallPassUiState(
    val isLoading: Boolean = true,
    val hallPasses: List<HallPass> = emptyList(),
    val error: String? = null,
    val isTeacher: Boolean = false,
    val selectedFilter: HallPassFilter = HallPassFilter.ALL,

    // Request form state (student)
    val showRequestForm: Boolean = false,
    val selectedDestination: DestinationType = DestinationType.RESTROOM,
    val reason: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
) {
    val activePasses: List<HallPass>
        get() = hallPasses.filter { it.status == HallPassStatus.ACTIVE }

    val pendingPasses: List<HallPass>
        get() = hallPasses.filter { it.status == HallPassStatus.REQUESTED }

    val filteredPasses: List<HallPass>
        get() = when (selectedFilter) {
            HallPassFilter.ALL -> hallPasses
            HallPassFilter.ACTIVE -> hallPasses.filter { it.status == HallPassStatus.ACTIVE }
            HallPassFilter.PENDING -> hallPasses.filter { it.status == HallPassStatus.REQUESTED }
            HallPassFilter.EXPIRED -> hallPasses.filter { it.status == HallPassStatus.EXPIRED }
            HallPassFilter.DENIED -> hallPasses.filter { it.status == HallPassStatus.DENIED }
        }
}

enum class HallPassFilter {
    ALL, ACTIVE, PENDING, EXPIRED, DENIED
}
