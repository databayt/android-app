package org.hogwarts.android.feature.guardian.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripPermissionUiState(
    val permissions: List<TripPermission> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TripPermissionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TripPermissionUiState())
    val uiState: StateFlow<TripPermissionUiState> = _uiState.asStateFlow()

    init { loadPermissions() }

    private fun loadPermissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API call placeholder
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun grantPermission(permissionId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(permissions = state.permissions.map {
                    if (it.id == permissionId) it.copy(status = "GRANTED") else it
                })
            }
        }
    }

    fun denyPermission(permissionId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(permissions = state.permissions.map {
                    if (it.id == permissionId) it.copy(status = "DENIED") else it
                })
            }
        }
    }
}
