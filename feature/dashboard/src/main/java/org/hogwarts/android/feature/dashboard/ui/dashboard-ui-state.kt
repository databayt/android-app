package org.hogwarts.android.feature.dashboard.ui

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** The data on screen came from the offline cache. */
    val isOffline: Boolean = false,
    val error: String? = null,
    val role: UserRole = UserRole.UNKNOWN,
    val data: DashboardDto? = null,
    /** Indexes of next actions the reader acknowledged this session. */
    val dismissedActions: Set<Int> = emptySet(),
) {
    val nextActions get() = data?.nextActions.orEmpty().filterIndexed { i, _ -> i !in dismissedActions }
}
