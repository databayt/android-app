package org.hogwarts.android.feature.exams.domain.model

import java.time.Instant

/** A school exam as `/api/mobile/exams` describes it. [status] and [examType] are the server enums. */
data class Exam(
    val id: String,
    val title: String,
    val examDate: Instant?,
    val startTime: String?,
    val endTime: String?,
    val durationMinutes: Int?,
    val totalMarks: Int?,
    val passingMarks: Int?,
    val examType: String?,
    val status: String?,
    val subject: String?,
    val description: String? = null,
    val instructions: String? = null,
) {
    /** Web: `status: { in: ["PLANNED", "IN_PROGRESS"] }`. */
    val isOpen: Boolean get() = status == "PLANNED" || status == "IN_PROGRESS"
}

/** A page of a listing with the server's total. */
data class Listing<T>(val items: List<T>, val total: Int)

/** The signed-in student's result for one exam. */
data class OwnResult(
    val score: Double?,
    val maxScore: Double?,
    val percentage: Double?,
    val grade: String?,
)

/** One graded row from the grades routes (an exam or an assignment result). */
data class GradeRow(
    val id: String,
    val title: String,
    val score: Double?,
    val maxScore: Double?,
    val percentage: Double?,
    val grade: String?,
    val gradedAt: Instant?,
    val subject: String?,
)

data class Child(val id: String, val name: String)

data class TeacherClass(val sectionId: String, val studentCount: Int)

/** Which records the account carries — `/api/mobile/profile`. */
data class ProfileRecords(val studentId: String?, val teacherId: String?)

data class Question(
    val id: String,
    val text: String,
    val type: String?,
    val difficulty: String?,
    val points: Double?,
    val subject: String?,
)

/** An open exam session and its questions — `/api/mobile/exams/:id/online`. */
data class OnlineExam(
    val sessionId: String,
    val examId: String,
    val title: String,
    val durationMinutes: Int?,
    val totalMarks: Int?,
    val instructions: String?,
    val secondsRemaining: Int,
    val questions: List<OnlineQuestion>,
)

data class OnlineQuestion(
    val id: String,
    val text: String,
    val type: String?,
    val options: List<String>,
    val marks: Double?,
)

data class SubmitOutcome(val answered: Int, val total: Int)
