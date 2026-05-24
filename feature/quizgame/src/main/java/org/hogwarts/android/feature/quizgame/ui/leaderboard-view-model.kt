package org.hogwarts.android.feature.quizgame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.quizgame.domain.model.LeaderboardEntry
import org.hogwarts.android.feature.quizgame.domain.usecase.GetLeaderboardUseCase
import timber.log.Timber
import javax.inject.Inject

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val entries: List<LeaderboardEntry> = emptyList(),
    val selectedPeriod: String = "weekly",
    val selectedScope: String = "school",
    val availablePeriods: List<String> = listOf("weekly", "monthly", "all-time"),
    val availableScopes: List<String> = listOf("class", "school"),
    val error: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun selectPeriod(period: String) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadLeaderboard()
    }

    fun selectScope(scope: String) {
        _uiState.update { it.copy(selectedScope = scope) }
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getLeaderboardUseCase(
                scope = _uiState.value.selectedScope,
                period = _uiState.value.selectedPeriod
            )

            result.onSuccess { entries ->
                _uiState.update { it.copy(isLoading = false, entries = entries) }
            }.onFailure { error ->
                Timber.e(error, "Failed to load leaderboard")
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load leaderboard.")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
