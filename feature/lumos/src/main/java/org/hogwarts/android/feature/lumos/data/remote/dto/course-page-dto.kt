package org.hogwarts.android.feature.lumos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /api/mobile/lumos/courses/{slug}` — `/lumos/courses/[slug]`, words included. */
@Serializable
data class CoursePageDto(
    val id: String,
    val slug: String,
    val title: String,
    val description: String? = null,
    val objectives: List<String> = emptyList(),
    val prerequisites: String? = null,
    @SerialName("target_audience") val targetAudience: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val color: String? = null,
    @SerialName("quiz_count") val quizCount: Int = 0,
    @SerialName("school_name") val schoolName: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    @SerialName("is_enrolled") val isEnrolled: Boolean = false,
    val progress: CourseProgressDto? = null,
    val chapters: List<CoursePageChapterDto> = emptyList(),
    val labels: Map<String, String?> = emptyMap(),
)

@Serializable
data class CourseProgressDto(
    @SerialName("total_lessons") val totalLessons: Int,
    @SerialName("completed_lessons") val completedLessons: Int,
    val percent: Int,
    @SerialName("remaining_minutes") val remainingMinutes: Int,
)

@Serializable
data class CoursePageChapterDto(val id: String, val title: String, val lessons: List<CoursePageLessonDto> = emptyList())

@Serializable
data class CoursePageLessonDto(
    val id: String,
    val title: String,
    val duration: Int? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_free") val isFree: Boolean = false,
)
