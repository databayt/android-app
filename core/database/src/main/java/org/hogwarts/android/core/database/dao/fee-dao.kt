package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.FeeEntity

@Dao
interface FeeDao {

    @Query("SELECT * FROM fees WHERE schoolId = :schoolId ORDER BY dueDate DESC")
    fun observeAll(schoolId: String): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE schoolId = :schoolId AND studentId = :studentId ORDER BY dueDate DESC")
    fun observeByStudent(schoolId: String, studentId: String): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE schoolId = :schoolId AND status = :status ORDER BY dueDate DESC")
    fun observeByStatus(schoolId: String, status: String): Flow<List<FeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fees: List<FeeEntity>)

    @Query("DELETE FROM fees WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM fees")
    suspend fun clearAll()
}
