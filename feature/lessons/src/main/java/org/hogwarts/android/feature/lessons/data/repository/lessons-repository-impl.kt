package org.hogwarts.android.feature.lessons.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.lessons.data.remote.LessonsApi
import org.hogwarts.android.feature.lessons.data.remote.dto.LessonPlanDto
import org.hogwarts.android.feature.lessons.domain.model.CurriculumMap
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan
import javax.inject.Inject

class LessonsRepositoryImpl @Inject constructor(
    private val api: LessonsApi,
    private val tenantContext: TenantContext
) : LessonsRepository {

    override suspend fun getLessonPlans(classId: String?, subjectId: String?): List<LessonPlan> {
        return api.getLessonPlans(classId, subjectId).map { it.toDomain() }
    }

    override suspend fun getLessonPlan(lessonId: String): LessonPlan {
        return api.getLessonPlan(lessonId).toDomain()
    }

    override suspend fun createLessonPlan(plan: LessonPlan): LessonPlan {
        val schoolId = tenantContext.requireSchoolId()
        val dto = LessonPlanDto.fromDomain(plan.copy(schoolId = schoolId))
        return api.createLessonPlan(dto).toDomain()
    }

    override suspend fun updateLessonPlan(plan: LessonPlan): LessonPlan {
        val schoolId = tenantContext.requireSchoolId()
        val dto = LessonPlanDto.fromDomain(plan.copy(schoolId = schoolId))
        return api.updateLessonPlan(plan.id, dto).toDomain()
    }

    override suspend fun getCurriculumMap(termId: String?): List<CurriculumMap> {
        return api.getCurriculumMap(termId).map { it.toDomain() }
    }
}
