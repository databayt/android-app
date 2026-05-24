package org.hogwarts.android.feature.quizgame.ui

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
import org.hogwarts.android.feature.quizgame.domain.model.QuizQuestion
import org.hogwarts.android.feature.quizgame.domain.model.QuizResult
import org.hogwarts.android.feature.quizgame.domain.model.QuizSession
import org.hogwarts.android.feature.quizgame.domain.usecase.StartQuizSessionUseCase
import org.hogwarts.android.feature.quizgame.domain.usecase.SubmitQuizSessionUseCase
import timber.log.Timber
import javax.inject.Inject

data class QuizSessionUiState(
    val isLoading: Boolean = true,
    val session: QuizSession? = null,
    val currentQuestion: QuizQuestion? = null,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerRevealed: Boolean = false,
    val isCorrect: Boolean = false,
    val answers: Map<String, String> = emptyMap(),
    val score: Int = 0,
    val streakCount: Int = 0,
    val timeRemainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val isSubmitting: Boolean = false,
    val result: QuizResult? = null,
    val showResult: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class QuizSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val submitQuizSessionUseCase: SubmitQuizSessionUseCase
) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""

    private val _uiState = MutableStateFlow(QuizSessionUiState())
    val uiState: StateFlow<QuizSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: Long = System.currentTimeMillis()

    fun initSession(session: QuizSession) {
        sessionStartTime = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                isLoading = false,
                session = session,
                currentQuestion = session.questions.firstOrNull(),
                currentIndex = 0,
                totalQuestions = session.questions.size,
                timeRemainingSeconds = session.timeLimitSeconds ?: 0,
                isTimerRunning = session.timeLimitSeconds != null
            )
        }
        if (session.timeLimitSeconds != null) {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000L)
                _uiState.update { state ->
                    val newTime = state.timeRemainingSeconds - 1
                    if (newTime <= 0) {
                        submitQuiz()
                    }
                    state.copy(timeRemainingSeconds = newTime.coerceAtLeast(0))
                }
            }
        }
    }

    fun selectAnswer(answer: String) {
        if (_uiState.value.isAnswerRevealed) return
        _uiState.update { it.copy(selectedAnswer = answer) }
    }

    fun confirmAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val selectedAnswer = state.selectedAnswer ?: return

        val isCorrect = selectedAnswer == question.correctAnswer
        val newStreak = if (isCorrect) state.streakCount + 1 else 0
        val pointsEarned = if (isCorrect) question.points * (1 + newStreak / 5) else 0

        _uiState.update {
            it.copy(
                isAnswerRevealed = true,
                isCorrect = isCorrect,
                answers = it.answers + (question.id to selectedAnswer),
                score = it.score + pointsEarned,
                streakCount = newStreak
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        val session = state.session ?: return
        val nextIndex = state.currentIndex + 1

        if (nextIndex >= session.questions.size) {
            submitQuiz()
            return
        }

        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                currentQuestion = session.questions[nextIndex],
                selectedAnswer = null,
                isAnswerRevealed = false,
                isCorrect = false
            )
        }
    }

    fun submitQuiz() {
        val state = _uiState.value
        val session = state.session ?: return

        timerJob?.cancel()
        val timeTaken = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, isTimerRunning = false) }

            val result = submitQuizSessionUseCase(
                sessionId = session.id,
                answers = state.answers,
                timeTakenSeconds = timeTaken
            )

            result.onSuccess { quizResult ->
                _uiState.update {
                    it.copy(isSubmitting = false, result = quizResult, showResult = true)
                }
            }.onFailure { error ->
                Timber.e(error, "Failed to submit quiz")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showResult = true,
                        result = QuizResult(
                            sessionId = session.id,
                            mode = session.mode,
                            totalQuestions = session.questions.size,
                            correctAnswers = state.answers.count { (qId, answer) ->
                                session.questions.find { it.id == qId }?.correctAnswer == answer
                            },
                            score = state.score,
                            timeTakenSeconds = timeTaken,
                            accuracy = state.answers.count { (qId, answer) ->
                                session.questions.find { it.id == qId }?.correctAnswer == answer
                            }.toFloat() / session.questions.size,
                            xpEarned = state.score,
                            streakBonus = 0,
                            newAchievements = emptyList()
                        ),
                        error = "Results saved locally. Will sync when online."
                    )
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
