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
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.attendance.domain.model.DestinationType
import org.hogwarts.android.feature.attendance.domain.usecase.ApproveHallPassUseCase
import org.hogwarts.android.feature.attendance.domain.usecase.GetHallPassesUseCase
import org.hogwarts.android.feature.attendance.domain.usecase.RequestHallPassUseCase
import javax.inject.Inject

/**
 * ViewModel for the Hall Pass screen.
 *
 * Students can request hall passes; teachers can approve/deny them.
 */
@HiltViewModel
class HallPassViewModel @Inject constructor(
    private val getHallPassesUseCase: GetHallPassesUseCase,
    private val requestHallPassUseCase: RequestHallPassUseCase,
    private val approveHallPassUseCase: ApproveHallPassUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(HallPassUiState())
    val uiState: StateFlow<HallPassUiState> = _uiState.asStateFlow()

    init {
        val isTeacher = tenantContext.hasAnyRole(UserRole.TEACHER, UserRole.ADMIN, UserRole.SUPER_ADMIN)
        _uiState.update { it.copy(isTeacher = isTeacher) }
        loadHallPasses()
    }

    private fun loadHallPasses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val studentId = if (!_uiState.value.isTeacher) tenantContext.userId else null
                val passes = getHallPassesUseCase(studentId = studentId)
                _uiState.update {
                    it.copy(isLoading = false, hallPasses = passes, error = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load hall passes")
                }
            }
        }
    }

    fun setFilter(filter: HallPassFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun showRequestForm() {
        _uiState.update {
            it.copy(
                showRequestForm = true,
                selectedDestination = DestinationType.RESTROOM,
                reason = "",
                submitError = null,
                submitSuccess = false
            )
        }
    }

    fun hideRequestForm() {
        _uiState.update { it.copy(showRequestForm = false) }
    }

    fun updateDestination(destination: DestinationType) {
        _uiState.update { it.copy(selectedDestination = destination) }
    }

    fun updateReason(reason: String) {
        _uiState.update { it.copy(reason = reason) }
    }

    fun submitRequest() {
        val userId = tenantContext.userId ?: return
        val state = _uiState.value

        if (state.reason.isBlank()) {
            _uiState.update { it.copy(submitError = "Please provide a reason") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            try {
                requestHallPassUseCase(
                    studentId = userId,
                    destination = state.selectedDestination,
                    reason = state.reason
                )
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showRequestForm = false,
                        submitSuccess = true
                    )
                }
                loadHallPasses()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = e.message ?: "Failed to submit request"
                    )
                }
            }
        }
    }

    fun approvePass(hallPassId: String) {
        viewModelScope.launch {
            try {
                approveHallPassUseCase(hallPassId, approved = true)
                loadHallPasses()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to approve pass")
                }
            }
        }
    }

    fun denyPass(hallPassId: String) {
        viewModelScope.launch {
            try {
                approveHallPassUseCase(hallPassId, approved = false)
                loadHallPasses()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to deny pass")
                }
            }
        }
    }

    fun retry() {
        loadHallPasses()
    }

    fun clearSubmitSuccess() {
        _uiState.update { it.copy(submitSuccess = false) }
    }
}
