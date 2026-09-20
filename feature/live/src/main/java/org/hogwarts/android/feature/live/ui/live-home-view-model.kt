package org.hogwarts.android.feature.live.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.live.data.repository.LiveRepository
import org.hogwarts.android.feature.live.domain.model.LiveSession
import timber.log.Timber
import javax.inject.Inject

data class LiveHomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** What is on now or later today — the landing's whole subject. */
    val today: List<LiveSession> = emptyList(),
    /** What already happened, newest first; where a recording is found. */
    val past: List<LiveSession> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class LiveHomeViewModel @Inject constructor(
    private val repository: LiveRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveHomeUiState())
    val uiState: StateFlow<LiveHomeUiState> = _uiState.asStateFlow()

    init { load() }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                // Two windows, one after the other rather than in parallel: the
                // second is the smaller ask and a phone on a school's network
                // is the wrong place to open two sockets to say hello.
                val today = repository.getSessions("today")
                val past = repository.getSessions("past", limit = 12)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        today = today,
                        past = past,
                        error = null,
                    )
                }
            } catch (error: Exception) {
                Timber.w(error, "Live sessions failed to load")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message,
                    )
                }
            }
        }
    }
}
