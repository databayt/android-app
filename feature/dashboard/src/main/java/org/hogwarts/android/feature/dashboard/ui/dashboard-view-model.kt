package org.hogwarts.android.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.remote.NextActionDto
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import org.hogwarts.android.feature.dashboard.data.repository.DashboardResult
import org.hogwarts.android.feature.dashboard.data.repository.DashboardSectionsRepository
import org.hogwarts.android.feature.dashboard.data.repository.SectionsResult
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val sectionsRepository: DashboardSectionsRepository,
    private val tenantContext: TenantContext,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(role = tenantContext.userRole ?: UserRole.UNKNOWN, weekday = deviceWeekday()),
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.cached()?.let { cached -> _uiState.update { it.copy(data = cached, isLoading = false) } }
            load(refreshing = false)
        }
    }

    fun refresh() {
        viewModelScope.launch { load(refreshing = true) }
    }

    /** "Acknowledge" is a local dismissal, as on the web — it goes nowhere. */
    fun acknowledge(action: NextActionDto) {
        val index = _uiState.value.data?.nextActions?.indexOf(action) ?: -1
        if (index >= 0) _uiState.update { it.copy(dismissedActions = it.dismissedActions + index) }
    }

    private suspend fun load(refreshing: Boolean) {
        _uiState.update { it.copy(isRefreshing = refreshing, weekday = deviceWeekday()) }
        when (val result = repository.refresh()) {
            is DashboardResult.Fresh -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = false,
                    error = null,
                    data = result.data,
                    role = UserRole.fromWire(result.data.role).takeIf { r -> r != UserRole.UNKNOWN } ?: it.role,
                )
            }
            is DashboardResult.Cached -> _uiState.update {
                it.copy(isLoading = false, isRefreshing = false, isOffline = true, data = result.data)
            }
            is DashboardResult.Failed -> _uiState.update {
                it.copy(isLoading = false, isRefreshing = false, error = result.message)
            }
        }
        loadSections()
    }

    /**
     * The two tables are a second read and never block the page: a 404 (the
     * route is not on this school's server yet) or a failed read leaves
     * `sections` null, and both sections hide themselves.
     */
    private suspend fun loadSections() {
        val sections = when (val result = sectionsRepository.load()) {
            is SectionsResult.Ready -> result.data
            SectionsResult.Unavailable -> null
        }
        _uiState.update { it.copy(sections = sections) }
    }

    /** 0 = Sunday, matching the server's `day_of_week`. */
    private fun deviceWeekday(): Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
}
