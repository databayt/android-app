package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.ExamEntity
import java.time.LocalDate

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams WHERE schoolId = :schoolId ORDER BY date ASC, startTime ASC")
    fun observeAll(schoolId: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE schoolId = :schoolId AND status = :status ORDER BY date ASC")
    fun observeByStatus(schoolId: String, status: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE schoolId = :schoolId AND date >= :fromDate ORDER BY date ASC")
    fun observeUpcoming(schoolId: String, fromDate: LocalDate): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE schoolId = :schoolId AND id = :examId")
    fun observeById(schoolId: String, examId: String): Flow<ExamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exams: List<ExamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM exams")
    suspend fun clearAll()
}
