package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached report card data.
 */
@Entity(
    tableName = "report_cards",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["studentId", "schoolId"]),
        Index(value = ["termId", "schoolId"])
    ]
)
data class ReportCardEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val studentId: String,
    val studentName: String,
    val termId: String,
    val termName: String,
    val academicYear: String,
    val gpa: Float? = null,
    val rank: Int? = null,
    val totalStudents: Int? = null,
    val status: String = "Published",
    val overallRemarks: String? = null,
    val lastSyncedAt: Instant
)

/**
 * Room entity for subject reports within a report card.
 */
@Entity(
    tableName = "subject_reports",
    indices = [
        Index(value = ["reportCardId"]),
        Index(value = ["subjectId"])
    ]
)
data class SubjectReportEntity(
    @PrimaryKey val id: String,
    val reportCardId: String,
    val subjectId: String,
    val subjectName: String,
    val teacherName: String = "",
    val marks: Float,
    val maxMarks: Float,
    val grade: String,
    val percentage: Float,
    val remarks: String? = null
)
