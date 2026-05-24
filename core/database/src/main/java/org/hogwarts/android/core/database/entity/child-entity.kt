package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached guardian children data.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "children",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["guardianId", "schoolId"])
    ]
)
data class ChildEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val guardianId: String,
    val studentId: String,
    val givenName: String,
    val familyName: String,
    val grade: String,
    val section: String = "",
    val avatarUrl: String? = null,
    val attendanceRate: Float = 0f,
    val latestGrade: String? = null,
    val feeBalance: Double = 0.0,
    val className: String = "",

    /** Sync metadata */
    val lastSyncedAt: Instant
)
