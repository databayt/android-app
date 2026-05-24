package org.hogwarts.android.feature.subjects.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.subjects.domain.model.AssignmentItem
import org.hogwarts.android.feature.subjects.domain.model.ExamItem
import org.hogwarts.android.feature.subjects.domain.model.MaterialItem
import org.hogwarts.android.feature.subjects.domain.model.MySubjectSummary
import org.hogwarts.android.feature.subjects.domain.model.QuestionStats
import org.hogwarts.android.feature.subjects.domain.model.QuestionTypeCard
import org.hogwarts.android.feature.subjects.domain.model.Subject
import org.hogwarts.android.feature.subjects.domain.model.SubjectChapter
import org.hogwarts.android.feature.subjects.domain.model.SubjectDetail
import org.hogwarts.android.feature.subjects.domain.model.SubjectLesson
import org.hogwarts.android.feature.subjects.domain.model.SubjectLevel
import org.hogwarts.android.feature.subjects.domain.model.VideoItem

@Serializable
data class SubjectDto(
    val id: String,
    val name: String,
    val slug: String,
    val department: String,
    val description: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val color: String? = null,
    val levels: List<String> = emptyList(),
    val grades: List<Int> = emptyList(),
    @SerialName("total_chapters") val totalChapters: Int = 0,
    @SerialName("total_lessons") val totalLessons: Int = 0,
    @SerialName("average_rating") val averageRating: Float = 0f,
    @SerialName("rating_count") val ratingCount: Int = 0,
) {
    fun toDomain() = Subject(
        id = id,
        slug = slug,
        name = name,
        department = department,
        description = description,
        thumbnailUrl = thumbnailUrl,
        color = color,
        levels = levels.map(SubjectLevel::fromString),
        grades = grades,
        totalChapters = totalChapters,
        totalLessons = totalLessons,
        averageRating = averageRating,
        ratingCount = ratingCount,
    )
}

@Serializable
data class SubjectListResponse(
    val data: List<SubjectDto>,
    val total: Int? = null,
)

@Serializable
data class SubjectLessonDto(
    val id: String,
    val title: String,
    val slug: String? = null,
    val description: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val color: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("video_count") val videoCount: Int = 0,
    @SerialName("resource_count") val resourceCount: Int = 0,
) {
    fun toDomain() = SubjectLesson(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        color = color,
        durationMinutes = durationMinutes,
        videoCount = videoCount,
        resourceCount = resourceCount,
    )
}

@Serializable
data class SubjectChapterDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val color: String? = null,
    @SerialName("total_lessons") val totalLessons: Int = 0,
    val lessons: List<SubjectLessonDto> = emptyList(),
) {
    fun toDomain() = SubjectChapter(
        id = id,
        name = name,
        description = description,
        thumbnailUrl = thumbnailUrl,
        color = color,
        totalLessons = totalLessons,
        lessons = lessons.map { it.toDomain() },
    )
}

@Serializable
data class VideoItemDto(
    val id: String,
    val title: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int = 0,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    val provider: String = "catalog",
    @SerialName("catalog_lesson_id") val catalogLessonId: String = "",
    val color: String? = null,
) {
    fun toDomain() = VideoItem(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        viewCount = viewCount,
        isFeatured = isFeatured,
        provider = provider,
        catalogLessonId = catalogLessonId,
        color = color,
    )
}

@Serializable
data class MaterialItemDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val type: String,
    @SerialName("page_count") val pageCount: Int? = null,
    @SerialName("download_count") val downloadCount: Int = 0,
    @SerialName("file_size") val fileSize: Long? = null,
    @SerialName("mime_type") val mimeType: String? = null,
) {
    fun toDomain() = MaterialItem(
        id = id,
        title = title,
        description = description,
        type = type,
        pageCount = pageCount,
        downloadCount = downloadCount,
        fileSize = fileSize,
        mimeType = mimeType,
    )
}

