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

data class SyncSettingsUiState(
    val syncFrequency: String = "Every 30 min",
    val wifiOnly: Boolean = false,
    val syncAttendance: Boolean = true,
    val syncGrades: Boolean = true,
    val syncTimetable: Boolean = true,
    val syncMessages: Boolean = true,
    val syncNotifications: Boolean = true,
    val syncFees: Boolean = true,
    val syncLibrary: Boolean = true,
    val totalCacheSize: String = "0 MB",
    val lastSyncTime: String = "Never",
    val isSyncing: Boolean = false
)

@HiltViewModel
class SyncSettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SyncSettingsUiState())
    val uiState: StateFlow<SyncSettingsUiState> = _uiState.asStateFlow()

    fun setSyncFrequency(frequency: String) {
        _uiState.update { it.copy(syncFrequency = frequency) }
    }

    fun setWifiOnly(wifiOnly: Boolean) {
        _uiState.update { it.copy(wifiOnly = wifiOnly) }
    }

    fun toggleEntity(entity: String, enabled: Boolean) {
        _uiState.update { state ->
            when (entity) {
                "attendance" -> state.copy(syncAttendance = enabled)
                "grades" -> state.copy(syncGrades = enabled)
                "timetable" -> state.copy(syncTimetable = enabled)
                "messages" -> state.copy(syncMessages = enabled)
                "notifications" -> state.copy(syncNotifications = enabled)
                "fees" -> state.copy(syncFees = enabled)
                "library" -> state.copy(syncLibrary = enabled)
                else -> state
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            // Trigger sync
            _uiState.update { it.copy(isSyncing = false, lastSyncTime = "Just now") }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // Clear Room tables
            _uiState.update { it.copy(totalCacheSize = "0 MB") }
        }
    }
}
