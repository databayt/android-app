package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.AnnouncementEntity

@Dao
interface AnnouncementDao {

    @Query("SELECT * FROM announcements WHERE schoolId = :schoolId ORDER BY date DESC")
    fun observeAll(schoolId: String): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE schoolId = :schoolId AND type = :type ORDER BY date DESC")
    fun observeByType(schoolId: String, type: String): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE schoolId = :schoolId AND id = :id")
    fun observeById(schoolId: String, id: String): Flow<AnnouncementEntity?>

    @Query("SELECT * FROM announcements WHERE schoolId = :schoolId AND type = 'EVENT' AND date >= :fromDate ORDER BY date ASC")
    fun observeUpcomingEvents(schoolId: String, fromDate: String): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AnnouncementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)
}
