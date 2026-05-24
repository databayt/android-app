package org.hogwarts.android.feature.stream.domain.model

/**
 * Course status reflecting publishing lifecycle.
 */
enum class CourseStatus {
    DRAFT, PUBLISHED, ARCHIVED
}

/**
 * Lesson content type.
 */
enum class LessonType {
    VIDEO, TEXT, QUIZ
}

/**
 * Progress status for individual lessons.
 */
enum class LessonProgressStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED
}

/**
 * Domain model for a course in the LMS.
 */
data class Course(
    val id: String,
    val schoolId: String,
    val title: String,
    val description: String,
    val instructorName: String,
    val thumbnailUrl: String? = null,
    val category: String,
    val enrollmentCount: Int = 0,
    val lessonCount: Int = 0,
    val totalDuration: String = "",
    val status: CourseStatus = CourseStatus.PUBLISHED,
    val progress: Float = 0f,
    val grades: List<Int> = emptyList()
)

/**
 * A chapter groups lessons within a course.
 * [lessons] may be null when only the chapter metadata is loaded.
 */
data class Chapter(
    val id: String,
    val courseId: String,
    val title: String,
    val orderIndex: Int,
    val lessonCount: Int = 0,
    val completedLessons: Int = 0,
    val lessons: List<Lesson>? = null
)

/**
 * An individual lesson within a chapter.
 */
data class Lesson(
    val id: String,
    val chapterId: String,
    val title: String,
    val type: LessonType,
    val duration: String = "",
    val contentUrl: String? = null,
    val thumbnailUrl: String? = null,
    val orderIndex: Int,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false
)

/**
 * Enrollment record linking a user to a course.
 */
data class Enrollment(
    val id: String,
    val courseId: String,
    val userId: String,
    val progress: Float = 0f,
    val startedAt: String,
    val lastAccessedAt: String,
    val completedAt: String? = null
)

/**
 * Tracks progress for each lesson within an enrollment.
 */
data class LessonProgress(
    val id: String,
    val lessonId: String,
    val enrollmentId: String,
    val status: LessonProgressStatus = LessonProgressStatus.NOT_STARTED,
    val score: Int? = null,
    val startedAt: String? = null,
    val completedAt: String? = null
)

/**
 * A multiple-choice quiz question.
 */
data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String? = null
)

/**
 * Certificate awarded on course completion.
 */
data class CourseCertificate(
    val id: String,
    val courseId: String,
    val userId: String,
    val studentName: String,
    val courseName: String,
    val completedAt: String,
    val certificateUrl: String? = null,
    val verificationCode: String
)
