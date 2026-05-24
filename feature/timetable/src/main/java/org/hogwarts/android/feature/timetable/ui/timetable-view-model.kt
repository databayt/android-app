package org.hogwarts.android.feature.timetable.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.timetable.domain.model.TimetableEntry
import org.hogwarts.android.feature.timetable.domain.usecase.GetTimetableUseCase
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val getTimetableUseCase: GetTimetableUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState(selectedDay = LocalDate.now().dayOfWeek))
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        loadTimetable()
    }

    private fun loadTimetable() {
        val userId = tenantContext.userId ?: return
        viewModelScope.launch {
            getTimetableUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, entries = resource.data ?: emptyList()) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, entries = resource.data ?: emptyList(), error = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, entries = resource.data ?: emptyList(), error = resource.error?.message) }
                }
            }
        }
    }

    fun selectDay(day: DayOfWeek) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    fun getEntriesForSelectedDay(): List<TimetableEntry> =
        _uiState.value.entries.filter { it.dayOfWeek == _uiState.value.selectedDay }.sortedBy { it.startTime }
}
