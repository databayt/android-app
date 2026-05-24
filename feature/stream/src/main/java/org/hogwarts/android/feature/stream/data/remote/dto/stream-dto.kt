package org.hogwarts.android.feature.stream.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.stream.domain.model.Chapter
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.model.CourseCertificate
import org.hogwarts.android.feature.stream.domain.model.CourseStatus
import org.hogwarts.android.feature.stream.domain.model.Enrollment
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonProgress
import org.hogwarts.android.feature.stream.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.stream.domain.model.LessonType
import org.hogwarts.android.feature.stream.domain.model.QuizQuestion

@Serializable
data class CourseDto(
    val id: String,
    @SerialName("school_id") val schoolId: String = "",
    val title: String,
    val description: String = "",
    @SerialName("instructor_name") val instructorName: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val category: String = "",
    @SerialName("enrollment_count") val enrollmentCount: Int = 0,
    @SerialName("lesson_count") val lessonCount: Int = 0,
    @SerialName("total_duration") val totalDuration: String = "",
    val status: String = "PUBLISHED",
    val progress: Float = 0f,
    val slug: String = "",
    // Backend returns a grade array — a subject can span multiple grade levels.
    // Mobile historically read a singular `grade` that the backend never sent,
    // which quietly left every course ungraded and broke the grade filter.
    val grades: List<Int> = emptyList(),
    val chapters: List<ChapterDto>? = null
) {
    fun toDomain(): Course = Course(
        id = id,
        schoolId = schoolId,
        title = title,
        description = description,
        instructorName = instructorName,
        thumbnailUrl = thumbnailUrl,
        category = category,
        enrollmentCount = enrollmentCount,
        lessonCount = lessonCount,
        totalDuration = totalDuration,
        status = runCatching { CourseStatus.valueOf(status.uppercase()) }.getOrDefault(CourseStatus.PUBLISHED),
        progress = progress,
        grades = grades
    )
}

@Serializable
data class ChapterDto(
    val id: String,
    @SerialName("course_id") val courseId: String = "",
    val title: String,
    @SerialName("order_index") val orderIndex: Int = 0,
    @SerialName("lesson_count") val lessonCount: Int = 0,
    @SerialName("completed_lessons") val completedLessons: Int = 0,
    val lessons: List<LessonDto>? = null
) {
    fun toDomain(): Chapter = Chapter(
        id = id,
        courseId = courseId,
        title = title,
        orderIndex = orderIndex,
        lessonCount = lessonCount,
        completedLessons = completedLessons,
        lessons = lessons?.map { it.toDomain() }
    )
}

@Serializable
data class LessonDto(
    val id: String,
    @SerialName("chapter_id") val chapterId: String = "",
    val title: String,
    val type: String = "VIDEO",
    val duration: String = "",
    @SerialName("content_url") val contentUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("order_index") val orderIndex: Int = 0,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("is_locked") val isLocked: Boolean = false,
    @SerialName("quiz_questions") val quizQuestions: List<QuizQuestionDto>? = null
) {
    fun toDomain(): Lesson = Lesson(
        id = id,
        chapterId = chapterId,
        title = title,
        type = runCatching { LessonType.valueOf(type.uppercase()) }.getOrDefault(LessonType.VIDEO),
        duration = duration,
        contentUrl = contentUrl,
        thumbnailUrl = thumbnailUrl,
        orderIndex = orderIndex,
        isCompleted = isCompleted,
        isLocked = isLocked
    )
}

@Serializable
data class QuizQuestionDto(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String? = null
) {
    fun toDomain(): QuizQuestion = QuizQuestion(
        id = id,
        question = question,
        options = options,
        correctAnswer = correctAnswer,
        explanation = explanation
    )
}

@Serializable
data class EnrollmentDto(
    val id: String,
    val courseId: String,
    val userId: String,
    val progress: Float = 0f,
    val startedAt: String = "",
    val lastAccessedAt: String = "",
    val completedAt: String? = null
) {
    fun toDomain(): Enrollment = Enrollment(
        id = id,
        courseId = courseId,
        userId = userId,
        progress = progress,
        startedAt = startedAt,
        lastAccessedAt = lastAccessedAt,
        completedAt = completedAt
    )
}

@Serializable
data class LessonProgressDto(
    val id: String = "",
    val lessonId: String = "",
    val enrollmentId: String = "",
    val status: String = "NOT_STARTED",
    val score: Int? = null,
    val startedAt: String? = null,
    val completedAt: String? = null
) {
    fun toDomain(): LessonProgress = LessonProgress(
        id = id,
        lessonId = lessonId,
        enrollmentId = enrollmentId,
        status = runCatching { LessonProgressStatus.valueOf(status.uppercase()) }.getOrDefault(LessonProgressStatus.NOT_STARTED),
        score = score,
        startedAt = startedAt,
        completedAt = completedAt
    )
}

@Serializable
data class CertificateDto(
    val id: String,
    val courseId: String,
    val userId: String,
    val studentName: String = "",
    val courseName: String = "",
    val completedAt: String = "",
    val certificateUrl: String? = null,
    val verificationCode: String = ""
) {
    fun toDomain(): CourseCertificate = CourseCertificate(
        id = id,
        courseId = courseId,
        userId = userId,
        studentName = studentName,
        courseName = courseName,
        completedAt = completedAt,
        certificateUrl = certificateUrl,
        verificationCode = verificationCode
    )
}
