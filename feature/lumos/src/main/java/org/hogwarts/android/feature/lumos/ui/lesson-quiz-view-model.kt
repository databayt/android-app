package org.hogwarts.android.feature.lumos.ui

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
import org.hogwarts.android.feature.lumos.domain.model.QuizQuestion
import org.hogwarts.android.feature.lumos.domain.usecase.GetQuizQuestionsUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.SubmitQuizUseCase
import javax.inject.Inject

data class LessonQuizUiState(
    val isLoading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, Int> = emptyMap(),
    val isAnswerChecked: Boolean = false,
    val isFinished: Boolean = false,
    val score: Int = 0,
    val isPassed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LessonQuizViewModel @Inject constructor(
    private val getQuizQuestionsUseCase: GetQuizQuestionsUseCase,
    private val submitQuizUseCase: SubmitQuizUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val courseId: String = checkNotNull(savedStateHandle["courseId"])
    val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _uiState = MutableStateFlow(LessonQuizUiState(isLoading = true))
    val uiState: StateFlow<LessonQuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun selectOption(questionId: String, optionIndex: Int) {
        if (_uiState.value.isAnswerChecked) return
        val map = _uiState.value.selectedAnswers.toMutableMap()
        map[questionId] = optionIndex
        _uiState.update { it.copy(selectedAnswers = map) }
    }

    fun checkAnswer() {
        _uiState.update { it.copy(isAnswerChecked = true) }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex < state.questions.lastIndex) {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    isAnswerChecked = false
                )
            }
        } else {
            finishQuiz()
        }
    }

    fun previousQuestion() {
        val state = _uiState.value
        if (state.currentIndex > 0) {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex - 1,
                    isAnswerChecked = false
                )
            }
        }
    }

    fun finishQuiz() {
        viewModelScope.launch {
            val state = _uiState.value
            val res = submitQuizUseCase(courseId, lessonId, state.selectedAnswers)
            if (res is Result.Success) {
                _uiState.update {
                    it.copy(
                        isFinished = true,
                        score = res.data.first,
                        isPassed = res.data.second
                    )
                }
            } else {
                // Grade locally
                var correct = 0
                state.questions.forEach { q ->
                    if (state.selectedAnswers[q.id] == q.correctAnswer) {
                        correct++
                    }
                }
                _uiState.update {
                    it.copy(
                        isFinished = true,
                        score = correct,
                        isPassed = (correct.toFloat() / state.questions.size) >= 0.7f
                    )
                }
            }
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getQuizQuestionsUseCase(courseId, lessonId)
            if (result is Result.Success) {
                _uiState.update {
                    it.copy(isLoading = false, questions = result.data)
                }
            } else if (result is Result.Error) {
                _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }
}
