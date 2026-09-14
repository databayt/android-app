package org.hogwarts.android.feature.attendance.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.attendance.data.repository.AttendanceRepository
import org.hogwarts.android.feature.attendance.di.AttendanceClock
import org.hogwarts.android.feature.attendance.domain.model.TodayTotals
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class StaffOverviewUiState(
    val loading: Boolean = true,
    val totals: TodayTotals? = null,
    val loadFailed: Boolean = false,
    /** ADMIN or DEVELOPER: sees the admin-only quick-access tiles. */
    val isAdmin: Boolean = false,
)

/** School-wide attendance today for admin / staff — mirrors `attendance/overview/content.tsx`. */
@HiltViewModel
class StaffOverviewViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val tenantContext: TenantContext,
    @AttendanceClock private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffOverviewUiState(isAdmin = tenantContext.userRole?.isAdmin == true))
    val uiState: StateFlow<StaffOverviewUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            try {
                val totals = repository.todayTotals(LocalDate.now(clock))
                _uiState.update { it.copy(loading = false, totals = totals) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, loadFailed = true) }
            }
        }
    }
}
