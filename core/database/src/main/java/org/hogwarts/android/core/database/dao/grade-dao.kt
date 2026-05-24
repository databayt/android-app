package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.GradeEntity

/**
 * Room DAO for grade records.
 *
 * All queries filter by schoolId for multi-tenant isolation.
 */
@Dao
interface GradeDao {

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId AND studentId = :studentId ORDER BY date DESC")
    fun observeByStudent(schoolId: String, studentId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId AND studentId = :studentId AND subjectId = :subjectId ORDER BY date DESC")
    fun observeByStudentAndSubject(schoolId: String, studentId: String, subjectId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId AND studentId = :studentId AND term = :term ORDER BY subjectName ASC, date DESC")
    fun observeByStudentAndTerm(schoolId: String, studentId: String, term: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE schoolId = :schoolId AND studentId = :studentId AND assessmentType = :type ORDER BY date DESC")
    fun observeByType(schoolId: String, studentId: String, type: String): Flow<List<GradeEntity>>

    @Query("SELECT AVG(score / maxScore * 100) FROM grades WHERE schoolId = :schoolId AND studentId = :studentId AND subjectId = :subjectId")
    suspend fun getAverageBySubject(schoolId: String, studentId: String, subjectId: String): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<GradeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: GradeEntity)

    @Query("DELETE FROM grades WHERE schoolId = :schoolId AND studentId = :studentId")
    suspend fun deleteByStudent(schoolId: String, studentId: String)

    @Query("DELETE FROM grades WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM grades")
    suspend fun clearAll()
}
