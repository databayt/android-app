package org.hogwarts.android.feature.exams.domain.model

data class Quiz(
    val id: String,
    val title: String,
    val subjectName: String,
    val questions: List<QuizQuestion>,
    val timeLimitMinutes: Int? = null,
    val totalMarks: Int
)

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val marks: Int = 1
)

data class QuizAttempt(
    val quizId: String,
    val answers: Map<String, Int> = emptyMap(),
    val score: Int = 0,
    val totalMarks: Int = 0,
    val isSubmitted: Boolean = false,
    val startedAt: Long = System.currentTimeMillis()
)