@Serializable
data class ExamItemDto(
    val id: String,
    val title: String,
    @SerialName("exam_type") val examType: String,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("total_marks") val totalMarks: Int? = null,
    @SerialName("total_questions") val totalQuestions: Int? = null,
    @SerialName("usage_count") val usageCount: Int = 0,
) {
    fun toDomain() = ExamItem(
        id = id,
        title = title,
        examType = examType,
        durationMinutes = durationMinutes,
        totalMarks = totalMarks,
        totalQuestions = totalQuestions,
        usageCount = usageCount,
    )
}

@Serializable
data class AssignmentItemDto(
    val id: String,
    val title: String,
    @SerialName("assignment_type") val assignmentType: String? = null,
    @SerialName("estimated_time") val estimatedTime: Int? = null,
    @SerialName("total_points") val totalPoints: Float? = null,
    @SerialName("usage_count") val usageCount: Int = 0,
) {
    fun toDomain() = AssignmentItem(
        id = id,
        title = title,
        assignmentType = assignmentType,
        estimatedTime = estimatedTime,
        totalPoints = totalPoints,
        usageCount = usageCount,
    )
}

@Serializable
data class QuestionTypeCardDto(
    val type: String,
    val count: Int = 0,
    @SerialName("by_difficulty") val byDifficulty: Map<String, Int> = emptyMap(),
) {
    fun toDomain() = QuestionTypeCard(
        type = type,
        count = count,
        byDifficulty = byDifficulty,
    )
}

@Serializable
data class QuestionStatsDto(
    val total: Int = 0,
    val cards: List<QuestionTypeCardDto> = emptyList(),
) {
    fun toDomain() = QuestionStats(
        total = total,
        cards = cards.map { it.toDomain() },
    )
}

@Serializable
data class SubjectDetailDto(
    val id: String,
    val name: String,
    val slug: String,
    val department: String,
    val description: String? = null,
    val levels: List<String> = emptyList(),
    val grades: List<Int> = emptyList(),
    @SerialName("grade_range") val gradeRange: String? = null,
    val curriculum: String? = null,
    val country: String? = null,
    val tags: List<String> = emptyList(),
    val color: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("total_chapters") val totalChapters: Int = 0,
    @SerialName("total_lessons") val totalLessons: Int = 0,
    @SerialName("total_content") val totalContent: Int = 0,
    @SerialName("usage_count") val usageCount: Int = 0,
    @SerialName("average_rating") val averageRating: Float = 0f,
    @SerialName("rating_count") val ratingCount: Int = 0,
    val chapters: List<SubjectChapterDto> = emptyList(),
    val videos: List<VideoItemDto> = emptyList(),
    val materials: List<MaterialItemDto> = emptyList(),
    val exams: List<ExamItemDto> = emptyList(),
    val assignments: List<AssignmentItemDto> = emptyList(),
    @SerialName("question_stats") val questionStats: QuestionStatsDto = QuestionStatsDto(),
) {
    fun toDomain() = SubjectDetail(
        subject = Subject(
            id = id,
            slug = slug,
            name = name,
            department = department,
            description = description,
            thumbnailUrl = thumbnailUrl,
            color = color,
            levels = levels.map(SubjectLevel::fromString),
            grades = grades,
            totalChapters = totalChapters,
            totalLessons = totalLessons,
            averageRating = averageRating,
            ratingCount = ratingCount,
        ),
        bannerUrl = bannerUrl,
        gradeRange = gradeRange,
        curriculum = curriculum,
        tags = tags,
        totalContent = totalContent,
        chapters = chapters.map { it.toDomain() },
        videos = videos.map { it.toDomain() },
        materials = materials.map { it.toDomain() },
        exams = exams.map { it.toDomain() },
        assignments = assignments.map { it.toDomain() },
        questionStats = questionStats.toDomain(),
    )
}

@Serializable
data class MySubjectSummaryDto(
    val id: String,
    val name: String,
    val slug: String,
    val department: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("teacher_name") val teacherName: String? = null,
) {
    fun toDomain() = MySubjectSummary(
        id = id,
        name = name,
        slug = slug,
        department = department,
        thumbnailUrl = thumbnailUrl,
        teacherName = teacherName,
    )
}

@Serializable
data class MySubjectsResponse(
    val data: List<MySubjectSummaryDto> = emptyList(),
)
