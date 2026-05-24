package org.hogwarts.android.feature.stream.data.repository

import kotlinx.coroutines.flow.first
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.CourseDao
import org.hogwarts.android.core.database.entity.ChapterEntity
import org.hogwarts.android.core.database.entity.CourseEntity
import org.hogwarts.android.core.database.entity.LessonEntity
import org.hogwarts.android.feature.stream.data.local.StreamDemoSeeder
import org.hogwarts.android.feature.stream.data.remote.StreamApi
import org.hogwarts.android.feature.stream.data.remote.dto.ChapterDto
import org.hogwarts.android.feature.stream.data.remote.dto.LessonDto
import org.hogwarts.android.feature.stream.data.remote.dto.LessonProgressDto
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
import timber.log.Timber
import javax.inject.Inject

class StreamRepositoryImpl @Inject constructor(
    private val api: StreamApi,
    private val tenantContext: TenantContext,
    private val courseDao: CourseDao,
    private val demoSeeder: StreamDemoSeeder
) : StreamRepository {

    override suspend fun getCourses(category: String?, search: String?): List<Course> {
        val schoolId = tenantContext.requireSchoolId()

        // Try API first (JWT auth interceptor adds Bearer token automatically)
        try {
            val remote = api.getCourses(category, search)
            // Cache to Room
            courseDao.upsertCourses(remote.map { it.toDomain().toEntity(schoolId) })
            return remote.map { it.toDomain() }
        } catch (e: Exception) {
            Timber.d(e, "API unavailable, using local cache")
        }

        // Return from Room, seed demo data if empty
        var local = courseDao.getCourses(schoolId).first()
        if (local.isEmpty()) {
            demoSeeder.seedIfEmpty(schoolId)
            local = courseDao.getCourses(schoolId).first()
        }

        return local
            .map { it.toDomain() }
            .let { courses ->
                if (category != null) courses.filter { it.category == category } else courses
            }
            .let { courses ->
                if (search != null) courses.filter {
                    it.title.contains(search, ignoreCase = true)
                } else courses
            }
    }

    override suspend fun getCourse(courseId: String): Course {
        val schoolId = tenantContext.requireSchoolId()

        try {
            // The detail endpoint accepts slug or ID
            val dto = api.getCourse(courseId)
            // Cache course + inline chapters + lessons
            val course = dto.toDomain()
            courseDao.upsertCourses(listOf(course.toEntity(schoolId)))
            dto.chapters?.let { chapters ->
                courseDao.upsertChapters(chapters.map { it.toEntity() })
                chapters.forEach { ch ->
                    ch.lessons?.let { lessons ->
                        courseDao.upsertLessons(lessons.map { it.toEntity() })
                    }
                }
            }
            return course
        } catch (e: Exception) {
            Timber.d(e, "API unavailable for course $courseId")
        }

        return courseDao.getCourseById(courseId, schoolId)?.toDomain()
            ?: throw NoSuchElementException("Course $courseId not found")
    }

    override suspend fun getChapters(courseId: String): List<Chapter> {
        try {
            // Fetch full course detail which includes chapters+lessons
            val dto = api.getCourse(courseId)
            dto.chapters?.let { chapters ->
                courseDao.upsertChapters(chapters.map { it.toEntity() })
                chapters.forEach { ch ->
                    ch.lessons?.let { lessons ->
                        courseDao.upsertLessons(lessons.map { it.toEntity() })
                    }
                }
                return chapters.map { it.toDomain() }
            }
        } catch (e: Exception) {
            Timber.d(e, "API unavailable for chapters of $courseId")
        }

        // Cache fallback — attach lessons per chapter so the course-detail screen
        // can render chapter sections inline without extra fetches.
        return courseDao.getChapters(courseId).map { chapter ->
            chapter.toDomain().copy(
                lessons = courseDao.getLessons(chapter.id).map { it.toDomain() }
            )
        }
    }

    override suspend fun getLesson(courseId: String, lessonId: String): Lesson {
        // Lessons come from the cached detail response or Room
        return courseDao.getLessonById(lessonId)?.toDomain()
            ?: throw NoSuchElementException("Lesson $lessonId not found")
    }

    override suspend fun getQuizQuestions(courseId: String, lessonId: String): List<QuizQuestion> {
        // Quiz questions not yet served by catalog API
        return emptyList()
    }

    override suspend fun enrollCourse(courseId: String): Enrollment {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            api.enrollCourse(courseId, schoolId).toDomain()
        } catch (e: Exception) {
            // Demo/offline fallback — synthesize a local Enrollment so the
            // enroll → first-lesson flow is usable without a backend.
            Timber.d(e, "API unavailable for enroll, returning local enrollment")
            val now = java.time.Instant.now().toString()
            Enrollment(
                id = "local_${courseId}_${System.currentTimeMillis()}",
                courseId = courseId,
                userId = "local",
                progress = 0f,
                startedAt = now,
                lastAccessedAt = now,
                completedAt = null
            )
        }
    }

    override suspend fun updateLessonProgress(
        courseId: String,
        lessonId: String,
        status: LessonProgressStatus,
        score: Int?
    ): LessonProgress {
        val schoolId = tenantContext.requireSchoolId()
        val progressDto = LessonProgressDto(
            lessonId = lessonId,
            status = status.name,
            score = score
        )
        return api.updateLessonProgress(courseId, lessonId, schoolId, progressDto).toDomain()
    }

    override suspend fun getCertificate(courseId: String): CourseCertificate {
        val schoolId = tenantContext.requireSchoolId()
        return api.getCertificate(courseId, schoolId).toDomain()
    }
}

