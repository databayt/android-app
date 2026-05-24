package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.MessageAttachmentEntity

@Dao
interface MessageAttachmentDao {

    @Query("SELECT * FROM message_attachments WHERE messageId = :messageId")
    fun observeByMessage(messageId: String): Flow<List<MessageAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attachments: List<MessageAttachmentEntity>)

    @Query("DELETE FROM message_attachments WHERE messageId = :messageId")
    suspend fun deleteByMessage(messageId: String)

    @Query("DELETE FROM message_attachments WHERE schoolId = :schoolId")
    suspend fun deleteAllForSchool(schoolId: String)
}
