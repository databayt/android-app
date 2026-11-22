package org.hogwarts.android.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.connectivity.ConnectivityObserver
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.remote.NextActionDto
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import org.hogwarts.android.feature.dashboard.data.repository.DashboardResult
import org.hogwarts.android.feature.dashboard.data.repository.DashboardSectionsRepository
import org.hogwarts.android.feature.dashboard.data.repository.RefreshFailure
import org.hogwarts.android.feature.dashboard.data.repository.SectionsResult
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val sectionsRepository: DashboardSectionsRepository,
    private val tenantContext: TenantContext,
    private val connectivity: ConnectivityObserver,
) : ViewModel() {

    /** Test seam for the resume throttle. */
    internal var clock: () -> Long = System::currentTimeMillis

    private val lastFailure = MutableStateFlow<RefreshFailure?>(null)
    private var loadJob: Job? = null
    private var retryJob: Job? = null
    private var retryAttempt = 0
    private var lastLoadStartMs = 0L

    private val _uiState = MutableStateFlow(
        DashboardUiState(role = tenantContext.userRole ?: UserRole.UNKNOWN, weekday = deviceWeekday()),
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadJob = viewModelScope.launch {
            repository.cached()?.let { cached -> _uiState.update { it.copy(data = cached, isLoading = false) } }
            load(refreshing = false)
        }
        // Which line to show follows the last failure AND the live network state.
        viewModelScope.launch {
            combine(lastFailure, connectivity.online) { failure, online -> refreshNotice(failure, online) }
                .collect { notice -> _uiState.update { it.copy(notice = notice) } }
        }
        // The network coming or going is worth a look: back online re-fetches; going offline confirms it.
        viewModelScope.launch {
            connectivity.online.drop(1).collect {
                retryAttempt = 0
                launchLoad(refreshing = false, force = false)
            }
        }
    }

    /** Pull-to-refresh and the retry button: always runs, and restarts the automatic back-off. */
    fun refresh() {
        retryAttempt = 0
        launchLoad(refreshing = true, force = true)
    }

    /** The screen is back in the foreground: re-fetch (throttled; at once after a failure). */
    fun onResume() {
        if (!shouldRefreshOnResume(lastLoadStartMs, clock(), hadFailure = lastFailure.value != null)) return
        retryAttempt = 0
        launchLoad(refreshing = false, force = false)
    }

    private fun launchLoad(refreshing: Boolean, force: Boolean) {
        if (loadJob?.isActive == true) {
            if (!force) return
            loadJob?.cancel()
        }
        loadJob = viewModelScope.launch { load(refreshing) }
    }

    /** After a failed refresh: try again by itself after 5 s, 15 s, 45 s, then wait for the user. */
    private fun scheduleRetry() {
        retryJob?.cancel()
        val wait = retryDelayMs(retryAttempt) ?: return
        retryAttempt++
        retryJob = viewModelScope.launch {
            delay(wait)
            launchLoad(refreshing = false, force = false)
        }
    }

    /** "Acknowledge" is a local dismissal, as on the web — it goes nowhere. */
    fun acknowledge(action: NextActionDto) {
        val index = _uiState.value.data?.nextActions?.indexOf(action) ?: -1
        if (index >= 0) _uiState.update { it.copy(dismissedActions = it.dismissedActions + index) }
    }

    private suspend fun load(refreshing: Boolean) {
        lastLoadStartMs = clock()
        _uiState.update { it.copy(isRefreshing = refreshing, weekday = deviceWeekday()) }
        when (val result = repository.refresh()) {
            is DashboardResult.Fresh -> {
                retryJob?.cancel()
                retryAttempt = 0
                lastFailure.value = null
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                        data = result.data,
                        role = UserRole.fromWire(result.data.role).takeIf { r -> r != UserRole.UNKNOWN } ?: it.role,
                    )
                }
            }
            is DashboardResult.Cached -> {
                lastFailure.value = result.failure
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, data = result.data) }
                scheduleRetry()
            }
            is DashboardResult.Failed -> {
                lastFailure.value = result.failure
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = result.message) }
                scheduleRetry()
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
