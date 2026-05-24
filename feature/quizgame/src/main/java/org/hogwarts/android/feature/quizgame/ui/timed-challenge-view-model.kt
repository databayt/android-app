package org.hogwarts.android.feature.quizgame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.quizgame.domain.model.DifficultyLevel
import org.hogwarts.android.feature.quizgame.domain.model.QuizMode
import org.hogwarts.android.feature.quizgame.domain.usecase.StartQuizSessionUseCase
import timber.log.Timber
import javax.inject.Inject

data class TimedChallengeUiState(
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val selectedTimeLimit: Int = 60,
    val availableTimeLimits: List<Int> = listOf(30, 60, 90, 120),
    val isStarting: Boolean = false,
    val streakMultiplier: Float = 1.0f,
    val speedBonus: Int = 0,
    val animatedScore: Int = 0,
    val error: String? = null
)

@HiltViewModel
class TimedChallengeViewModel @Inject constructor(
    private val startQuizSessionUseCase: StartQuizSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimedChallengeUiState())
    val uiState: StateFlow<TimedChallengeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun selectDifficulty(difficulty: DifficultyLevel) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun selectTimeLimit(seconds: Int) {
        _uiState.update { it.copy(selectedTimeLimit = seconds) }
    }

    fun startTimedChallenge(onSessionStarted: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isStarting = true, error = null) }

            val result = startQuizSessionUseCase(
                mode = QuizMode.TIMED,
                difficulty = _uiState.value.selectedDifficulty,
                questionCount = 15
            )

            result.onSuccess { session ->
                _uiState.update { it.copy(isStarting = false) }
                onSessionStarted(session.id)
            }.onFailure { error ->
                Timber.e(error, "Failed to start timed challenge")
                _uiState.update {
                    it.copy(isStarting = false, error = "Failed to start timed challenge. Please try again.")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
