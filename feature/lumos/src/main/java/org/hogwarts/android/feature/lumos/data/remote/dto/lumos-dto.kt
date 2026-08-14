package org.hogwarts.android.feature.lumos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.lumos.domain.model.ApprovalStatus
import org.hogwarts.android.feature.lumos.domain.model.Chapter
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.model.CourseCertificate
import org.hogwarts.android.feature.lumos.domain.model.CourseStatus
import org.hogwarts.android.feature.lumos.domain.model.Enrollment
import org.hogwarts.android.feature.lumos.domain.model.Lesson
import org.hogwarts.android.feature.lumos.domain.model.LessonProgress
import org.hogwarts.android.feature.lumos.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.lumos.domain.model.LessonType
import org.hogwarts.android.feature.lumos.domain.model.Material
import org.hogwarts.android.feature.lumos.domain.model.QuizQuestion
import org.hogwarts.android.feature.lumos.domain.model.VideoItem
import org.hogwarts.android.feature.lumos.domain.model.VideoVisibility

@Serializable
data class CourseDto(
    val id: String,
    @SerialName("school_id") val schoolId: String? = null,
    val title: String,
    val slug: String? = null,
    val description: String? = null,
    @SerialName("instructor_name") val instructorName: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    val category: String? = null,
    @SerialName("enrollment_count") val enrollmentCount: Int = 0,
    @SerialName("lesson_count") val lessonCount: Int = 0,
    @SerialName("total_duration") val totalDuration: String? = null,
    val status: String? = null,
    val progress: Float = 0f,
    val grades: List<Int> = emptyList(),
    @SerialName("is_enrolled") val isEnrolled: Boolean = false,
    val chapters: List<ChapterDto>? = null
) {
    fun toDomain(): Course = Course(
        id = id,
        schoolId = schoolId.orEmpty(),
        title = title,
        description = description.orEmpty(),
        instructorName = instructorName.orEmpty(),
        thumbnailUrl = thumbnailUrl,
        bannerUrl = bannerUrl,
        category = category.orEmpty(),
        enrollmentCount = enrollmentCount,
        lessonCount = lessonCount,
        totalDuration = totalDuration.orEmpty(),
        status = when (status?.uppercase()) {
            "DRAFT" -> CourseStatus.DRAFT
            "ARCHIVED" -> CourseStatus.ARCHIVED
            else -> CourseStatus.PUBLISHED
        },
        progress = progress,
        grades = grades,
        isEnrolled = isEnrolled
    )
}

@Serializable
data class ChapterDto(
    val id: String,
    @SerialName("course_id") val courseId: String,
    val title: String,
    @SerialName("order_index") val orderIndex: Int,
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
    @SerialName("chapter_id") val chapterId: String,
    val title: String,
    val type: String,
    val duration: String? = null,
    @SerialName("content_url") val contentUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("is_locked") val isLocked: Boolean = false,
    @SerialName("is_paywalled") val isPaywalled: Boolean = false,
    val materials: List<MaterialDto> = emptyList(),
    @SerialName("available_videos") val availableVideos: List<VideoDto> = emptyList()
) {
    fun toDomain(): Lesson = Lesson(
        id = id,
        chapterId = chapterId,
        title = title,
        type = when (type.uppercase()) {
            "TEXT" -> LessonType.TEXT
            "QUIZ" -> LessonType.QUIZ
            else -> LessonType.VIDEO
        },
        duration = duration.orEmpty(),
        contentUrl = contentUrl,
        thumbnailUrl = thumbnailUrl,
        orderIndex = orderIndex,
        isCompleted = isCompleted,
        isLocked = isLocked,
        isPaywalled = isPaywalled,
        materials = materials.map { it.toDomain() },
        availableVideos = availableVideos.map { it.toDomain() }
    )
}

@Serializable
data class MaterialDto(
    val id: String,
    @SerialName("lesson_id") val lessonId: String,
    val title: String,
    val description: String? = null,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("file_type") val fileType: String = "pdf",
    @SerialName("file_size") val fileSize: Long = 0,
    @SerialName("is_downloadable") val isDownloadable: Boolean = true
) {
    fun toDomain(): Material = Material(
        id = id,
        lessonId = lessonId,
        title = title,
        description = description,
        fileUrl = fileUrl,
        fileType = fileType,
        fileSize = fileSize,
        isDownloadable = isDownloadable
    )
}

