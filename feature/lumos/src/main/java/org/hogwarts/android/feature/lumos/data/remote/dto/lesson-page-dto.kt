package org.hogwarts.android.feature.lumos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /api/mobile/lumos/lessons/{id}` — `/lumos/courses/[slug]/[lessonId]`, words included. */
@Serializable
data class LessonPageDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val color: String? = null,
    @SerialName("is_free") val isFree: Boolean = false,
    val duration: Int? = null,
    @SerialName("video_duration") val videoDuration: Int? = null,
    val course: LessonCourseRef,
    val chapter: LessonChapterRef,
    val position: Int = 1,
    val meta: String = "",
    val blurb: String = "",
    @SerialName("play_label") val playLabel: String = "",
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("is_fallback_video") val isFallbackVideo: Boolean = false,
    val instructors: Int = 0,
    val progress: LessonProgressRef? = null,
    val previous: LessonLink? = null,
    val next: LessonLink? = null,
    val siblings: List<SiblingDto> = emptyList(),
    val resources: List<ResourceDto> = emptyList(),
    val quiz: List<QuizQuestionDtoV2> = emptyList(),
    val labels: Map<String, String?> = emptyMap(),
)

@Serializable data class LessonCourseRef(val id: String, val slug: String, val title: String)
@Serializable data class LessonChapterRef(val title: String, val position: Int = 1)
@Serializable data class LessonLink(val id: String, val title: String)

@Serializable
data class LessonProgressRef(
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("watched_seconds") val watchedSeconds: Int = 0,
    @SerialName("total_seconds") val totalSeconds: Int? = null,
)

@Serializable
data class SiblingDto(
    val id: String,
    val title: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val color: String? = null,
    val duration: Int? = null,
    @SerialName("lesson_position") val lessonPosition: Int = 1,
    @SerialName("chapter_position") val chapterPosition: Int = 1,
    @SerialName("watched_minutes") val watchedMinutes: Int? = null,
)

@Serializable
data class ResourceDto(val id: String, val title: String, val description: String? = null, val url: String? = null)

/** An answer-key-free question; `choices == null` is a free-text answer. */
@Serializable
data class QuizQuestionDtoV2(val id: String, val text: String, val type: String = "", val choices: List<String>? = null)

@Serializable
data class QuizGradeDto(
    val score: Int = 0,
    @SerialName("total_questions") val total: Int = 0,
    val percentage: Int = 0,
    @SerialName("synced_to_gradebook") val recorded: Boolean = false,
    val verdicts: List<QuizVerdictDto> = emptyList(),
)

@Serializable
data class QuizVerdictDto(
    @SerialName("question_id") val questionId: String,
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("correct_index") val correctIndex: Int? = null,
    @SerialName("correct_answers") val correctAnswers: List<String>? = null,
    val explanation: String? = null,
    @SerialName("sample_answer") val sampleAnswer: String? = null,
)
