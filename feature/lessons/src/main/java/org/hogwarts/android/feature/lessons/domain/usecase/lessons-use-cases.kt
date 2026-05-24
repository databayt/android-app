package org.hogwarts.android.feature.lessons.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lessons.data.repository.LessonsRepository
import org.hogwarts.android.feature.lessons.domain.model.CurriculumMap
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan
import org.hogwarts.android.feature.lessons.domain.model.LessonResource
import javax.inject.Inject

class GetLessonPlansUseCase @Inject constructor(
    private val repository: LessonsRepository
) {
    suspend operator fun invoke(
        classId: String? = null,
        subjectId: String? = null
    ): Result<List<LessonPlan>> {
        return try {
            Result.Success(repository.getLessonPlans(classId, subjectId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetLessonDetailUseCase @Inject constructor(
    private val repository: LessonsRepository
) {
    suspend operator fun invoke(lessonId: String): Result<LessonPlan> {
        return try {
            Result.Success(repository.getLessonPlan(lessonId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class CreateLessonPlanUseCase @Inject constructor(
    private val repository: LessonsRepository
) {
    suspend operator fun invoke(plan: LessonPlan): Result<LessonPlan> {
        return try {
            Result.Success(repository.createLessonPlan(plan))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class UpdateLessonPlanUseCase @Inject constructor(
    private val repository: LessonsRepository
) {
    suspend operator fun invoke(plan: LessonPlan): Result<LessonPlan> {
        return try {
            Result.Success(repository.updateLessonPlan(plan))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetCurriculumMapUseCase @Inject constructor(
    private val repository: LessonsRepository
) {
    suspend operator fun invoke(termId: String? = null): Result<List<CurriculumMap>> {
        return try {
            Result.Success(repository.getCurriculumMap(termId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetResourcesUseCase @Inject constructor(
    private val repository: LessonsRepository
) {
    suspend operator fun invoke(
        classId: String? = null,
        subjectId: String? = null
    ): Result<List<LessonResource>> {
        return try {
            val plans = repository.getLessonPlans(classId, subjectId)
            val resources = plans.flatMap { it.resources }.distinctBy { it.id }
            Result.Success(resources)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
