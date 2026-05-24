package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.PendingMessageEntity

@Dao
interface PendingMessageDao {

    @Query("SELECT * FROM pending_messages WHERE schoolId = :schoolId AND conversationId = :conversationId ORDER BY createdAt ASC")
    fun observeByConversation(schoolId: String, conversationId: String): Flow<List<PendingMessageEntity>>

    @Query("SELECT * FROM pending_messages WHERE status IN ('QUEUED', 'FAILED') AND retryCount < 5 ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: PendingMessageEntity)

    @Query("UPDATE pending_messages SET status = :status, lastAttemptAt = :attemptAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, attemptAt: Long)

    @Query("UPDATE pending_messages SET retryCount = retryCount + 1, status = :status, lastAttemptAt = :attemptAt WHERE id = :id")
    suspend fun incrementRetry(id: String, status: String, attemptAt: Long)

    @Query("DELETE FROM pending_messages WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_messages WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)
}
