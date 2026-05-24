package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached notifications.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["schoolId", "createdAt"]),
        Index(value = ["schoolId", "isRead"])
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val title: String,
    val body: String,
    val type: String,
    /** Notification priority: low | normal | high | urgent (mirrors web NotificationPriority). */
    val priority: String = "normal",
    val isRead: Boolean = false,
    val deepLink: String? = null,
    val createdAt: Instant,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0
)
