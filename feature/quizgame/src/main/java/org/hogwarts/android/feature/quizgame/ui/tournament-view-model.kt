package org.hogwarts.android.feature.quizgame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.quizgame.domain.model.Tournament
import org.hogwarts.android.feature.quizgame.domain.usecase.GetTournamentsUseCase
import org.hogwarts.android.feature.quizgame.domain.usecase.JoinTournamentUseCase
import timber.log.Timber
import javax.inject.Inject

data class TournamentUiState(
    val isLoading: Boolean = true,
    val tournaments: List<Tournament> = emptyList(),
    val joiningTournamentId: String? = null,
    val error: String? = null
)

@HiltViewModel
class TournamentViewModel @Inject constructor(
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val joinTournamentUseCase: JoinTournamentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TournamentUiState())
    val uiState: StateFlow<TournamentUiState> = _uiState.asStateFlow()

    init {
        loadTournaments()
    }

    fun loadTournaments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getTournamentsUseCase()

            result.onSuccess { tournaments ->
                _uiState.update { it.copy(isLoading = false, tournaments = tournaments) }
            }.onFailure { error ->
                Timber.e(error, "Failed to load tournaments")
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load tournaments.")
                }
            }
        }
    }

    fun joinTournament(tournamentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(joiningTournamentId = tournamentId, error = null) }

            val result = joinTournamentUseCase(tournamentId)

            result.onSuccess { updatedTournament ->
                _uiState.update { state ->
                    state.copy(
                        joiningTournamentId = null,
                        tournaments = state.tournaments.map {
                            if (it.id == tournamentId) updatedTournament else it
                        }
                    )
                }
            }.onFailure { error ->
                Timber.e(error, "Failed to join tournament")
                _uiState.update {
                    it.copy(joiningTournamentId = null, error = "Failed to join tournament.")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
