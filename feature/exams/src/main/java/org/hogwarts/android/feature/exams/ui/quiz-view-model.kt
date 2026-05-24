package org.hogwarts.android.feature.exams.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.hogwarts.android.feature.exams.domain.model.Quiz
import org.hogwarts.android.feature.exams.domain.model.QuizAttempt
import org.hogwarts.android.feature.exams.domain.model.QuizQuestion
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        // Load sample quiz for offline demo
        loadSampleQuiz()
    }

    private fun loadSampleQuiz() {
        val quiz = Quiz(
            id = "sample-quiz-1",
            title = "Quick Assessment",
            subjectName = "General Knowledge",
            timeLimitMinutes = 10,
            totalMarks = 5,
            questions = listOf(
                QuizQuestion("q1", "What is the capital of Saudi Arabia?", listOf("Jeddah", "Riyadh", "Mecca", "Medina"), 1),
                QuizQuestion("q2", "Which planet is closest to the Sun?", listOf("Venus", "Earth", "Mercury", "Mars"), 2),
                QuizQuestion("q3", "What is 15 x 12?", listOf("160", "170", "180", "190"), 2),
                QuizQuestion("q4", "Who wrote 'Romeo and Juliet'?", listOf("Dickens", "Shakespeare", "Austen", "Twain"), 1),
                QuizQuestion("q5", "What is H2O commonly known as?", listOf("Salt", "Sugar", "Water", "Acid"), 2),
            )
        )
        _uiState.update {
            it.copy(
                quiz = quiz,
                attempt = QuizAttempt(quizId = quiz.id, totalMarks = quiz.totalMarks)
            )
        }
    }

    fun selectAnswer(questionId: String, optionIndex: Int) {
        _uiState.update { state ->
            val currentAttempt = state.attempt ?: return
            state.copy(
                attempt = currentAttempt.copy(
                    answers = currentAttempt.answers + (questionId to optionIndex)
                )
            )
        }
    }

    fun nextQuestion() {
        _uiState.update {
            if (!it.isLastQuestion) it.copy(currentQuestionIndex = it.currentQuestionIndex + 1) else it
        }
    }

    fun previousQuestion() {
        _uiState.update {
            if (!it.isFirstQuestion) it.copy(currentQuestionIndex = it.currentQuestionIndex - 1) else it
        }
    }

    fun submitQuiz() {
        _uiState.update { state ->
            val quiz = state.quiz ?: return
            val attempt = state.attempt ?: return
            var score = 0
            quiz.questions.forEach { question ->
                val selectedIndex = attempt.answers[question.id]
                if (selectedIndex == question.correctOptionIndex) {
                    score += question.marks
                }
            }
            state.copy(
                attempt = attempt.copy(score = score, isSubmitted = true),
                isSubmitted = true
            )
        }
    }
}
