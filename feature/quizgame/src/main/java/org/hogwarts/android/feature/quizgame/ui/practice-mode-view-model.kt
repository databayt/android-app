package org.hogwarts.android.feature.quizgame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

data class PracticeModeUiState(
    val selectedSubject: String? = null,
    val selectedTopic: String? = null,
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val questionCount: Int = 10,
    val availableSubjects: List<String> = listOf("Mathematics", "Science", "English", "History", "Geography"),
    val isStarting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PracticeModeViewModel @Inject constructor(
    private val startQuizSessionUseCase: StartQuizSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeModeUiState())
    val uiState: StateFlow<PracticeModeUiState> = _uiState.asStateFlow()

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject, selectedTopic = null) }
    }

    fun selectDifficulty(difficulty: DifficultyLevel) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun setQuestionCount(count: Int) {
        _uiState.update { it.copy(questionCount = count.coerceIn(5, 30)) }
    }

    fun startPractice(onSessionStarted: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isStarting = true, error = null) }

            val result = startQuizSessionUseCase(
                mode = QuizMode.PRACTICE,
                subject = _uiState.value.selectedSubject,
                difficulty = _uiState.value.selectedDifficulty,
                questionCount = _uiState.value.questionCount
            )

            result.onSuccess { session ->
                _uiState.update { it.copy(isStarting = false) }
                onSessionStarted(session.id)
            }.onFailure { error ->
                Timber.e(error, "Failed to start practice session")
                _uiState.update {
                    it.copy(isStarting = false, error = "Failed to start quiz. Please try again.")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
