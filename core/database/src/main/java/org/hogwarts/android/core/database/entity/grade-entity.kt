package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * Room entity for cached grade records.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "grades",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["studentId"]),
        Index(value = ["schoolId", "studentId"]),
        Index(value = ["subjectId"])
    ]
)
data class GradeEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val studentId: String,
    val subjectId: String,
    val subjectName: String,
    val assessmentType: String,
    val assessmentName: String,
    val score: Float,
    val maxScore: Float,
    val grade: String? = null,
    val date: LocalDate,
    val term: String? = null,
    val remarks: String? = null,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0
)
