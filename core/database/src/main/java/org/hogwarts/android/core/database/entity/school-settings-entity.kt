package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "school_settings",
    indices = [
        Index(value = ["schoolId"])
    ]
)
data class SchoolSettingsEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val name: String,
    val domain: String,
    val logoUrl: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null,
    val subscription: String? = null,
    val academicYear: String? = null,
    val activeTerms: String? = null,
    val lastSyncedAt: Long
)

@Entity(
    tableName = "academic_years",
    indices = [
        Index(value = ["schoolId"])
    ]
)
data class AcademicYearEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean = false,
    val terms: String? = null,
    val lastSyncedAt: Long
)
