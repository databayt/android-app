package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached subject data.
 */
@Entity(
    tableName = "subjects",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["department", "schoolId"])
    ]
)
data class SubjectEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val name: String,
    val code: String = "",
    val department: String = "",
    val description: String = "",
    val teacherCount: Int = 0,
    val studentCount: Int = 0,
    val iconUrl: String? = null,
    val lastSyncedAt: Instant
)
