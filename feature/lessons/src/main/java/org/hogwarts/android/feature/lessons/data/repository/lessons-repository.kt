package org.hogwarts.android.feature.lessons.data.repository

import org.hogwarts.android.feature.lessons.domain.model.CurriculumMap
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan

/**
 * Repository interface for lessons & curriculum data.
 */
interface LessonsRepository {
    suspend fun getLessonPlans(classId: String? = null, subjectId: String? = null): List<LessonPlan>
    suspend fun getLessonPlan(lessonId: String): LessonPlan
    suspend fun createLessonPlan(plan: LessonPlan): LessonPlan
    suspend fun updateLessonPlan(plan: LessonPlan): LessonPlan
    suspend fun getCurriculumMap(termId: String? = null): List<CurriculumMap>
}
