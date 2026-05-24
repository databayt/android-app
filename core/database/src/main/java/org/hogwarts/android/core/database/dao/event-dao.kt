package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.EventEntity
import org.hogwarts.android.core.database.entity.EventRegistrationEntity

/**
 * DAO for event-related database operations.
 *
 * CRITICAL: All queries MUST include schoolId for multi-tenant isolation.
 */
@Dao
interface EventDao {

    @Query("SELECT * FROM events WHERE schoolId = :schoolId ORDER BY startDate ASC")
    fun getEvents(schoolId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE schoolId = :schoolId AND type = :type ORDER BY startDate ASC")
    fun getEventsByType(schoolId: String, type: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id AND schoolId = :schoolId")
    suspend fun getEventById(id: String, schoolId: String): EventEntity?

    @Query("SELECT * FROM events WHERE schoolId = :schoolId AND startDate LIKE :yearMonth || '%' ORDER BY startDate ASC")
    fun getEventsByMonth(schoolId: String, yearMonth: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRegistrations(registrations: List<EventRegistrationEntity>)

    @Query("DELETE FROM events WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)
}
