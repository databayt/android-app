package org.hogwarts.android.feature.exams.ui

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
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.data.remote.dto.OnlineExamQuestionDto
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.OnlineExamStatus
import org.hogwarts.android.feature.exams.domain.model.ViolationType
import org.hogwarts.android.feature.exams.domain.usecase.StartOnlineExamUseCase
import org.hogwarts.android.feature.exams.domain.usecase.SubmitExamAnswersUseCase
import org.hogwarts.android.feature.exams.domain.usecase.SubmitExamViolationUseCase
import javax.inject.Inject

data class OnlineExamUiState(
    val isLoading: Boolean = true,
    val exam: OnlineExam? = null,
    val questions: List<OnlineExamQuestionDto> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String?> = emptyMap(),
    val remainingSeconds: Long = 0L,
    val violationCount: Int = 0,
    val maxViolations: Int = 3,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val isExamTerminated: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val showViolationWarning: Boolean = false,
    val lastViolationType: ViolationType? = null,
    val error: String? = null
) {
    val currentQuestion: OnlineExamQuestionDto? get() = questions.getOrNull(currentQuestionIndex)
    val totalQuestions: Int get() = questions.size
    val isLastQuestion: Boolean get() = currentQuestionIndex >= totalQuestions - 1
    val isFirstQuestion: Boolean get() = currentQuestionIndex == 0
    val progress: Float get() = if (totalQuestions > 0) (currentQuestionIndex + 1f) / totalQuestions else 0f
    val answeredCount: Int get() = answers.count { it.value != null }
    val timerMinutes: Long get() = remainingSeconds / 60
    val timerSeconds: Long get() = remainingSeconds % 60
    val timerFormatted: String get() = String.format("%02d:%02d", timerMinutes, timerSeconds)
    val isTimerCritical: Boolean get() = remainingSeconds in 1..300 // last 5 minutes
}

@HiltViewModel
class OnlineExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startOnlineExamUseCase: StartOnlineExamUseCase,
    private val submitExamAnswersUseCase: SubmitExamAnswersUseCase,
    private val submitExamViolationUseCase: SubmitExamViolationUseCase
) : ViewModel() {

    private val examId: String = savedStateHandle["examId"] ?: ""

    private val _uiState = MutableStateFlow(OnlineExamUiState())
    val uiState: StateFlow<OnlineExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadOnlineExam()
    }

    private fun loadOnlineExam() {
        if (examId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid exam ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = startOnlineExamUseCase(examId)) {
                is Resource.Success -> {
                    val exam = result.data!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            exam = exam,
                            remainingSeconds = (exam.duration * 60).toLong(),
                            error = null
                        )
                    }
                    startTimer()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error?.message ?: "Failed to load exam"
                        )
                    }
                }
                is Resource.Loading -> { /* handled above */ }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isSubmitted) {
                delay(1000L)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            // Auto-submit when time runs out
            if (!_uiState.value.isSubmitted && _uiState.value.remainingSeconds <= 0) {
                submitExam()
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String?) {
        _uiState.update { state ->
            state.copy(answers = state.answers + (questionId to answer))
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            if (!state.isLastQuestion) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
            } else state
        }
    }

    fun previousQuestion() {
        _uiState.update { state ->
            if (!state.isFirstQuestion) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex - 1)
            } else state
        }
    }

    fun jumpToQuestion(index: Int) {
        _uiState.update { state ->
            if (index in 0 until state.totalQuestions) {
                state.copy(currentQuestionIndex = index)
            } else state
        }
    }

    fun showSubmitConfirmation() {
        _uiState.update { it.copy(showSubmitDialog = true) }
    }

    fun dismissSubmitDialog() {
        _uiState.update { it.copy(showSubmitDialog = false) }
    }

    fun dismissViolationWarning() {
        _uiState.update { it.copy(showViolationWarning = false) }
    }

    fun submitExam() {
        val state = _uiState.value
        if (state.isSubmitting || state.isSubmitted) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, showSubmitDialog = false) }
            when (val result = submitExamAnswersUseCase(examId, state.answers)) {
                is Resource.Success -> {
                    timerJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSubmitted = true,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.error?.message ?: "Failed to submit exam"
                        )
                    }
                }
                is Resource.Loading -> { /* handled above */ }
            }
        }
    }

    fun reportViolation(type: ViolationType, description: String) {
        viewModelScope.launch {
            when (val result = submitExamViolationUseCase(examId, type, description)) {
                is Resource.Success -> {
                    val data = result.data!!
                    _uiState.update {
                        it.copy(
                            violationCount = data.violationCount,
                            maxViolations = data.maxViolations,
                            isExamTerminated = data.examTerminated,
                            showViolationWarning = true,
                            lastViolationType = type
                        )
                    }
                    if (data.examTerminated) {
                        timerJob?.cancel()
                        _uiState.update { it.copy(isSubmitted = true) }
                    }
                }
                is Resource.Error -> {
                    // Still increment locally even if report fails
                    _uiState.update { state ->
                        val newCount = state.violationCount + 1
                        state.copy(
                            violationCount = newCount,
                            showViolationWarning = true,
                            lastViolationType = type,
                            isExamTerminated = newCount >= state.maxViolations
                        )
                    }
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
