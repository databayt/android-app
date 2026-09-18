package org.hogwarts.android.feature.dashboard.ui

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.remote.DashboardDto
import org.hogwarts.android.feature.dashboard.data.remote.DashboardSectionsDto

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** The data on screen came from the offline cache. */
    val isOffline: Boolean = false,
    val error: String? = null,
    val role: UserRole = UserRole.UNKNOWN,
    val data: DashboardDto? = null,
    /**
     * The role's two tables. Null while they are loading, and null for good
     * when the route is not deployed — both sections then stay off the page.
     */
    val sections: DashboardSectionsDto? = null,
    /**
     * The device's weekday, 0 = Sunday, for deciding whether the day the
     * server resolved is today when it does not say so itself.
     */
    val weekday: Int = 0,
    /** Indexes of next actions the reader acknowledged this session. */
    val dismissedActions: Set<Int> = emptySet(),
) {
    val nextActions get() = data?.nextActions.orEmpty().filterIndexed { i, _ -> i !in dismissedActions }
}
