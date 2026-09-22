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

    init { load() }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val landing = repository.getLanding()
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, landing = landing, error = null)
                }
            } catch (error: Exception) {
                Timber.w(error, "Live landing failed to load")
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = error.message)
                }
            }
        }
    }
}
