package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached teacher class data.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "classes",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["subjectId", "schoolId"])
    ]
)
data class ClassEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val name: String,
    val grade: String,
    val section: String = "",
    val subjectId: String,
    val subjectName: String,
    val teacherCount: Int = 0,
    val studentCount: Int = 0,

    /** Sync metadata */
    val lastSyncedAt: Instant
)

/**
 * Room entity for teacher-class assignments.
 */
@Entity(
    tableName = "class_assignments",
    indices = [
        Index(value = ["classId", "schoolId"]),
        Index(value = ["teacherId", "schoolId"])
    ]
)
data class ClassAssignmentEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val classId: String,
    val teacherId: String,
    val role: String = "PRIMARY"
)
