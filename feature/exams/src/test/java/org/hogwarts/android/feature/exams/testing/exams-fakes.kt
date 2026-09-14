package org.hogwarts.android.feature.exams.testing

import org.hogwarts.android.feature.exams.data.repository.ExamsApiException
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.domain.model.Child
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.domain.model.GradeRow
import org.hogwarts.android.feature.exams.domain.model.Listing
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.OwnResult
import org.hogwarts.android.feature.exams.domain.model.ProfileRecords
import org.hogwarts.android.feature.exams.domain.model.Question
import org.hogwarts.android.feature.exams.domain.model.SubmitOutcome
import org.hogwarts.android.feature.exams.domain.model.TeacherClass
import java.time.Instant

/* Invented fixtures only: this repository is public. */

fun exam(
    id: String,
    title: String,
    date: String,
    status: String = "PLANNED",
    subject: String? = "Subject A",
    start: String? = "08:00",
) = Exam(
    id = id,
    title = title,
    examDate = Instant.parse(date),
    startTime = start,
    endTime = start?.let { "%02d:%s".format(it.substring(0, 2).toInt() + 1, it.substring(3)) },
    durationMinutes = 90,
    totalMarks = 100,
    passingMarks = 50,
    examType = "MIDTERM",
    status = status,
    subject = subject,
)

fun grade(id: String, title: String, percentage: Double, graded: String, subject: String = "Subject B") = GradeRow(
    id = id,
    title = title,
    score = percentage / 5,
    maxScore = 20.0,
    percentage = percentage,
    grade = if (percentage >= 90) "A" else if (percentage >= 50) "C" else "F",
    gradedAt = Instant.parse(graded),
    subject = subject,
)

class FakeExamsRepository : ExamsRepository {
    var failAll: Exception? = null
    var examsByQuery: (status: String?, upcoming: Boolean) -> Listing<Exam> = { _, _ -> Listing(emptyList(), 0) }
    var questionBankError: Exception? = null
    var questionTotal = 0
    var students: Int? = 0
    var profile = ProfileRecords(studentId = null, teacherId = null)
    var teacherClasses = emptyList<TeacherClass>()
    var children = emptyList<Child>()
    var studentGrades = emptyList<GradeRow>()
    var childGrades = mapOf<String, List<GradeRow>>()
    var detail: Exam? = null
    var ownResult: OwnResult? = null
    var online: OnlineExam? = null
    var onlineError: Exception? = null
    val submitted = mutableListOf<Map<String, String>>()

    private fun check() = failAll?.let { throw it }

    override suspend fun exams(status: String?, upcoming: Boolean, perPage: Int): Listing<Exam> {
        check(); return examsByQuery(status, upcoming)
    }

    override suspend fun exam(id: String): Exam {
        check(); return detail ?: throw ExamsApiException(404, "Exam not found")
    }

    override suspend fun ownResult(examId: String): OwnResult? = ownResult

    override suspend fun questionBank(perPage: Int): Listing<Question> {
        check(); questionBankError?.let { throw it }
        return Listing((0 until minOf(perPage, questionTotal)).map { Question("q-$it", "Question text $it", "MULTIPLE_CHOICE", "EASY", 1.0, "Subject A") }, questionTotal)
    }

    override suspend fun totalStudents(): Int {
        check(); return students ?: throw ExamsApiException(403, "Forbidden")
    }

    override suspend fun teacherClasses(): List<TeacherClass> {
        check(); return teacherClasses
    }

    override suspend fun profile(): ProfileRecords {
        check(); return profile
    }

    override suspend fun children(): List<Child> {
        check(); return children
    }

    override suspend fun studentGrades(studentId: String, perPage: Int): List<GradeRow> {
        check(); return studentGrades
    }

    override suspend fun childGrades(childId: String, perPage: Int): List<GradeRow> {
        check(); return childGrades[childId].orEmpty()
    }

    override suspend fun startOnlineExam(examId: String): OnlineExam {
        onlineError?.let { throw it }
        return online ?: throw ExamsApiException(404, "Exam not found")
    }

    override suspend fun submitAnswers(examId: String, sessionId: String, answers: Map<String, String>): SubmitOutcome {
        submitted += answers
        return SubmitOutcome(answers.size, online?.questions?.size ?: answers.size)
    }
}
