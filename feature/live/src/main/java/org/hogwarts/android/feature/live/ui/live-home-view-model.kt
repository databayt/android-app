package org.hogwarts.android.feature.live.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.live.data.repository.LiveRepository
import org.hogwarts.android.feature.live.domain.model.LiveLanding
import timber.log.Timber
import javax.inject.Inject

data class LiveHomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** The web's landing for this reader; null until the first load lands. */
    val landing: LiveLanding? = null,
    val error: String? = null,
)

/**
 * One read, the web's own: `/api/mobile/live/landing` returns what
 * `loadLiveLanding` hands the page, so nothing here decides which classes
 * are live, which were missed or which recordings come first.
 */
@HiltViewModel
class LiveHomeViewModel @Inject constructor(
    private val repository: LiveRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveHomeUiState())
    val uiState: StateFlow<LiveHomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init { load() }

    /** Pull to refresh: the spinner shows, a read in flight is replaced. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadJob?.cancel()
        load()
    }

    /**
     * A background read (screen resumed, every 30 s): no spinner, the old content stays until the new
     * lands, and a read already in flight is left to finish.
     */
    fun refreshQuietly() {
        if (loadJob?.isActive == true) return
        load()
    }

    private fun load() {
        loadJob = viewModelScope.launch {
            try {
                val landing = repository.getLanding()
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, landing = landing, error = null)
                }
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                Timber.w(error, "Live landing failed to load")
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = error.message)
                }
            }
        }
    }
}
