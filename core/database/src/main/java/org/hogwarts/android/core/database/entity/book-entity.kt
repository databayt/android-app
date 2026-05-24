package org.hogwarts.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for cached book catalog data.
 */
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["category", "schoolId"])
    ]
)
data class BookEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val title: String,
    val author: String,
    val isbn: String = "",
    val category: String,
    val description: String = "",
    val coverImageUrl: String? = null,
    val availableCopies: Int = 0,
    val totalCopies: Int = 0,
    val shelfLocation: String? = null,
    val sectionName: String? = null,
    val lastSyncedAt: Instant
)

/**
 * Room entity for borrowing records.
 */
@Entity(
    tableName = "borrowings",
    indices = [
        Index(value = ["schoolId"]),
        Index(value = ["userId", "schoolId"]),
        Index(value = ["bookId", "schoolId"])
    ]
)
data class BorrowingEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val bookId: String,
    val bookTitle: String,
    val userId: String,
    val borrowedDate: String,
    val dueDate: String,
    val returnedDate: String? = null,
    val status: String,
    val fine: Double? = null,
    val lastSyncedAt: Instant
)
