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

data class CommunicationPreferencesUiState(
    val attendanceEnabled: Boolean = true,
    val gradesEnabled: Boolean = true,
    val feesEnabled: Boolean = true,
    val announcementsEnabled: Boolean = true,
    val eventsEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val emergencyOverride: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class CommunicationPreferencesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CommunicationPreferencesUiState())
    val uiState: StateFlow<CommunicationPreferencesUiState> = _uiState.asStateFlow()

    fun toggleAttendance(enabled: Boolean) { _uiState.update { it.copy(attendanceEnabled = enabled) } }
    fun toggleGrades(enabled: Boolean) { _uiState.update { it.copy(gradesEnabled = enabled) } }
    fun toggleFees(enabled: Boolean) { _uiState.update { it.copy(feesEnabled = enabled) } }
    fun toggleAnnouncements(enabled: Boolean) { _uiState.update { it.copy(announcementsEnabled = enabled) } }
    fun toggleEvents(enabled: Boolean) { _uiState.update { it.copy(eventsEnabled = enabled) } }
    fun toggleQuietHours(enabled: Boolean) { _uiState.update { it.copy(quietHoursEnabled = enabled) } }
    fun toggleEmergencyOverride(enabled: Boolean) { _uiState.update { it.copy(emergencyOverride = enabled) } }

    fun savePreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API call to save preferences
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
