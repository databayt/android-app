package org.hogwarts.android.feature.lumos.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.common.result.asResult
import org.hogwarts.android.feature.lumos.data.repository.LumosRepository
import org.hogwarts.android.feature.lumos.domain.model.Chapter
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.model.CourseCertificate
import org.hogwarts.android.feature.lumos.domain.model.Enrollment
import org.hogwarts.android.feature.lumos.domain.model.Lesson
import org.hogwarts.android.feature.lumos.domain.model.LessonProgress
import org.hogwarts.android.feature.lumos.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.lumos.domain.model.QuizQuestion
import org.hogwarts.android.feature.lumos.domain.model.VideoItem
import javax.inject.Inject

class GetCoursesUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(category: String? = null, search: String? = null, grade: Int? = null): Result<List<Course>> {
        return try {
            val courses = repository.getCourses(category, search, grade)
            Result.Success(courses)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetCourseDetailUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(courseId: String): Result<Course> {
        return try {
            val course = repository.getCourse(courseId)
            Result.Success(course)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChaptersUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(courseId: String): Result<List<Chapter>> {
        return try {
            val chapters = repository.getChapters(courseId)
            Result.Success(chapters)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetLessonUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(courseId: String, lessonId: String): Result<Lesson> {
        return try {
            val lesson = repository.getLesson(courseId, lessonId)
            Result.Success(lesson)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetQuizQuestionsUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(courseId: String, lessonId: String): Result<List<QuizQuestion>> {
        return try {
            val questions = repository.getQuizQuestions(courseId, lessonId)
            Result.Success(questions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class EnrollCourseUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(courseId: String): Result<Enrollment> {
        return try {
            val enrollment = repository.enrollCourse(courseId)
            Result.Success(enrollment)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class UpdateLessonProgressUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(
        courseId: String,
        lessonId: String,
        status: LessonProgressStatus,
        score: Int? = null,
        watchedSeconds: Long = 0,
        totalSeconds: Long = 0
    ): Result<LessonProgress> {
        return try {
            val progress = repository.updateLessonProgress(courseId, lessonId, status, score, watchedSeconds, totalSeconds)
            Result.Success(progress)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class SubmitQuizUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(
        courseId: String,
        lessonId: String,
        answers: Map<String, Int>
    ): Result<Pair<Int, Boolean>> {
        return try {
            val result = repository.submitQuiz(courseId, lessonId, answers)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetCertificateUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(courseId: String): Result<CourseCertificate> {
        return try {
            val cert = repository.getCertificate(courseId)
            Result.Success(cert)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetContinueWatchingUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(): Result<List<Course>> {
        return try {
            val list = repository.getContinueWatching()
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetTeacherVideosUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(): Result<List<VideoItem>> {
        return try {
            val list = repository.getMyVideos()
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class ProposeVideoUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(
        lessonId: String,
        title: String,
        videoUrl: String,
        duration: Long,
        visibility: String
    ): Result<VideoItem> {
        return try {
            val video = repository.proposeVideo(lessonId, title, videoUrl, duration, visibility)
            Result.Success(video)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetPendingVideosUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(): Result<List<VideoItem>> {
        return try {
            val list = repository.getPendingVideos()
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class ReviewVideoUseCase @Inject constructor(
    private val repository: LumosRepository
) {
    suspend operator fun invoke(videoId: String, action: String, feedback: String?): Result<VideoItem> {
        return try {
            val video = repository.reviewVideo(videoId, action, feedback)
            Result.Success(video)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