@Serializable
data class VideoDto(
    val id: String,
    @SerialName("lesson_id") val lessonId: String,
    val title: String,
    val duration: Long = 0,
    @SerialName("video_url") val videoUrl: String,
    @SerialName("instructor_name") val instructorName: String? = null,
    val visibility: String = "SCHOOL",
    @SerialName("approval_status") val approvalStatus: String = "APPROVED",
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false
) {
    fun toDomain(): VideoItem = VideoItem(
        id = id,
        lessonId = lessonId,
        title = title,
        duration = duration,
        videoUrl = videoUrl,
        instructorName = instructorName.orEmpty(),
        visibility = when (visibility.uppercase()) {
            "PUBLIC" -> VideoVisibility.PUBLIC
            "PRIVATE" -> VideoVisibility.PRIVATE
            "PAID" -> VideoVisibility.PAID
            else -> VideoVisibility.SCHOOL
        },
        approvalStatus = when (approvalStatus.uppercase()) {
            "PENDING" -> ApprovalStatus.PENDING
            "REJECTED" -> ApprovalStatus.REJECTED
            else -> ApprovalStatus.APPROVED
        },
        rejectionReason = rejectionReason,
        isFeatured = isFeatured
    )
}

@Serializable
data class EnrollmentDto(
    val id: String,
    @SerialName("course_id") val courseId: String,
    @SerialName("user_id") val userId: String,
    val progress: Float = 0f,
    @SerialName("started_at") val startedAt: String,
    @SerialName("last_accessed_at") val lastAccessedAt: String,
    @SerialName("completed_at") val completedAt: String? = null
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
    @SerialName("lesson_id") val lessonId: String,
    val status: String,
    val score: Int? = null,
    @SerialName("watched_seconds") val watchedSeconds: Long = 0,
    @SerialName("total_seconds") val totalSeconds: Long = 0,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null
) {
    fun toDomain(id: String = "", enrollmentId: String = ""): LessonProgress = LessonProgress(
        id = id,
        lessonId = lessonId,
        enrollmentId = enrollmentId,
        status = when (status.uppercase()) {
            "COMPLETED" -> LessonProgressStatus.COMPLETED
            "IN_PROGRESS" -> LessonProgressStatus.IN_PROGRESS
            else -> LessonProgressStatus.NOT_STARTED
        },
        score = score,
        watchedSeconds = watchedSeconds,
        totalSeconds = totalSeconds,
        startedAt = startedAt,
        completedAt = completedAt
    )
}

@Serializable
data class QuizQuestionDto(
    val id: String,
    val question: String,
    val options: List<String>,
    @SerialName("correct_answer") val correctAnswer: Int,
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
data class QuizSubmissionDto(
    @SerialName("lesson_id") val lessonId: String,
    val answers: Map<String, Int>
)

@Serializable
data class QuizResultDto(
    val score: Int,
    @SerialName("total_questions") val totalQuestions: Int,
    val percentage: Float,
    val passed: Boolean,
    @SerialName("synced_to_gradebook") val syncedToGradebook: Boolean = false
)

@Serializable
data class CertificateDto(
    val id: String,
    @SerialName("course_id") val courseId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("course_name") val courseName: String,
    @SerialName("completed_at") val completedAt: String,
    @SerialName("certificate_url") val certificateUrl: String? = null,
    @SerialName("verification_code") val verificationCode: String
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

@Serializable
data class ProposeVideoRequestDto(
    @SerialName("lesson_id") val lessonId: String,
    val title: String,
    @SerialName("video_url") val videoUrl: String,
    val duration: Long = 0,
    val visibility: String = "SCHOOL"
)

@Serializable
data class ReviewVideoRequestDto(
    @SerialName("video_id") val videoId: String,
    val action: String, // "APPROVE" or "REJECT"
    val feedback: String? = null
)
