package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_badges",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["userId", "schoolId"])
    ]
)
data class AttendanceBadgeEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val userId: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val type: String,
    val earnedAt: Long? = null,
    val lastSyncedAt: Long
)

@Entity(
    tableName = "hall_passes",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["studentId", "schoolId"]),
        Index(value = ["teacherId", "schoolId"])
    ]
)
data class HallPassEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val studentId: String,
    val studentName: String,
    val teacherId: String,
    val destination: String,
    val reason: String? = null,
    val requestedAt: Long,
    val approvedAt: Long? = null,
    val expiresAt: Long? = null,
    val status: String,
    val lastSyncedAt: Long
)

@Entity(
    tableName = "attendance_interventions",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["studentId", "schoolId"])
    ]
)
data class AttendanceInterventionEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val studentId: String,
    val studentName: String,
    val type: String,
    val threshold: Float,
    val currentRate: Float,
    val status: String,
    val createdAt: Long,
    val resolvedAt: Long? = null,
    val lastSyncedAt: Long
)
