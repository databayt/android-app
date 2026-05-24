package org.hogwarts.android.feature.attendance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStatus
import org.hogwarts.android.feature.attendance.domain.usecase.GetAttendanceUseCase
import javax.inject.Inject

/**
 * ViewModel for the Attendance screen.
 */
@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val getAttendanceUseCase: GetAttendanceUseCase,
    private val tenantContext: TenantContext,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    init {
        loadAttendance()
    }

    private fun loadAttendance() {
        val userId = tenantContext.userId ?: return
        val isTeacher = tenantContext.hasAnyRole(UserRole.TEACHER, UserRole.ADMIN, UserRole.SUPER_ADMIN)

        _uiState.update { it.copy(isTeacher = isTeacher) }

        viewModelScope.launch {
            getAttendanceUseCase(studentId = userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                records = resource.data ?: emptyList()
                            )
                        }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = resource.data ?: emptyList(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = resource.data ?: emptyList(),
                                error = resource.error?.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun setFilter(filter: AttendanceFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    /**
     * Get filtered records based on selected filter.
     */
    fun getFilteredRecords(): List<org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord> {
        val state = _uiState.value
        return when (state.selectedFilter) {
            AttendanceFilter.ALL -> state.records
            AttendanceFilter.PRESENT -> state.records.filter { it.status == AttendanceStatus.PRESENT }
            AttendanceFilter.ABSENT -> state.records.filter { it.status == AttendanceStatus.ABSENT }
            AttendanceFilter.LATE -> state.records.filter { it.status == AttendanceStatus.LATE }
            AttendanceFilter.EXCUSED -> state.records.filter { it.status == AttendanceStatus.EXCUSED }
        }
    }

    fun retry() {
        loadAttendance()
    }
}
