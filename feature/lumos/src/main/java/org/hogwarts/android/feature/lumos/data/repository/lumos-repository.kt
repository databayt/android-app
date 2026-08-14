package org.hogwarts.android.feature.lumos.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.feature.lumos.domain.model.Chapter
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.model.CourseCertificate
import org.hogwarts.android.feature.lumos.domain.model.Enrollment
import org.hogwarts.android.feature.lumos.domain.model.Lesson
import org.hogwarts.android.feature.lumos.domain.model.LessonProgress
import org.hogwarts.android.feature.lumos.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.lumos.domain.model.QuizQuestion
import org.hogwarts.android.feature.lumos.domain.model.VideoItem

/**
 * Repository interface for Lumos LMS data access.
 */
interface LumosRepository {
    suspend fun getCourses(category: String? = null, search: String? = null, grade: Int? = null): List<Course>
    fun observeCourses(): Flow<List<Course>>
    suspend fun getCourse(courseId: String): Course
    suspend fun getChapters(courseId: String): List<Chapter>
    suspend fun getLesson(courseId: String, lessonId: String): Lesson
    suspend fun getQuizQuestions(courseId: String, lessonId: String): List<QuizQuestion>
    suspend fun enrollCourse(courseId: String): Enrollment
    suspend fun updateLessonProgress(
        courseId: String,
        lessonId: String,
        status: LessonProgressStatus,
        score: Int? = null,
        watchedSeconds: Long = 0,
        totalSeconds: Long = 0
    ): LessonProgress
    suspend fun submitQuiz(
        courseId: String,
        lessonId: String,
        answers: Map<String, Int>
    ): Pair<Int, Boolean>
    suspend fun getCertificate(courseId: String): CourseCertificate
    suspend fun getContinueWatching(): List<Course>

    // Teacher & Admin Video Management
    suspend fun getMyVideos(): List<VideoItem>
    suspend fun proposeVideo(lessonId: String, title: String, videoUrl: String, duration: Long, visibility: String): VideoItem
    suspend fun getPendingVideos(): List<VideoItem>
    suspend fun reviewVideo(videoId: String, action: String, feedback: String?): VideoItem
}
