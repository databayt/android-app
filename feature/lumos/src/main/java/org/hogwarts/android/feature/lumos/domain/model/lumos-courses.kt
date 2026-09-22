package org.hogwarts.android.feature.lumos.domain.model

/**
 * `/lumos/courses` for one reader — the page's `CoursesRenderer`, served by
 * `/api/mobile/lumos/courses` over the same reads, so the grade the page opens
 * on, the lead card and the shelves are the browser's, not rebuilt here.
 */
data class LumosCoursesPage(
    /** One shelf per grade the school offers, lowest first. */
    val shelves: List<GradeShelf>,
    val continueWatching: List<ResumeLesson>,
    /** The reader's own grade — a student's; null for everyone else. */
    val recommendedGrade: Int?,
    /** Which grade the browse view is showing. */
    val effectiveGrade: Int?,
    /** The grade's opening lesson, when nothing is in progress. */
    val startHere: StartHereLesson?,
    /** Filled only when the request carried a search. */
    val search: CourseSearch,
)

data class GradeShelf(val grade: Int, val courses: List<CatalogCourse>)

/** A course as the catalog cards draw it. */
data class CatalogCourse(
    val id: String,
    val slug: String,
    val title: String,
    val imageUrl: String?,
    val color: String?,
    val grades: List<Int>,
    val chapters: Int,
    val enrollments: Int,
    val totalLessons: Int,
    val averageRating: Double,
    val levels: List<String> = emptyList(),
    val category: String? = null,
) {
    /**
     * `getCourseTypeKey` in `course-card.tsx`: the course's kind, read off how
     * many chapters it has.
     */
    val typeKey: CourseTypeKey
        get() = when {
            chapters >= 10 -> CourseTypeKey.PROFESSIONAL_CERTIFICATE
            chapters >= 5 -> CourseTypeKey.SPECIALIZATION
            chapters >= 3 -> CourseTypeKey.COURSE
            else -> CourseTypeKey.SHORT_COURSE
        }
}

enum class CourseTypeKey { PROFESSIONAL_CERTIFICATE, SPECIALIZATION, COURSE, SHORT_COURSE }

data class LessonInstructor(val name: String?, val image: String?)

/** A lesson this reader started and has not finished. */
data class ResumeLesson(
    val lessonId: String,
    val lessonTitle: String,
    val chapterTitle: String,
    val courseTitle: String,
    val courseSlug: String,
    val thumbnailUrl: String?,
    val color: String?,
    val grade: Int?,
    val watchedSeconds: Int,
    val totalSeconds: Int,
    val instructor: LessonInstructor?,
)

/** The opening lesson of the shown grade's first course with a way in. */
data class StartHereLesson(
    val lessonId: String,
    val lessonTitle: String,
    val chapterTitle: String,
    val courseId: String,
    val courseTitle: String,
    val courseSlug: String,
    val thumbnailUrl: String?,
    val color: String?,
    val grade: Int?,
    val totalLessons: Int,
    val instructor: LessonInstructor?,
)

/** One page of `/api/mobile/lumos/course-search`. */
data class CourseSearchPage(val courses: List<CatalogCourse>, val count: Int)

data class CourseSearch(
    val query: String,
    val courses: List<CatalogCourse>,
    val total: Int,
    val page: Int,
)
