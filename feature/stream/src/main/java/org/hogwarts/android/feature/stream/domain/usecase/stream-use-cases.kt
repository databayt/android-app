package org.hogwarts.android.feature.stream.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.stream.data.repository.StreamRepository
import org.hogwarts.android.feature.stream.domain.model.Chapter
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.model.CourseCertificate
import org.hogwarts.android.feature.stream.domain.model.Enrollment
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonProgress
import org.hogwarts.android.feature.stream.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.stream.domain.model.QuizQuestion
import javax.inject.Inject

class GetCoursesUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(category: String? = null, search: String? = null): Result<List<Course>> {
        return try {
            Result.Success(repository.getCourses(category, search))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetCourseDetailUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(courseId: String): Result<Course> {
        return try {
            Result.Success(repository.getCourse(courseId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChaptersUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(courseId: String): Result<List<Chapter>> {
        return try {
            Result.Success(repository.getChapters(courseId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class EnrollCourseUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(courseId: String): Result<Enrollment> {
        return try {
            Result.Success(repository.enrollCourse(courseId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetLessonUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(courseId: String, lessonId: String): Result<Lesson> {
        return try {
            Result.Success(repository.getLesson(courseId, lessonId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetQuizQuestionsUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(courseId: String, lessonId: String): Result<List<QuizQuestion>> {
        return try {
            Result.Success(repository.getQuizQuestions(courseId, lessonId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class UpdateLessonProgressUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(
        courseId: String,
        lessonId: String,
        status: LessonProgressStatus,
        score: Int? = null
    ): Result<LessonProgress> {
        return try {
            Result.Success(repository.updateLessonProgress(courseId, lessonId, status, score))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetCertificateUseCase @Inject constructor(
    private val repository: StreamRepository
) {
    suspend operator fun invoke(courseId: String): Result<CourseCertificate> {
        return try {
            Result.Success(repository.getCertificate(courseId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
