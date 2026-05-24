package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Room entity for cached attendance records.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["studentId", "date"]),
        Index(value = ["classId", "date"]),
        Index(value = ["schoolId", "date"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val studentId: String,
    val studentName: String,
    val classId: String,
    val className: String,
    val date: LocalDate,
    val status: String,
    val checkInTime: LocalTime? = null,
    val note: String? = null,
    val markedBy: String? = null,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0
)
