package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached conversations.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["schoolId", "updatedAt"]),
        Index(value = ["schoolId", "isPinned", "updatedAt"]),
    ]
)
data class ConversationEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val title: String,
    val type: String,
    val avatarUrl: String? = null,
    val lastMessageContent: String? = null,
    val lastMessageSenderName: String? = null,
    val lastMessageSentAt: Instant? = null,
    val lastMessageStatus: String? = null,
    val unreadCount: Int = 0,
    val isWhatsAppEnabled: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val updatedAt: Instant,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0,
)
