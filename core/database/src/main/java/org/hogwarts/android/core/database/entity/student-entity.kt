package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * Room entity for cached student records.
 *
 * CRITICAL: schoolId is MANDATORY for multi-tenant isolation.
 */
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["schoolId", "classId"]),
        Index(value = ["schoolId", "status"])
    ]
)
data class StudentEntity(
    @PrimaryKey
    val id: String,

    /** School ID for multi-tenant isolation - MANDATORY */
    val schoolId: String,

    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val enrollmentNumber: String? = null,
    val classId: String? = null,
    val className: String? = null,
    val section: String? = null,
    val guardianName: String? = null,
    val guardianPhone: String? = null,
    val status: String = "ACTIVE",
    val avatarUrl: String? = null,

    /** Sync metadata */
    val lastSyncAt: Instant,
    val serverVersion: Long = 0
)
