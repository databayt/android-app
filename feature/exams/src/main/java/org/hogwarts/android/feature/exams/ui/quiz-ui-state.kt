package org.hogwarts.android.feature.exams.ui

import org.hogwarts.android.feature.exams.domain.model.Quiz
import org.hogwarts.android.feature.exams.domain.model.QuizAttempt

data class QuizUiState(
    val quiz: Quiz? = null,
    val attempt: QuizAttempt? = null,
    val currentQuestionIndex: Int = 0,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
) {
    val currentQuestion get() = quiz?.questions?.getOrNull(currentQuestionIndex)
    val totalQuestions get() = quiz?.questions?.size ?: 0
    val selectedAnswer get() = attempt?.answers?.get(currentQuestion?.id)
    val isLastQuestion get() = currentQuestionIndex >= totalQuestions - 1
    val isFirstQuestion get() = currentQuestionIndex == 0
    val progress get() = if (totalQuestions > 0) (currentQuestionIndex + 1f) / totalQuestions else 0f
}
