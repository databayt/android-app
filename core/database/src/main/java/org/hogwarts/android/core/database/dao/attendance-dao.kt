package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.AttendanceEntity
import java.time.LocalDate

/**
 * Room DAO for attendance records.
 *
 * All queries filter by schoolId for multi-tenant isolation.
 */
@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance WHERE schoolId = :schoolId AND studentId = :studentId ORDER BY date DESC")
    fun observeByStudent(schoolId: String, studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE schoolId = :schoolId AND studentId = :studentId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun observeByStudentDateRange(
        schoolId: String,
        studentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE schoolId = :schoolId AND classId = :classId AND date = :date ORDER BY studentName ASC")
    fun observeByClassAndDate(schoolId: String, classId: String, date: LocalDate): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE schoolId = :schoolId AND date = :date ORDER BY className ASC, studentName ASC")
    fun observeByDate(schoolId: String, date: LocalDate): Flow<List<AttendanceEntity>>

    @Query("SELECT COUNT(*) FROM attendance WHERE schoolId = :schoolId AND studentId = :studentId AND status = :status")
    suspend fun countByStatus(schoolId: String, studentId: String, status: String): Int

    @Query("SELECT COUNT(*) FROM attendance WHERE schoolId = :schoolId AND studentId = :studentId")
    suspend fun countTotal(schoolId: String, studentId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceEntity)

    @Update
    suspend fun update(record: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE schoolId = :schoolId AND studentId = :studentId")
    suspend fun deleteByStudent(schoolId: String, studentId: String)

    @Query("DELETE FROM attendance WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM attendance")
    suspend fun clearAll()
}
