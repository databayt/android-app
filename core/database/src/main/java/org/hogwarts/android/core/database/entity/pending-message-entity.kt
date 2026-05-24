package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for messages queued for sending.
 * Persists across app restarts so WorkManager can retry.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "pending_messages",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["schoolId", "conversationId"]),
        Index(value = ["status"]),
    ]
)
data class PendingMessageEntity(
    /** UUID nonce -- also used as the dedup key in MessageEntity */
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val conversationId: String,
    val content: String,
    val contentType: String = "text",
    val replyToId: String? = null,
    val attachmentUri: String? = null,
    val status: String = "QUEUED",
    val createdAt: Instant,
    val retryCount: Int = 0,
    val lastAttemptAt: Instant? = null,
)
