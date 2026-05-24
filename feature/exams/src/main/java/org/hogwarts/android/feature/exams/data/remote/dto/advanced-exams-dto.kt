package org.hogwarts.android.feature.exams.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.exams.domain.model.DetailedExamResult
import org.hogwarts.android.feature.exams.domain.model.Difficulty
import org.hogwarts.android.feature.exams.domain.model.ExamAnswer
import org.hogwarts.android.feature.exams.domain.model.ExamCertificate
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.OnlineExamStatus
import org.hogwarts.android.feature.exams.domain.model.QuestionBankItem
import org.hogwarts.android.feature.exams.domain.model.QuestionType
import java.time.Instant

// --- Online Exam ---

@Serializable
data class OnlineExamDto(
    val id: String,
    @SerialName("exam_id") val examId: String,
    val title: String,
    val subject: String,
    val duration: Int,
    @SerialName("question_count") val questionCount: Int,
    val instructions: List<String>,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val status: String,
    val questions: List<OnlineExamQuestionDto>? = null
)

@Serializable
data class OnlineExamQuestionDto(
    val id: String,
    val question: String,
    val type: String,
    val options: List<String>? = null,
    val marks: Float = 1f,
    @SerialName("question_number") val questionNumber: Int
)

fun OnlineExamDto.toDomain() = OnlineExam(
    id = id,
    examId = examId,
    title = title,
    subject = subject,
    duration = duration,
    questionCount = questionCount,
    instructions = instructions,
    startTime = Instant.parse(startTime),
    endTime = Instant.parse(endTime),
    status = try { OnlineExamStatus.valueOf(status) } catch (_: Exception) { OnlineExamStatus.NOT_STARTED }
)

// --- Submit Answers ---

@Serializable
data class SubmitAnswersRequest(
    @SerialName("exam_id") val examId: String,
    val answers: List<SubmitAnswerItemDto>
)

@Serializable
data class SubmitAnswerItemDto(
    @SerialName("question_id") val questionId: String,
    val answer: String?
)

@Serializable
data class SubmitAnswersResponse(
    val success: Boolean,
    val message: String? = null,
    @SerialName("submitted_at") val submittedAt: String? = null
)

// --- Violations ---

@Serializable
data class ViolationReportRequest(
    @SerialName("exam_id") val examId: String,
    val type: String,
    val description: String,
    val timestamp: String
)

@Serializable
data class ViolationReportResponse(
    val success: Boolean,
    @SerialName("violation_count") val violationCount: Int = 0,
    @SerialName("max_violations") val maxViolations: Int = 3,
    @SerialName("exam_terminated") val examTerminated: Boolean = false
)

// --- Question Bank ---

@Serializable
data class QuestionBankItemDto(
    val id: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val type: String,
    val question: String,
    val options: List<String>? = null,
    @SerialName("correct_answer") val correctAnswer: String,
    val explanation: String? = null,
    @SerialName("is_bookmarked") val isBookmarked: Boolean = false
)

@Serializable
data class QuestionBankListResponse(
    val data: List<QuestionBankItemDto>
)

fun QuestionBankItemDto.toDomain() = QuestionBankItem(
    id = id,
    subject = subject,
    topic = topic,
    difficulty = try { Difficulty.valueOf(difficulty.uppercase()) } catch (_: Exception) { Difficulty.MEDIUM },
    type = try { QuestionType.valueOf(type.uppercase()) } catch (_: Exception) { QuestionType.MCQ },
    question = question,
    options = options,
    correctAnswer = correctAnswer,
    explanation = explanation,
    isBookmarked = isBookmarked
)

@Serializable
data class GenerateQuestionsRequest(
    val subject: String,
    val topic: String? = null,
    val difficulty: String? = null,
    val count: Int = 10
)

@Serializable
data class GenerateQuestionsResponse(
    val data: List<QuestionBankItemDto>,
    val message: String? = null
)

// --- Bookmark ---

@Serializable
data class BookmarkResponse(
    val success: Boolean,
    @SerialName("is_bookmarked") val isBookmarked: Boolean
)

// --- Exam Results ---

@Serializable
data class DetailedExamResultDto(
    val id: String,
    @SerialName("exam_id") val examId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("total_marks") val totalMarks: Float,
    @SerialName("obtained_marks") val obtainedMarks: Float,
    val percentage: Float,
    val grade: String,
    val rank: Int? = null,
    @SerialName("total_students") val totalStudents: Int? = null,
    val answers: List<ExamAnswerDto>,
    @SerialName("submitted_at") val submittedAt: String
)

@Serializable
data class ExamAnswerDto(
    val id: String,
    @SerialName("exam_id") val examId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("student_answer") val studentAnswer: String? = null,
    @SerialName("is_correct") val isCorrect: Boolean? = null,
    @SerialName("marks_obtained") val marksObtained: Float? = null,
    @SerialName("question_text") val questionText: String? = null,
    @SerialName("correct_answer") val correctAnswer: String? = null,
    val explanation: String? = null
)

fun ExamAnswerDto.toDomain() = ExamAnswer(
    id = id,
    examId = examId,
    questionId = questionId,
    studentAnswer = studentAnswer,
    isCorrect = isCorrect,
    marksObtained = marksObtained
)

fun DetailedExamResultDto.toDomain() = DetailedExamResult(
    id = id,
    examId = examId,
    studentId = studentId,
    totalMarks = totalMarks,
    obtainedMarks = obtainedMarks,
    percentage = percentage,
    grade = grade,
    rank = rank,
    totalStudents = totalStudents,
    answers = answers.map { it.toDomain() },
    submittedAt = Instant.parse(submittedAt)
)

// --- Exam Certificate ---

@Serializable
data class ExamCertificateDto(
    val id: String,
    @SerialName("exam_id") val examId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("exam_title") val examTitle: String,
    val subject: String,
    val score: Float,
    val grade: String,
    @SerialName("completed_at") val completedAt: String,
    @SerialName("certificate_url") val certificateUrl: String,
    @SerialName("verification_code") val verificationCode: String
)

fun ExamCertificateDto.toDomain() = ExamCertificate(
    id = id,
    examId = examId,
    studentName = studentName,
    examTitle = examTitle,
    subject = subject,
    score = score,
    grade = grade,
    completedAt = Instant.parse(completedAt),
    certificateUrl = certificateUrl,
    verificationCode = verificationCode
)
