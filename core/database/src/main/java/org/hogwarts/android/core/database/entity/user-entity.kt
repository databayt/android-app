package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Cached user entity for offline access.
 *
 * Note: schoolId is MANDATORY for all entities to ensure multi-tenant isolation.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["email"])
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val email: String,
    val role: String,
    val givenName: String?,
    val familyName: String?,
    val avatarUrl: String?,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0
)
