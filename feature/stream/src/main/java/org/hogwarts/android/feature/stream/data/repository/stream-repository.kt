package org.hogwarts.android.feature.stream.data.repository

import org.hogwarts.android.feature.stream.data.remote.dto.QuizQuestionDto
import org.hogwarts.android.feature.stream.domain.model.Chapter
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.model.CourseCertificate
import org.hogwarts.android.feature.stream.domain.model.Enrollment
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonProgress
import org.hogwarts.android.feature.stream.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.stream.domain.model.QuizQuestion

/**
 * Repository interface for LMS/Stream data operations.
 */
interface StreamRepository {
    suspend fun getCourses(category: String? = null, search: String? = null): List<Course>
    suspend fun getCourse(courseId: String): Course
    suspend fun getChapters(courseId: String): List<Chapter>
    suspend fun getLesson(courseId: String, lessonId: String): Lesson
    suspend fun getQuizQuestions(courseId: String, lessonId: String): List<QuizQuestion>
    suspend fun enrollCourse(courseId: String): Enrollment
    suspend fun updateLessonProgress(courseId: String, lessonId: String, status: LessonProgressStatus, score: Int? = null): LessonProgress
    suspend fun getCertificate(courseId: String): CourseCertificate
}
