package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for cached message attachments.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "message_attachments",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["messageId"]),
    ]
)
data class MessageAttachmentEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val messageId: String,
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long = 0,
    val fileType: String,
    val thumbnail: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)
