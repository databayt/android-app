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
import org.hogwarts.android.feature.events.domain.model.EventType
import org.hogwarts.android.feature.events.domain.usecase.GetEventsUseCase
import javax.inject.Inject

data class EventsListUiState(
    val allEvents: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val selectedType: EventType? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EventsListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsListUiState())
    val uiState: StateFlow<EventsListUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getEventsUseCase()) {
                is Result.Success -> {
                    val events = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allEvents = events,
                            filteredEvents = filterEvents(events, it.selectedType)
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectType(type: EventType?) {
        _uiState.update { state ->
            state.copy(
                selectedType = type,
                filteredEvents = filterEvents(state.allEvents, type)
            )
        }
    }

    fun refresh() = loadEvents()

    private fun filterEvents(events: List<Event>, type: EventType?): List<Event> {
        return if (type == null) events else events.filter { it.type == type }
    }
}
