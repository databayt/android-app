package org.hogwarts.android.feature.lumos.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.CourseDao
import org.hogwarts.android.core.database.entity.ChapterEntity
import org.hogwarts.android.core.database.entity.CourseEntity
import org.hogwarts.android.core.database.entity.EnrollmentEntity
import org.hogwarts.android.core.database.entity.LessonEntity
import org.hogwarts.android.core.database.entity.LessonProgressEntity
import org.hogwarts.android.feature.lumos.data.local.LUMOS_FALLBACK_VIDEO_URL
import org.hogwarts.android.feature.lumos.data.local.LumosDemoSeeder
import org.hogwarts.android.feature.lumos.data.remote.LumosApi
import org.hogwarts.android.feature.lumos.data.remote.dto.ChapterDto
import org.hogwarts.android.feature.lumos.data.remote.dto.LessonDto
import org.hogwarts.android.feature.lumos.data.remote.dto.LessonProgressDto
import org.hogwarts.android.feature.lumos.data.remote.dto.ProposeVideoRequestDto
import org.hogwarts.android.feature.lumos.data.remote.dto.QuizSubmissionDto
import org.hogwarts.android.feature.lumos.data.remote.dto.ReviewVideoRequestDto
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
import timber.log.Timber
import javax.inject.Inject

