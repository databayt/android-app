package org.hogwarts.android.feature.exams.domain.model

import java.time.Instant

data class OnlineExam(
    val id: String,
    val examId: String,
    val title: String,
    val subject: String,
    val duration: Int,
    val questionCount: Int,
    val instructions: List<String>,
    val startTime: Instant,
    val endTime: Instant,
    val status: OnlineExamStatus
)

enum class OnlineExamStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    GRADED,
    EXPIRED
}

data class ExamAnswer(
    val id: String,
    val examId: String,
    val questionId: String,
    val studentAnswer: String?,
    val isCorrect: Boolean?,
    val marksObtained: Float?
)

data class ExamViolation(
    val id: String,
    val examId: String,
    val type: ViolationType,
    val timestamp: Instant,
    val description: String
)

enum class ViolationType {
    APP_SWITCH,
    SCREENSHOT_ATTEMPT,
    MULTIPLE_FACE,
    NO_FACE,
    TAB_CHANGE
}

data class QuestionBankItem(
    val id: String,
    val subject: String,
    val topic: String,
    val difficulty: Difficulty,
    val type: QuestionType,
    val question: String,
    val options: List<String>?,
    val correctAnswer: String,
    val explanation: String?,
    val isBookmarked: Boolean = false
)

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class QuestionType {
    MCQ,
    TRUE_FALSE,
    SHORT_ANSWER,
    FILL_BLANK,
    MATCHING
}

data class DetailedExamResult(
    val id: String,
    val examId: String,
    val studentId: String,
    val totalMarks: Float,
    val obtainedMarks: Float,
    val percentage: Float,
    val grade: String,
    val rank: Int?,
    val totalStudents: Int?,
    val answers: List<ExamAnswer>,
    val submittedAt: Instant
)

data class ExamCertificate(
    val id: String,
    val examId: String,
    val studentName: String,
    val examTitle: String,
    val subject: String,
    val score: Float,
    val grade: String,
    val completedAt: Instant,
    val certificateUrl: String,
    val verificationCode: String
)
