package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalTime

/**
 * Room entity for cached timetable entries.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "timetable",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["schoolId", "userId"]),
        Index(value = ["dayOfWeek"])
    ]
)
data class TimetableEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    /** User this timetable belongs to (student or teacher) */
    val userId: String,

    val subjectName: String,
    val teacherName: String,
    val roomNumber: String,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val section: String? = null,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0
)