// Entity -> Domain mappers

private fun CourseEntity.toDomain() = Course(
    id = id,
    schoolId = schoolId,
    title = title,
    description = description,
    instructorName = instructorName,
    thumbnailUrl = thumbnailUrl,
    category = category,
    enrollmentCount = enrollmentCount,
    lessonCount = lessonCount,
    totalDuration = formatDuration(totalDuration),
    status = try { CourseStatus.valueOf(status) } catch (_: Exception) { CourseStatus.PUBLISHED },
    progress = 0f,
    grades = parseGrades(grades)
)

private fun ChapterEntity.toDomain() = Chapter(
    id = id,
    courseId = courseId,
    title = title,
    orderIndex = orderIndex,
    lessonCount = lessonCount,
    completedLessons = completedLessons
)

private fun LessonEntity.toDomain() = Lesson(
    id = id,
    chapterId = chapterId,
    title = title,
    type = try { LessonType.valueOf(type) } catch (_: Exception) { LessonType.VIDEO },
    duration = formatMinutes(duration),
    contentUrl = contentUrl,
    thumbnailUrl = thumbnailUrl,
    orderIndex = orderIndex,
    isCompleted = isCompleted,
    isLocked = isLocked
)

// Domain -> Entity mapper (for caching API results)

private fun Course.toEntity(schoolId: String) = CourseEntity(
    id = id,
    schoolId = schoolId,
    title = title,
    description = description,
    instructorName = instructorName,
    thumbnailUrl = thumbnailUrl,
    category = category,
    enrollmentCount = enrollmentCount,
    lessonCount = lessonCount,
    totalDuration = 0L,
    status = status.name,
    grades = grades.joinToString(",").ifEmpty { null },
    lastSyncedAt = System.currentTimeMillis()
)

private fun parseGrades(raw: String?): List<Int> =
    raw?.split(",")?.mapNotNull { it.trim().toIntOrNull() }.orEmpty()

// DTO -> Entity mappers (for caching API detail responses)

private fun ChapterDto.toEntity() = ChapterEntity(
    id = id,
    courseId = courseId,
    title = title,
    orderIndex = orderIndex,
    lessonCount = lessonCount,
    completedLessons = completedLessons
)

private fun LessonDto.toEntity() = LessonEntity(
    id = id,
    chapterId = chapterId,
    title = title,
    type = type,
    duration = parseDurationToSeconds(duration),
    contentUrl = contentUrl,
    thumbnailUrl = thumbnailUrl,
    orderIndex = orderIndex,
    isCompleted = isCompleted,
    isLocked = isLocked
)

private fun parseDurationToSeconds(duration: String): Long {
    if (duration.isBlank()) return 0
    val minutes = duration.replace("m", "").trim().toLongOrNull() ?: 0
    return minutes * 60
}

private fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0) return ""
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

private fun formatMinutes(totalSeconds: Long): String {
    if (totalSeconds <= 0) return ""
    val minutes = totalSeconds / 60
    return "${minutes}m"
}
