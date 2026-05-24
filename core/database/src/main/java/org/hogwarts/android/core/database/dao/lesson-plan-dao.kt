package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.LessonPlanEntity
import org.hogwarts.android.core.database.entity.LessonResourceEntity

@Dao
interface LessonPlanDao {
    @Query("SELECT * FROM lesson_plans WHERE schoolId = :schoolId ORDER BY date DESC")
    fun getLessonPlans(schoolId: String): Flow<List<LessonPlanEntity>>

    @Query("SELECT * FROM lesson_plans WHERE classId = :classId AND schoolId = :schoolId ORDER BY date DESC")
    fun getLessonPlansByClass(classId: String, schoolId: String): Flow<List<LessonPlanEntity>>

    @Query("SELECT * FROM lesson_plans WHERE id = :id AND schoolId = :schoolId")
    suspend fun getLessonPlanById(id: String, schoolId: String): LessonPlanEntity?

    @Query("SELECT * FROM lesson_resources WHERE lessonPlanId = :lessonPlanId")
    suspend fun getResources(lessonPlanId: String): List<LessonResourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonPlans(plans: List<LessonPlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResources(resources: List<LessonResourceEntity>)
}
