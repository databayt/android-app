package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached messages.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["conversationId", "sentAt"]),
        Index(value = ["schoolId", "conversationId"]),
        Index(value = ["schoolId", "nonce"]),
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String? = null,
    val content: String,
    val contentType: String = "text",
    val status: String = "sent",
    val sentAt: Instant,
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val replyToId: String? = null,
    val nonce: String? = null,
    val whatsappStatus: String? = null,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0,
)
