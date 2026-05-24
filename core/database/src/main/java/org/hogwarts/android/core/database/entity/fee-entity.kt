package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "fees",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["schoolId", "studentId"]),
        Index(value = ["schoolId", "status"])
    ]
)
data class FeeEntity(
    @PrimaryKey
    val id: String,
    val schoolId: String,
    val studentId: String,
    val studentName: String,
    val description: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: LocalDate,
    val status: String,
    val category: String? = null,
    val term: String? = null,
    val lastSyncAt: Instant,
    val serverVersion: Long = 0
)
