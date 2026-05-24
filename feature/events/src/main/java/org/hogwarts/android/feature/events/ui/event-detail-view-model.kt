package org.hogwarts.android.feature.events.ui

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
import org.hogwarts.android.feature.events.domain.model.Event
import org.hogwarts.android.feature.events.domain.usecase.GetEventDetailUseCase
import org.hogwarts.android.feature.events.domain.usecase.RegisterForEventUseCase
import org.hogwarts.android.feature.events.domain.usecase.UnregisterFromEventUseCase
import javax.inject.Inject

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val isRegistering: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEventDetailUseCase: GetEventDetailUseCase,
    private val registerForEventUseCase: RegisterForEventUseCase,
    private val unregisterFromEventUseCase: UnregisterFromEventUseCase
) : ViewModel() {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getEventDetailUseCase(eventId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, event = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true) }
            when (val result = registerForEventUseCase(eventId)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isRegistering = false,
                            event = state.event?.copy(
                                isRegistered = true,
                                currentAttendees = state.event.currentAttendees + 1
                            )
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isRegistering = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun unregister() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true) }
            when (val result = unregisterFromEventUseCase(eventId)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isRegistering = false,
                            event = state.event?.copy(
                                isRegistered = false,
                                currentAttendees = (state.event.currentAttendees - 1).coerceAtLeast(0)
                            )
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isRegistering = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }
}
