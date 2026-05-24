package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.StudentEntity

/**
 * Room DAO for student records.
 *
 * All queries filter by schoolId for multi-tenant isolation.
 */
@Dao
interface StudentDao {

    @Query("SELECT * FROM students WHERE schoolId = :schoolId ORDER BY firstName ASC, lastName ASC")
    fun observeAll(schoolId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE schoolId = :schoolId AND id = :studentId")
    fun observeById(schoolId: String, studentId: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE schoolId = :schoolId AND classId = :classId ORDER BY firstName ASC")
    fun observeByClass(schoolId: String, classId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE schoolId = :schoolId AND status = :status ORDER BY firstName ASC")
    fun observeByStatus(schoolId: String, status: String): Flow<List<StudentEntity>>

    @Query("""
        SELECT * FROM students
        WHERE schoolId = :schoolId
        AND (firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR enrollmentNumber LIKE '%' || :query || '%')
        ORDER BY firstName ASC
    """)
    fun search(schoolId: String, query: String): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM students WHERE schoolId = :schoolId")
    suspend fun countAll(schoolId: String): Int

    @Query("SELECT COUNT(*) FROM students WHERE schoolId = :schoolId AND status = :status")
    suspend fun countByStatus(schoolId: String, status: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    @Update
    suspend fun update(student: StudentEntity)

    @Query("DELETE FROM students WHERE schoolId = :schoolId AND id = :studentId")
    suspend fun deleteById(schoolId: String, studentId: String)

    @Query("DELETE FROM students WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM students")
    suspend fun clearAll()
}
