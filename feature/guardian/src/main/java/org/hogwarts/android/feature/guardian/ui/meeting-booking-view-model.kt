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

data class TeacherOption(val id: String, val name: String)
data class TimeSlot(val id: String, val time: String, val duration: String, val isAvailable: Boolean)

data class MeetingBookingUiState(
    val teachers: List<TeacherOption> = emptyList(),
    val selectedTeacherId: String? = null,
    val availableDates: List<String> = emptyList(),
    val selectedDate: String? = null,
    val timeSlots: List<TimeSlot> = emptyList(),
    val selectedTimeSlotId: String? = null,
    val agenda: String = "",
    val isLoading: Boolean = false,
    val isBooked: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MeetingBookingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingBookingUiState())
    val uiState: StateFlow<MeetingBookingUiState> = _uiState.asStateFlow()

    init { loadTeachers() }

    private fun loadTeachers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API call placeholder
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectTeacher(teacherId: String) {
        _uiState.update { it.copy(selectedTeacherId = teacherId) }
        loadAvailableDates()
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadTimeSlots()
    }

    fun selectTimeSlot(slotId: String) {
        _uiState.update { it.copy(selectedTimeSlotId = slotId) }
    }

    fun updateAgenda(agenda: String) {
        _uiState.update { it.copy(agenda = agenda) }
    }

    fun bookMeeting() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API call to book meeting
            _uiState.update { it.copy(isLoading = false, isBooked = true) }
        }
    }

    private fun loadAvailableDates() {
        viewModelScope.launch {
            // API call placeholder
        }
    }

    private fun loadTimeSlots() {
        viewModelScope.launch {
            // API call placeholder
        }
    }
}
