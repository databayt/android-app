package org.hogwarts.android.feature.exams.data.repository

import kotlinx.serialization.json.Json
import org.hogwarts.android.feature.exams.data.remote.ExamsApi
import org.hogwarts.android.feature.exams.data.remote.dto.ErrorDto
import org.hogwarts.android.feature.exams.data.remote.dto.ExamDto
import org.hogwarts.android.feature.exams.data.remote.dto.GradeDto
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswerDto
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswersRequest
import org.hogwarts.android.feature.exams.domain.model.Child
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.domain.model.GradeRow
import org.hogwarts.android.feature.exams.domain.model.Listing
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.OnlineQuestion
import org.hogwarts.android.feature.exams.domain.model.OwnResult
import org.hogwarts.android.feature.exams.domain.model.ProfileRecords
import org.hogwarts.android.feature.exams.domain.model.Question
import org.hogwarts.android.feature.exams.domain.model.SubmitOutcome
import org.hogwarts.android.feature.exams.domain.model.TeacherClass
import retrofit2.Response
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** A non-2xx answer from a mobile route; [serverMessage] is its `{ error }` text. */
class ExamsApiException(val code: Int, val serverMessage: String?) : IOException("HTTP $code ${serverMessage.orEmpty()}")

interface ExamsRepository {
    suspend fun exams(status: String? = null, upcoming: Boolean = false, perPage: Int = 30): Listing<Exam>
    suspend fun exam(id: String): Exam
    /** The student's own result, or null when the server has none (404). */
    suspend fun ownResult(examId: String): OwnResult?
    suspend fun questionBank(perPage: Int = 30): Listing<Question>
    suspend fun totalStudents(): Int
    suspend fun teacherClasses(): List<TeacherClass>
    suspend fun profile(): ProfileRecords
    suspend fun children(): List<Child>
    suspend fun studentGrades(studentId: String, perPage: Int): List<GradeRow>
    suspend fun childGrades(childId: String, perPage: Int): List<GradeRow>
    suspend fun startOnlineExam(examId: String): OnlineExam
    suspend fun submitAnswers(examId: String, sessionId: String, answers: Map<String, String>): SubmitOutcome
}

@Singleton
class ExamsRepositoryImpl @Inject constructor(
    private val api: ExamsApi,
    private val json: Json,
) : ExamsRepository {

    override suspend fun exams(status: String?, upcoming: Boolean, perPage: Int): Listing<Exam> =
        api.getExams(status = status, upcoming = upcoming.takeIf { it }, perPage = perPage).bodyOrThrow()
            .let { page -> Listing(page.data.map { it.toDomain() }, page.total) }

    override suspend fun exam(id: String): Exam = api.getExam(id).bodyOrThrow().toDomain()

    override suspend fun ownResult(examId: String): OwnResult? {
        val response = api.getExamResult(examId)
        if (response.code() == 404) return null
        val dto = response.bodyOrThrow()
        return OwnResult(dto.score, dto.maxScore, dto.percentage, dto.grade)
    }

    override suspend fun questionBank(perPage: Int): Listing<Question> =
        api.getQuestionBank(perPage = perPage).bodyOrThrow().let { page ->
            Listing(page.data.map { Question(it.id, it.text, it.type, it.difficulty, it.points, it.subjectName) }, page.total)
        }

    override suspend fun totalStudents(): Int = api.getAdminStats().bodyOrThrow().totalStudents ?: 0

    override suspend fun teacherClasses(): List<TeacherClass> =
        api.getTeacherClasses().bodyOrThrow().data.map { TeacherClass(it.sectionId, it.studentCount) }

    override suspend fun profile(): ProfileRecords =
        api.getProfile().bodyOrThrow().let { ProfileRecords(it.student?.id, it.teacher?.id) }

    override suspend fun children(): List<Child> =
        api.getChildren().bodyOrThrow().data.map { child ->
            Child(child.id, listOfNotNull(child.givenName, child.familyName).filter { it.isNotBlank() }.joinToString(" "))
        }

    override suspend fun studentGrades(studentId: String, perPage: Int): List<GradeRow> =
        api.getStudentGrades(studentId, perPage).bodyOrThrow().data.map { it.toDomain() }

    override suspend fun childGrades(childId: String, perPage: Int): List<GradeRow> =
        api.getChildGrades(childId, perPage).bodyOrThrow().data.map { it.toDomain() }

    override suspend fun startOnlineExam(examId: String): OnlineExam =
        api.startOnlineExam(examId).bodyOrThrow().let { dto ->
            OnlineExam(
                sessionId = dto.sessionId,
                examId = dto.examId,
                title = dto.title,
                durationMinutes = dto.duration,
                totalMarks = dto.totalMarks,
                instructions = dto.instructions,
                secondsRemaining = dto.timeRemaining,
                questions = dto.questions.map { q ->
                    OnlineQuestion(q.id, q.text, q.type, q.options.orEmpty().map { it.text }, q.marks)
                },
            )
        }

    override suspend fun submitAnswers(examId: String, sessionId: String, answers: Map<String, String>): SubmitOutcome {
        val body = SubmitAnswersRequest(sessionId, answers.map { (id, answer) -> SubmitAnswerDto(id, answer) })
        val dto = api.submitAnswers(examId, body).bodyOrThrow()
        return SubmitOutcome(answered = dto.answeredCount ?: answers.size, total = dto.totalQuestions ?: answers.size)
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        val body = body()
        if (isSuccessful && body != null) return body
        val message = runCatching { errorBody()?.string()?.let { json.decodeFromString<ErrorDto>(it).error } }.getOrNull()
        throw ExamsApiException(code(), message)
    }
}

private fun String?.toInstant(): Instant? = this?.let { runCatching { Instant.parse(it) }.getOrNull() }

internal fun ExamDto.toDomain() = Exam(
    id = id,
    title = title,
    examDate = examDate.toInstant(),
    startTime = startTime,
    endTime = endTime,
    durationMinutes = duration,
    totalMarks = totalMarks,
    passingMarks = passingMarks,
    examType = examType,
    status = status,
    subject = subjectName,
    description = description,
    instructions = instructions,
)

internal fun GradeDto.toDomain() = GradeRow(
    id = id,
    title = title.orEmpty(),
    score = score,
    maxScore = maxScore,
    percentage = percentage,
    grade = grade,
    gradedAt = gradedAt.toInstant(),
    subject = subjectName,
)
