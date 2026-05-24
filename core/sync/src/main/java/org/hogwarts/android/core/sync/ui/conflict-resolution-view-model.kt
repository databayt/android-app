package org.hogwarts.android.core.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConflictResolutionUiState(
    val conflicts: List<SyncConflict> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConflictResolutionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ConflictResolutionUiState())
    val uiState: StateFlow<ConflictResolutionUiState> = _uiState.asStateFlow()

    init { loadConflicts() }

    private fun loadConflicts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Load conflicts from sync engine
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun resolveWithLocal(conflictId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(conflicts = state.conflicts.filter { it.id != conflictId })
            }
        }
    }

    fun resolveWithServer(conflictId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(conflicts = state.conflicts.filter { it.id != conflictId })
            }
        }
    }

    fun resolveAllWithServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(conflicts = emptyList()) }
        }
    }
}
