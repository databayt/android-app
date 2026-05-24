package org.hogwarts.android.feature.attendance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.attendance.domain.usecase.GetAttendanceAnalyticsUseCase
import javax.inject.Inject

/**
 * ViewModel for the Advanced Attendance Analytics screen.
 *
 * Manages heatmap, day-of-week patterns, and subject correlation data.
 */
@HiltViewModel
class AttendanceAnalyticsViewModel @Inject constructor(
    private val getAttendanceAnalyticsUseCase: GetAttendanceAnalyticsUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceAnalyticsUiState())
    val uiState: StateFlow<AttendanceAnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        val userId = tenantContext.userId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val analytics = getAttendanceAnalyticsUseCase(studentId = userId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        analytics = analytics,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load analytics"
                    )
                }
            }
        }
    }

    fun selectTab(tab: AnalyticsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retry() {
        loadAnalytics()
    }
}
