package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.TimetableEntity

/**
 * Room DAO for timetable entries.
 *
 * All queries filter by schoolId for multi-tenant isolation.
 */
@Dao
interface TimetableDao {

    @Query("SELECT * FROM timetable WHERE schoolId = :schoolId AND userId = :userId ORDER BY dayOfWeek ASC, startTime ASC")
    fun observeByUser(schoolId: String, userId: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE schoolId = :schoolId AND userId = :userId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun observeByUserAndDay(schoolId: String, userId: String, dayOfWeek: Int): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TimetableEntity>)

    @Query("DELETE FROM timetable WHERE schoolId = :schoolId AND userId = :userId")
    suspend fun deleteByUser(schoolId: String, userId: String)

    @Query("DELETE FROM timetable WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM timetable")
    suspend fun clearAll()
}
