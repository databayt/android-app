package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "exams",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["schoolId", "date"]),
        Index(value = ["schoolId", "status"])
    ]
)
data class ExamEntity(
    @PrimaryKey
    val id: String,
    val schoolId: String,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val venue: String? = null,
    val instructions: String? = null,
    val type: String = "WRITTEN",
    val status: String = "UPCOMING",
    val maxMarks: Int? = null,
    val passingMarks: Int? = null,
    val marksObtained: Double? = null,
    val grade: String? = null,
    val remarks: String? = null,
    val isPassed: Boolean? = null,
    val lastSyncAt: Instant
)
