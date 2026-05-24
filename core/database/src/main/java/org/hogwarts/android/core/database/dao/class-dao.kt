package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.ClassAssignmentEntity
import org.hogwarts.android.core.database.entity.ClassEntity

/**
 * DAO for class-related database operations.
 *
 * CRITICAL: All queries MUST include schoolId for multi-tenant isolation.
 */
@Dao
interface ClassDao {

    @Query("SELECT c.* FROM classes c INNER JOIN class_assignments ca ON c.id = ca.classId WHERE ca.teacherId = :teacherId AND c.schoolId = :schoolId")
    fun getClassesByTeacher(teacherId: String, schoolId: String): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id AND schoolId = :schoolId")
    suspend fun getClassById(id: String, schoolId: String): ClassEntity?

    @Query("SELECT * FROM classes WHERE schoolId = :schoolId ORDER BY grade, section")
    fun getAllClasses(schoolId: String): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClasses(classes: List<ClassEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssignments(assignments: List<ClassAssignmentEntity>)

    @Query("DELETE FROM classes WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM class_assignments WHERE schoolId = :schoolId")
    suspend fun deleteAllAssignmentsForSchool(schoolId: String)
}
