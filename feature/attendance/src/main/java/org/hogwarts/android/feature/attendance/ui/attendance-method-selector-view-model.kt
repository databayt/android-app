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
import org.hogwarts.android.feature.attendance.domain.model.AttendanceMethod
import org.hogwarts.android.feature.attendance.domain.model.MethodType
import org.hogwarts.android.feature.attendance.domain.usecase.GetMethodsUseCase
import org.hogwarts.android.feature.attendance.domain.usecase.UpdateMethodsUseCase
import javax.inject.Inject

/**
 * ViewModel for the Attendance Method Selector screen.
 *
 * Manages toggle states for each attendance capture method
 * (Manual, Geofence, NFC, Bluetooth, Barcode) and their permission statuses.
 */
@HiltViewModel
class AttendanceMethodSelectorViewModel @Inject constructor(
    private val getMethodsUseCase: GetMethodsUseCase,
    private val updateMethodsUseCase: UpdateMethodsUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceMethodSelectorUiState())
    val uiState: StateFlow<AttendanceMethodSelectorUiState> = _uiState.asStateFlow()

    init {
        loadMethods()
    }

    private fun loadMethods() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                tenantContext.requireSchoolId()
                val methods = getMethodsUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        methods = methods,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load methods"
                    )
                }
            }
        }
    }

    /**
     * Toggle a method's enabled status.
     */
    fun toggleMethod(methodType: MethodType) {
        _uiState.update { state ->
            val updatedMethods = state.methods.map { method ->
                if (method.type == methodType) {
                    method.copy(enabled = !method.enabled)
                } else {
                    method
                }
            }
            state.copy(methods = updatedMethods)
        }
    }

    /**
     * Set a method as the default.
     */
    fun setDefaultMethod(methodType: MethodType) {
        _uiState.update { state ->
            val updatedMethods = state.methods.map { method ->
                method.copy(
                    isDefault = method.type == methodType,
                    enabled = if (method.type == methodType) true else method.enabled
                )
            }
            state.copy(methods = updatedMethods)
        }
    }

    /**
     * Save method configuration to the backend.
     */
    fun saveMethods() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updated = updateMethodsUseCase(_uiState.value.methods)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        methods = updated,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save settings"
                    )
                }
            }
        }
    }

    /**
     * Update permission status for a specific method.
     */
    fun updatePermissionStatus(methodType: MethodType, status: PermissionStatus) {
        _uiState.update { state ->
            state.copy(
                permissionStatuses = state.permissionStatuses + (methodType to status)
            )
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun retry() {
        loadMethods()
    }
}
