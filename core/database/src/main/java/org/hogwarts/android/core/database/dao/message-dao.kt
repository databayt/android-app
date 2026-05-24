package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.MessageEntity

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE schoolId = :schoolId AND conversationId = :conversationId ORDER BY sentAt ASC")
    fun observeByConversation(schoolId: String, conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE schoolId = :schoolId AND conversationId = :conversationId ORDER BY sentAt DESC LIMIT :limit")
    fun observeRecent(schoolId: String, conversationId: String, limit: Int = 50): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE schoolId = :schoolId AND nonce = :nonce LIMIT 1")
    suspend fun findByNonce(schoolId: String, nonce: String): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages WHERE schoolId = :schoolId AND conversationId = :conversationId AND isRead = 0")
    fun observeUnreadCount(schoolId: String, conversationId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE messages SET isRead = 1 WHERE schoolId = :schoolId AND conversationId = :conversationId")
    suspend fun markAllRead(schoolId: String, conversationId: String)

    @Query("UPDATE messages SET isDeleted = 1, content = '' WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE messages SET content = :content, isEdited = 1 WHERE id = :id")
    suspend fun updateContent(id: String, content: String)

    @Query("DELETE FROM messages WHERE schoolId = :schoolId AND conversationId = :conversationId")
    suspend fun deleteByConversation(schoolId: String, conversationId: String)

    @Query("DELETE FROM messages WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
