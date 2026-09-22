package org.hogwarts.android.feature.lumos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.lumos.domain.model.CatalogCourse
import org.hogwarts.android.feature.lumos.domain.model.CourseSearch
import org.hogwarts.android.feature.lumos.domain.model.GradeShelf
import org.hogwarts.android.feature.lumos.domain.model.LessonInstructor
import org.hogwarts.android.feature.lumos.domain.model.LumosCoursesPage
import org.hogwarts.android.feature.lumos.domain.model.ResumeLesson
import org.hogwarts.android.feature.lumos.domain.model.StartHereLesson

@Serializable
data class LumosCoursesPageDto(
    val shelves: List<GradeShelfDto> = emptyList(),
    @SerialName("continue_watching") val continueWatching: List<ResumeLessonDto> = emptyList(),
    @SerialName("recommended_grade") val recommendedGrade: Int? = null,
    @SerialName("effective_grade") val effectiveGrade: Int? = null,
    @SerialName("start_here") val startHere: StartHereLessonDto? = null,
    val search: CourseSearchDto = CourseSearchDto(),
) {
    fun toDomain() = LumosCoursesPage(
        shelves = shelves.map { GradeShelf(it.grade, it.courses.map(CatalogCourseDto::toDomain)) },
        continueWatching = continueWatching.map { it.toDomain() },
        recommendedGrade = recommendedGrade,
        effectiveGrade = effectiveGrade,
        startHere = startHere?.toDomain(),
        search = CourseSearch(
            query = search.query,
            courses = search.courses.map(CatalogCourseDto::toDomain),
            total = search.total,
            page = search.page,
        ),
    )
}

@Serializable
data class GradeShelfDto(val grade: Int, val courses: List<CatalogCourseDto> = emptyList())

@Serializable
data class CatalogCourseDto(
    val id: String,
    val slug: String,
    val title: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val color: String? = null,
    val grades: List<Int> = emptyList(),
    val chapters: Int = 0,
    val enrollments: Int = 0,
    @SerialName("total_lessons") val totalLessons: Int = 0,
    @SerialName("average_rating") val averageRating: Double = 0.0,
) {
    fun toDomain() = CatalogCourse(
        id = id,
        slug = slug,
        title = title,
        imageUrl = imageUrl,
        color = color,
        grades = grades,
        chapters = chapters,
        enrollments = enrollments,
        totalLessons = totalLessons,
        averageRating = averageRating,
    )
}

@Serializable
data class LessonInstructorDto(val name: String? = null, val image: String? = null)

@Serializable
data class ResumeLessonDto(
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_title") val lessonTitle: String = "",
    @SerialName("chapter_title") val chapterTitle: String = "",
    @SerialName("course_title") val courseTitle: String = "",
    @SerialName("course_slug") val courseSlug: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val color: String? = null,
    val grade: Int? = null,
    @SerialName("watched_seconds") val watchedSeconds: Int = 0,
    @SerialName("total_seconds") val totalSeconds: Int = 0,
    val instructor: LessonInstructorDto? = null,
) {
    fun toDomain() = ResumeLesson(
        lessonId = lessonId,
        lessonTitle = lessonTitle,
        chapterTitle = chapterTitle,
        courseTitle = courseTitle,
        courseSlug = courseSlug,
        thumbnailUrl = thumbnailUrl,
        color = color,
        grade = grade,
        watchedSeconds = watchedSeconds,
        totalSeconds = totalSeconds,
        instructor = instructor?.let { LessonInstructor(it.name, it.image) },
    )
}

@Serializable
data class StartHereLessonDto(
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_title") val lessonTitle: String = "",
    @SerialName("chapter_title") val chapterTitle: String = "",
    @SerialName("course_id") val courseId: String,
    @SerialName("course_title") val courseTitle: String = "",
    @SerialName("course_slug") val courseSlug: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val color: String? = null,
    val grade: Int? = null,
    @SerialName("total_lessons") val totalLessons: Int = 0,
    val instructor: LessonInstructorDto? = null,
) {
    fun toDomain() = StartHereLesson(
        lessonId = lessonId,
        lessonTitle = lessonTitle,
        chapterTitle = chapterTitle,
        courseId = courseId,
        courseTitle = courseTitle,
        courseSlug = courseSlug,
        thumbnailUrl = thumbnailUrl,
        color = color,
        grade = grade,
        totalLessons = totalLessons,
        instructor = instructor?.let { LessonInstructor(it.name, it.image) },
    )
}

@Serializable
data class CourseSearchDto(
    val query: String = "",
    val courses: List<CatalogCourseDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 12,
)
