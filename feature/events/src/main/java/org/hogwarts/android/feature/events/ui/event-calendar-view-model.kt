package org.hogwarts.android.feature.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.events.domain.model.Event
import org.hogwarts.android.feature.events.domain.usecase.GetCalendarEventsUseCase
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class EventCalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val events: List<Event> = emptyList(),
    val eventDates: Set<Int> = emptySet(),
    val selectedDate: LocalDate? = null,
    val selectedDateEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EventCalendarViewModel @Inject constructor(
    private val getCalendarEventsUseCase: GetCalendarEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventCalendarUiState())
    val uiState: StateFlow<EventCalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonthEvents()
    }

    private fun loadMonthEvents() {
        viewModelScope.launch {
            val month = _uiState.value.currentMonth
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getCalendarEventsUseCase(month.year, month.monthValue)) {
                is Result.Success -> {
                    val events = result.data
                    val eventDays = events.mapNotNull { event ->
                        runCatching { LocalDate.parse(event.startDate).dayOfMonth }.getOrNull()
                    }.toSet()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            events = events,
                            eventDates = eventDays
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun previousMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.minusMonths(1), selectedDate = null, selectedDateEvents = emptyList()) }
        loadMonthEvents()
    }

    fun nextMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.plusMonths(1), selectedDate = null, selectedDateEvents = emptyList()) }
        loadMonthEvents()
    }

    fun selectDate(date: LocalDate) {
        val dateStr = date.toString()
        val eventsForDate = _uiState.value.events.filter { it.startDate == dateStr }
        _uiState.update { it.copy(selectedDate = date, selectedDateEvents = eventsForDate) }
    }
}
