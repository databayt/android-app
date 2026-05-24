package org.hogwarts.android.feature.guardian.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.guardian.data.repository.ChildTimetableSlot
import org.hogwarts.android.feature.guardian.domain.model.Child
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildTimetableUseCase
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildrenUseCase
import java.util.Calendar
import javax.inject.Inject

data class ChildTimetableUiState(
    val children: List<Child> = emptyList(),
    val selectedChildId: String? = null,
    val slots: List<ChildTimetableSlot> = emptyList(),
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredSlots: List<ChildTimetableSlot>
        get() = slots.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startTime }
}

@HiltViewModel
class ChildTimetableViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChildrenUseCase: GetChildrenUseCase,
    private val getChildTimetableUseCase: GetChildTimetableUseCase
) : ViewModel() {

    private val childId: String? = savedStateHandle["childId"]

    private val _uiState = MutableStateFlow(ChildTimetableUiState())
    val uiState: StateFlow<ChildTimetableUiState> = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            when (val result = getChildrenUseCase()) {
                is Result.Success -> {
                    val selected = childId ?: result.data.firstOrNull()?.id
                    _uiState.update { it.copy(children = result.data, selectedChildId = selected) }
                    selected?.let { loadTimetable(it) }
                }
                is Result.Error -> _uiState.update { it.copy(error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectChild(id: String) {
        _uiState.update { it.copy(selectedChildId = id) }
        loadTimetable(id)
    }

    fun selectDay(day: Int) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    private fun loadTimetable(childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getChildTimetableUseCase(childId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, slots = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }
}