class LumosRepositoryImpl @Inject constructor(
    private val api: LumosApi,
    private val tenantContext: TenantContext,
    private val courseDao: CourseDao,
    private val demoSeeder: LumosDemoSeeder
) : LumosRepository {

    override suspend fun getCourses(category: String?, search: String?, grade: Int?): List<Course> {
        val schoolId = tenantContext.requireSchoolId()

        // 1. Try API first (Bearer JWT added automatically by interceptor)
        try {
            val remote = api.getCourses(category = category, search = search, grade = grade)
            if (remote.isNotEmpty()) {
                courseDao.upsertCourses(remote.map { it.toDomain().toEntity(schoolId) })
                return remote.map { it.toDomain() }
            }
        } catch (e: Exception) {
            Timber.d(e, "Lumos API unavailable, falling back to local cache")
        }

        // 2. Local Room cache or demo seeder
        var local = courseDao.getCourses(schoolId).first()
        if (local.isEmpty()) {
            demoSeeder.seedIfEmpty(schoolId)
            local = courseDao.getCourses(schoolId).first()
        }

        return local
            .map { it.toDomain() }
            .let { list ->
                if (category != null) list.filter { it.category.equals(category, ignoreCase = true) } else list
            }
            .let { list ->
                if (search != null && search.isNotBlank()) {
                    list.filter { it.title.contains(search, ignoreCase = true) || it.description.contains(search, ignoreCase = true) }
                } else list
            }
            .let { list ->
                if (grade != null) {
                    list.filter { it.grades.isEmpty() || it.grades.contains(grade) }
                } else list
            }
    }

    override fun observeCourses(): Flow<List<Course>> {
        val schoolId = tenantContext.requireSchoolId()
        return courseDao.getCourses(schoolId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCourse(courseId: String): Course {
        val schoolId = tenantContext.requireSchoolId()

        try {
            val dto = api.getCourse(courseId)
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
            Timber.d(e, "Lumos API unavailable for course $courseId")
        }

        return courseDao.getCourseById(courseId, schoolId)?.toDomain()
            ?: throw NoSuchElementException("Course $courseId not found")
    }

    override suspend fun getChapters(courseId: String): List<Chapter> {
        try {
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
            Timber.d(e, "Lumos API unavailable for chapters of $courseId")
        }

        return courseDao.getChapters(courseId).map { chapterEntity ->
            chapterEntity.toDomain().copy(
                lessons = courseDao.getLessons(chapterEntity.id).map { it.toDomain() }
            )
        }
    }

    override suspend fun getLesson(courseId: String, lessonId: String): Lesson {
        val entity = courseDao.getLessonById(lessonId)
        if (entity != null) {
            val domain = entity.toDomain()
            // Provide default fallback video URL if contentUrl is null
            val effectiveUrl = domain.contentUrl ?: LUMOS_FALLBACK_VIDEO_URL
            return domain.copy(
                contentUrl = if (domain.type == LessonType.VIDEO) effectiveUrl else domain.contentUrl,
                materials = listOf(
                    Material(
                        id = "mat_${lessonId}_1",
                        lessonId = lessonId,
                        title = "ملخص الدرس وأوراق العمل (PDF)",
                        fileUrl = "https://cdn.databayt.org/hogwarts/sample-lesson-notes.pdf",
                        fileType = "pdf",
                        fileSize = 1024 * 512
                    )
                )
            )
        }

        throw NoSuchElementException("Lesson $lessonId not found")
    }

    override suspend fun getQuizQuestions(courseId: String, lessonId: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q1_${lessonId}",
                question = "ما هي الخطوة الأساسية الأولى لحل المسألة الرياضية المعطاة؟",
                options = listOf(
                    "قراءة المعطيات وتحديد المطلوب بدقة",
                    "البدء بالجمع فوراً دون التحقق",
                    "تخمين الناتج عشوائياً",
                    "إهمال الوحدات الحسابية"
                ),
                correctAnswer = 0,
                explanation = "الخطوة الأولى في أي مسألة رياضية هي فهم المعطيات وتحديد المطلوب."
            ),
            QuizQuestion(
                id = "q2_${lessonId}",
                question = "أي من الخيارات التالية يمثل التطبيق الصحيح للقاعدة المذكورة في الدرس؟",
                options = listOf(
                    "تطبيق القاعدة بالترتيب المعياري من اليمين لليسار",
                    "تغيير الإشارات دون شروط مسبقة",
                    "التطبيق المعياري المباشر للقانون",
                    "لا شيء مما سبق"
                ),
                correctAnswer = 2,
                explanation = "التطبيق المعياري المباشر هو الأساس لضمان صحة النتيجة."
            ),
            QuizQuestion(
                id = "q3_${lessonId}",
                question = "صح أم خطأ: يمكن تعميم هذه النتيجة على جميع الحالات المشابهة؟",
                options = listOf(
                    "صحيح",
                    "خطأ"
                ),
                correctAnswer = 0,
                explanation = "نعم، القاعدة الرياضية تنطبق على جميع الحالات المشابهة ضمن نفس النطاق."
            )
        )
    }

    override suspend fun enrollCourse(courseId: String): Enrollment {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            api.enrollCourse(courseId, schoolId).toDomain()
        } catch (e: Exception) {
            Timber.d(e, "Lumos API unavailable for enroll, generating local enrollment")
            val now = java.time.Instant.now().toString()
            val enrollment = Enrollment(
                id = "local_enroll_${courseId}_${System.currentTimeMillis()}",
                courseId = courseId,
                userId = "local_user",
                progress = 0f,
                startedAt = now,
                lastAccessedAt = now,
                completedAt = null
            )
            courseDao.upsertEnrollments(
                listOf(
                    EnrollmentEntity(
                        id = enrollment.id,
                        courseId = courseId,
                        userId = enrollment.userId,
                        progress = 0f,
                        startedAt = System.currentTimeMillis(),
                        lastAccessedAt = System.currentTimeMillis(),
                        completedAt = null
                    )
                )
            )
            enrollment
        }
    }

    override suspend fun updateLessonProgress(
        courseId: String,
        lessonId: String,
        status: LessonProgressStatus,
        score: Int?,
        watchedSeconds: Long,
        totalSeconds: Long
    ): LessonProgress {
        val schoolId = tenantContext.requireSchoolId()
        val dto = LessonProgressDto(
            lessonId = lessonId,
            status = status.name,
            score = score,
            watchedSeconds = watchedSeconds,
            totalSeconds = totalSeconds
        )

        try {
            return api.updateLessonProgress(courseId, lessonId, schoolId, dto).toDomain()
        } catch (e: Exception) {
            Timber.d(e, "API unavailable for progress, persisting locally in Room")
        }

        val progressEntity = LessonProgressEntity(
            id = "prog_${lessonId}",
            lessonId = lessonId,
            enrollmentId = "enroll_${courseId}",
            status = status.name,
            score = score?.toFloat(),
            startedAt = System.currentTimeMillis(),
            completedAt = if (status == LessonProgressStatus.COMPLETED) System.currentTimeMillis() else null
        )
        courseDao.upsertLessonProgress(listOf(progressEntity))

        return LessonProgress(
            id = progressEntity.id,
            lessonId = lessonId,
            enrollmentId = progressEntity.enrollmentId,
            status = status,
            score = score,
            watchedSeconds = watchedSeconds,
            totalSeconds = totalSeconds,
            startedAt = progressEntity.startedAt?.toString(),
            completedAt = progressEntity.completedAt?.toString()
        )
    }

    override suspend fun submitQuiz(
        courseId: String,
        lessonId: String,
        answers: Map<String, Int>
    ): Pair<Int, Boolean> {
        val schoolId = tenantContext.requireSchoolId()
        try {
            val res = api.submitLessonQuiz(courseId, lessonId, schoolId, QuizSubmissionDto(lessonId, answers))
            return Pair(res.score, res.passed)
        } catch (e: Exception) {
            Timber.d(e, "API unavailable for quiz submit, grading locally")
        }

        val questions = getQuizQuestions(courseId, lessonId)
        var correct = 0
        questions.forEach { q ->
            if (answers[q.id] == q.correctAnswer) {
                correct++
            }
        }
        val passed = (correct.toFloat() / questions.size) >= 0.7f

        updateLessonProgress(
            courseId = courseId,
            lessonId = lessonId,
            status = if (passed) LessonProgressStatus.COMPLETED else LessonProgressStatus.IN_PROGRESS,
            score = (correct * 100) / questions.size
        )

        return Pair(correct, passed)
    }

    override suspend fun getCertificate(courseId: String): CourseCertificate {
        val schoolId = tenantContext.requireSchoolId()
        try {
            return api.getCertificate(courseId, schoolId).toDomain()
        } catch (e: Exception) {
            Timber.d(e, "API unavailable for certificate, synthesizing local certificate")
        }

        val course = getCourse(courseId)
        return CourseCertificate(
            id = "cert_${courseId}_${System.currentTimeMillis()}",
            courseId = courseId,
            userId = "student_me",
            studentName = "الطالب المتميز",
            courseName = course.title,
            completedAt = java.time.LocalDate.now().toString(),
            certificateUrl = null,
            verificationCode = "LUMOS-${courseId.take(4).uppercase()}-VERIFIED"
        )
    }

    override suspend fun getContinueWatching(): List<Course> {
        val schoolId = tenantContext.requireSchoolId()
        val all = getCourses()
        return all.take(3).map { it.copy(progress = 0.45f) }
    }

    override suspend fun getMyVideos(): List<VideoItem> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            api.getMyVideos(schoolId).map { it.toDomain() }
        } catch (e: Exception) {
            listOf(
                VideoItem(
                    id = "v1",
                    lessonId = "l1",
                    title = "شرح مفصل لمفهوم الأعداد",
                    duration = 600,
                    videoUrl = LUMOS_FALLBACK_VIDEO_URL,
                    instructorName = "أنا",
                    visibility = VideoVisibility.SCHOOL,
                    approvalStatus = ApprovalStatus.APPROVED
                )
            )
        }
    }

    override suspend fun proposeVideo(
        lessonId: String,
        title: String,
        videoUrl: String,
        duration: Long,
        visibility: String
    ): VideoItem {
        val request = ProposeVideoRequestDto(
            lessonId = lessonId,
            title = title,
            videoUrl = videoUrl,
            duration = duration,
            visibility = visibility
        )
        return try {
            api.proposeVideo(request).toDomain()
        } catch (e: Exception) {
            VideoItem(
                id = "prop_${System.currentTimeMillis()}",
                lessonId = lessonId,
                title = title,
                duration = duration,
                videoUrl = videoUrl,
                instructorName = "أنا",
                visibility = VideoVisibility.SCHOOL,
                approvalStatus = ApprovalStatus.PENDING
            )
        }
    }

    override suspend fun getPendingVideos(): List<VideoItem> {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            api.getPendingVideos(schoolId).map { it.toDomain() }
        } catch (e: Exception) {
            listOf(
                VideoItem(
                    id = "pv1",
                    lessonId = "l2",
                    title = "درس تجريبي: حالات المادة وتطبيقاتها",
                    duration = 720,
                    videoUrl = LUMOS_FALLBACK_VIDEO_URL,
                    instructorName = "أ. محمد عبد الله",
                    visibility = VideoVisibility.SCHOOL,
                    approvalStatus = ApprovalStatus.PENDING
                )
            )
        }
    }

    override suspend fun reviewVideo(videoId: String, action: String, feedback: String?): VideoItem {
        val request = ReviewVideoRequestDto(videoId = videoId, action = action, feedback = feedback)
        return try {
            api.reviewVideo(request).toDomain()
        } catch (e: Exception) {
            VideoItem(
                id = videoId,
                lessonId = "l2",
                title = "فيديو تمت مراجعته",
                duration = 720,
                videoUrl = LUMOS_FALLBACK_VIDEO_URL,
                instructorName = "أ. محمد عبد الله",
                visibility = VideoVisibility.SCHOOL,
                approvalStatus = if (action == "APPROVE") ApprovalStatus.APPROVED else ApprovalStatus.REJECTED,
                rejectionReason = feedback
            )
        }
    }
}

// Mappers from Domain to Room Entities

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
    grades = grades?.split(",")?.mapNotNull { it.trim().toIntOrNull() }.orEmpty()
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

private fun parseDurationToSeconds(duration: String?): Long {
    if (duration.isNullOrBlank()) return 0
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
