package org.hogwarts.android.core.sync.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SyncHistoryEntry(
    val description: String,
    val timestamp: String
)

data class SyncProgressUiState(
    val overallProgress: Float = 1.0f,
    val lastOverallSync: String = "Never",
    val entities: List<EntitySyncStatus> = emptyList(),
    val syncHistory: List<SyncHistoryEntry> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SyncProgressViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SyncProgressUiState())
    val uiState: StateFlow<SyncProgressUiState> = _uiState.asStateFlow()
}
