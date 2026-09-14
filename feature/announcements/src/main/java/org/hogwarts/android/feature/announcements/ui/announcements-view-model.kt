package org.hogwarts.android.feature.announcements.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.announcements.data.repository.AnnouncementsRepository
import org.hogwarts.android.feature.announcements.data.repository.ListResult
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import javax.inject.Inject

data class AnnouncementsUiState(
    val role: UserRole = UserRole.UNKNOWN,
    val query: String = "",
    val items: List<Announcement> = emptyList(),
    val page: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    /** Showing the saved first page because the network failed. */
    val isOffline: Boolean = false,
    /** Nothing to show and the network failed. */
    val failed: Boolean = false,
) {
    /** `permissions.ts` WRITE_ROLES — they get the authoring tabs. */
    val canWrite: Boolean get() = role in WRITE_ROLES
    /** `ADMIN_ROLES` — they also get Settings. */
    val isAdmin: Boolean get() = role.isAdmin

    companion object {
        val WRITE_ROLES = setOf(UserRole.DEVELOPER, UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER)
    }
}

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val repository: AnnouncementsRepository,
    tenantContext: TenantContext,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementsUiState(role = tenantContext.userRole ?: UserRole.UNKNOWN))
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            repository.cachedFirstPage()?.let { cached ->
                _uiState.update {
                    if (it.page == 0) it.copy(items = cached.items, page = 1, hasMore = cached.hasMore, isLoading = false) else it
                }
            }
        }
        reload(debounce = false)
    }

    /** The toolbar search, debounced like the web's `useDeferredValue`. */
    fun onQueryChange(query: String) {
        if (query == _uiState.value.query) return
        _uiState.update { it.copy(query = query) }
        reload(debounce = true)
    }

    fun retry() = reload(debounce = false)

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoadingMore || state.isLoading) return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.page(state.page + 1, state.query)) {
                is ListResult.Fresh -> _uiState.update { s ->
                    val known = s.items.map { it.id }.toSet()
                    s.copy(
                        items = s.items + result.page.items.filter { it.id !in known },
                        page = result.page.page,
                        hasMore = result.page.hasMore,
                        isLoadingMore = false,
                    )
                }
                else -> _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun reload(debounce: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isLoading = it.items.isEmpty() || debounce, failed = false) }
            val query = _uiState.value.query
            when (val result = repository.page(1, query)) {
                is ListResult.Fresh -> _uiState.update {
                    it.copy(items = result.page.items, page = 1, hasMore = result.page.hasMore, isLoading = false, isOffline = false, failed = false)
                }
                is ListResult.Cached -> _uiState.update {
                    it.copy(items = result.page.items, page = 1, hasMore = false, isLoading = false, isOffline = true, failed = false)
                }
                is ListResult.Failed -> _uiState.update {
                    it.copy(isLoading = false, isOffline = it.items.isNotEmpty(), failed = it.items.isEmpty() || query.isNotBlank(), items = if (query.isNotBlank()) emptyList() else it.items)
                }
            }
        }
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
