package org.hogwarts.android.shell.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.preferences.AppPreferences
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import javax.inject.Inject

data class SearchUiState(
    val role: UserRole? = null,
    val enabledModules: List<String>? = null,
    val recentIds: List<String> = emptyList(),
)

/**
 * What the search needs to draw its rows: the reader's role and the school's
 * module toggles — the same two the sidebar filters on — and the ids of what
 * they opened last.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    tenantContext: TenantContext,
    private val preferences: AppPreferences,
    dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState(role = tenantContext.userRole))
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dashboardRepository.latest.collect { dto ->
                _state.update { it.copy(enabledModules = dto?.school?.enabledModules) }
            }
        }
        viewModelScope.launch {
            preferences.searchRecents.collect { ids -> _state.update { it.copy(recentIds = ids) } }
        }
    }

    fun remember(item: SearchItem) {
        viewModelScope.launch { preferences.recordSearchRecent(item.id) }
    }
}
