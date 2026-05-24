package org.hogwarts.android.feature.stream.ui

import androidx.lifecycle.SavedStateHandle
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
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.stream.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.stream.domain.model.QuizQuestion
import org.hogwarts.android.feature.stream.domain.usecase.GetQuizQuestionsUseCase
import org.hogwarts.android.feature.stream.domain.usecase.UpdateLessonProgressUseCase
import javax.inject.Inject

data class LessonQuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val answers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOption
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isFinished: Boolean = false,
    val showExplanation: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val timeRemainingSeconds: Int = 0,
    val error: String? = null
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val isLastQuestion: Boolean
        get() = currentIndex >= questions.size - 1

    val scorePercentage: Int
        get() = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
}

@HiltViewModel
class LessonQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuizQuestionsUseCase: GetQuizQuestionsUseCase,
    private val updateLessonProgressUseCase: UpdateLessonProgressUseCase
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""
    val lessonId: String = savedStateHandle["lessonId"] ?: ""

    private val _uiState = MutableStateFlow(LessonQuizUiState())
    val uiState: StateFlow<LessonQuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadQuiz()
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getQuizQuestionsUseCase(courseId, lessonId)) {
                is Result.Success -> {
                    val questions = result.data
                    val timePerQuestion = 60 // 60 seconds per question
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questions = questions,
                            totalQuestions = questions.size,
                            timeRemainingSeconds = questions.size * timePerQuestion
                        )
                    }
                    startTimer()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0 && !_uiState.value.isFinished) {
                delay(1000)
                _uiState.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
            }
            if (!_uiState.value.isFinished && _uiState.value.timeRemainingSeconds <= 0) {
                submitQuiz()
            }
        }
    }

    fun selectAnswer(optionIndex: Int) {
        _uiState.update { state ->
            state.copy(
                selectedAnswer = optionIndex,
                answers = state.answers + (state.currentIndex to optionIndex)
            )
        }
    }

    fun confirmAnswer() {
        _uiState.update { it.copy(showExplanation = true) }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.isLastQuestion) {
            submitQuiz()
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedAnswer = it.answers[it.currentIndex + 1],
                    showExplanation = false
                )
            }
        }
    }

    fun previousQuestion() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(
                    currentIndex = state.currentIndex - 1,
                    selectedAnswer = state.answers[state.currentIndex - 1],
                    showExplanation = false
                )
            } else {
                state
            }
        }
    }

    private fun submitQuiz() {
        timerJob?.cancel()
        val state = _uiState.value
        var correct = 0
        state.questions.forEachIndexed { index, question ->
            if (state.answers[index] == question.correctAnswer) {
                correct++
            }
        }
        _uiState.update {
            it.copy(isFinished = true, score = correct, showExplanation = false)
        }

        // Submit score to backend
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val scorePercent = if (state.totalQuestions > 0) (correct * 100) / state.totalQuestions else 0
            updateLessonProgressUseCase(
                courseId = courseId,
                lessonId = lessonId,
                status = LessonProgressStatus.COMPLETED,
                score = scorePercent
            )
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
