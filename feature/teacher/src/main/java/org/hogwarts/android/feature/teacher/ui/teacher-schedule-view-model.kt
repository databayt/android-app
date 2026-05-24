package org.hogwarts.android.feature.teacher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.teacher.domain.model.ScheduleSlot
import org.hogwarts.android.feature.teacher.domain.usecase.GetTeacherScheduleUseCase
import java.util.Calendar
import javax.inject.Inject

data class TeacherScheduleUiState(
    val allSlots: List<ScheduleSlot> = emptyList(),
    val filteredSlots: List<ScheduleSlot> = emptyList(),
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeacherScheduleViewModel @Inject constructor(
    private val getTeacherScheduleUseCase: GetTeacherScheduleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherScheduleUiState())
    val uiState: StateFlow<TeacherScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getTeacherScheduleUseCase()) {
                is Result.Success -> {
                    val slots = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allSlots = slots,
                            filteredSlots = slots.filter { slot -> slot.dayOfWeek == it.selectedDay }
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectDay(day: Int) {
        _uiState.update { state ->
            state.copy(
                selectedDay = day,
                filteredSlots = state.allSlots.filter { it.dayOfWeek == day }
            )
        }
    }
}
