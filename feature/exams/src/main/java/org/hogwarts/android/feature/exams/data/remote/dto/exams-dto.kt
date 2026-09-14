package org.hogwarts.android.feature.exams.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Wire shapes of the hogwarts mobile routes this module reads. Every field is
 * snake_case exactly as the `src/app/api/mobile` routes writes it (checked against
 * production); anything the server may omit or null is nullable here.
 */

/** One row of `GET /api/mobile/exams` and the body of `GET /api/mobile/exams/:id`. */
@Serializable
data class ExamDto(
    val id: String,
    val title: String = "",
    val description: String? = null,
    @SerialName("exam_date") val examDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val duration: Int? = null,
    @SerialName("total_marks") val totalMarks: Int? = null,
    @SerialName("passing_marks") val passingMarks: Int? = null,
    @SerialName("exam_type") val examType: String? = null,
    val status: String? = null,
    val instructions: String? = null,
    @SerialName("subject_name") val subjectName: String? = null,
    @SerialName("max_attempts") val maxAttempts: Int? = null,
)

@Serializable
data class ExamListResponse(
    val data: List<ExamDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 30,
)

/** `GET /api/mobile/exams/question-bank` — TEACHER, ADMIN, DEVELOPER only. */
@Serializable
data class QuestionDto(
    val id: String,
    val text: String = "",
    val type: String? = null,
    val difficulty: String? = null,
    @SerialName("bloom_level") val bloomLevel: String? = null,
    val points: Double? = null,
    @SerialName("subject_name") val subjectName: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class QuestionListResponse(
    val data: List<QuestionDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 30,
)

/** `GET /api/mobile/exams/:id/results` — the signed-in student's own result. */
@Serializable
data class ExamResultDto(
    @SerialName("exam_title") val examTitle: String? = null,
    val score: Double? = null,
    @SerialName("max_score") val maxScore: Double? = null,
    val percentage: Double? = null,
    val grade: String? = null,
    val rank: Int? = null,
)

/** `GET /api/mobile/exams/:id/online` — opens (or resumes) the student's session. */
@Serializable
data class OnlineExamDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("exam_id") val examId: String,
    val title: String = "",
    val duration: Int? = null,
    @SerialName("total_marks") val totalMarks: Int? = null,
    val instructions: String? = null,
    @SerialName("time_remaining") val timeRemaining: Int = 0,
    val questions: List<OnlineQuestionDto> = emptyList(),
)

@Serializable
data class OnlineQuestionDto(
    val id: String,
    val text: String = "",
    val type: String? = null,
    val options: List<OnlineOptionDto>? = null,
    val marks: Double? = null,
    val order: Int? = null,
)

@Serializable
data class OnlineOptionDto(val text: String = "")

/** `POST /api/mobile/exams/:id/answers`. */
@Serializable
data class SubmitAnswersRequest(
    @SerialName("session_id") val sessionId: String,
    val answers: List<SubmitAnswerDto>,
)

@Serializable
data class SubmitAnswerDto(
    @SerialName("question_id") val questionId: String,
    val answer: String,
)

@Serializable
data class SubmitAnswersResponse(
    val submitted: Boolean = false,
    @SerialName("total_questions") val totalQuestions: Int? = null,
    @SerialName("answered_count") val answeredCount: Int? = null,
)

/** The error body every mobile route sends with a 4xx/5xx. */
@Serializable
data class ErrorDto(val error: String? = null)

/** `GET /api/mobile/admin/stats` — only the figure the exams page shows. */
@Serializable
data class AdminStatsDto(@SerialName("total_students") val totalStudents: Int? = null)

/** `GET /api/mobile/teacher/classes`. */
@Serializable
data class TeacherClassesResponse(val data: List<TeacherClassDto> = emptyList())

@Serializable
data class TeacherClassDto(
    @SerialName("section_id") val sectionId: String,
    @SerialName("student_count") val studentCount: Int = 0,
)

/** `GET /api/mobile/profile` — whether the account has a student / teacher record. */
@Serializable
data class ProfileDto(
    val student: ProfileRecordDto? = null,
    val teacher: ProfileRecordDto? = null,
)

@Serializable
data class ProfileRecordDto(val id: String)

/** `GET /api/mobile/guardian/children`. */
@Serializable
data class ChildrenResponse(val data: List<ChildDto> = emptyList())

@Serializable
data class ChildDto(
    val id: String,
    @SerialName("given_name") val givenName: String? = null,
    @SerialName("family_name") val familyName: String? = null,
)

/** `GET /api/mobile/grades/student/:id` and `/guardian/children/:id/grades`. */
@Serializable
data class GradesResponse(
    val data: List<GradeDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
data class GradeDto(
    val id: String,
    val title: String? = null,
    val score: Double? = null,
    @SerialName("max_score") val maxScore: Double? = null,
    val percentage: Double? = null,
    val grade: String? = null,
    @SerialName("graded_at") val gradedAt: String? = null,
    @SerialName("subject_name") val subjectName: String? = null,
)
