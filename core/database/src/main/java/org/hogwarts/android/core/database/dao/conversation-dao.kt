package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.ConversationEntity

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations WHERE schoolId = :schoolId ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAll(schoolId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE schoolId = :schoolId AND type = :type ORDER BY isPinned DESC, updatedAt DESC")
    fun observeByType(schoolId: String, type: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE schoolId = :schoolId AND unreadCount > 0 ORDER BY updatedAt DESC")
    fun observeUnread(schoolId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE schoolId = :schoolId AND id = :conversationId")
    fun observeById(schoolId: String, conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT COALESCE(SUM(unreadCount), 0) FROM conversations WHERE schoolId = :schoolId")
    fun observeTotalUnreadCount(schoolId: String): Flow<Int>

    @Query("UPDATE conversations SET unreadCount = :count WHERE schoolId = :schoolId AND id = :conversationId")
    suspend fun updateUnreadCount(schoolId: String, conversationId: String, count: Int)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE schoolId = :schoolId AND id = :conversationId")
    suspend fun updatePinned(schoolId: String, conversationId: String, pinned: Boolean)

    @Query("UPDATE conversations SET isMuted = :muted WHERE schoolId = :schoolId AND id = :conversationId")
    suspend fun updateMuted(schoolId: String, conversationId: String, muted: Boolean)

    @Query("""
        UPDATE conversations SET
            lastMessageContent = :content,
            lastMessageSenderName = :senderName,
            lastMessageSentAt = :sentAt,
            lastMessageStatus = :status,
            updatedAt = :sentAt
        WHERE schoolId = :schoolId AND id = :conversationId
    """)
    suspend fun updateLastMessage(
        schoolId: String,
        conversationId: String,
        content: String,
        senderName: String,
        sentAt: Long,
        status: String,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}
