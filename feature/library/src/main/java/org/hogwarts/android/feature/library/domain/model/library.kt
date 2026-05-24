package org.hogwarts.android.feature.library.domain.model

import java.time.LocalDate

/**
 * Domain model representing a book in the school library.
 */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val category: BookCategory,
    val description: String,
    val coverImageUrl: String?,
    val availableCopies: Int,
    val totalCopies: Int,
    val shelfLocation: String,
    val sectionName: String
) {
    val isAvailable: Boolean get() = availableCopies > 0
}

/**
 * Book categories for filtering.
 */
enum class BookCategory {
    FICTION,
    NON_FICTION,
    SCIENCE,
    MATHEMATICS,
    HISTORY,
    LITERATURE,
    REFERENCE,
    OTHER;

    companion object {
        fun fromString(value: String): BookCategory =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }

    fun displayName(): String = when (this) {
        FICTION -> "Fiction"
        NON_FICTION -> "Non-Fiction"
        SCIENCE -> "Science"
        MATHEMATICS -> "Mathematics"
        HISTORY -> "History"
        LITERATURE -> "Literature"
        REFERENCE -> "Reference"
        OTHER -> "Other"
    }
}

/**
 * Domain model representing a book borrowing record.
 */
data class Borrowing(
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val userId: String,
    val borrowedDate: LocalDate,
    val dueDate: LocalDate,
    val returnedDate: LocalDate?,
    val status: BorrowingStatus,
    val fine: Double?
) {
    val isOverdue: Boolean get() = status == BorrowingStatus.OVERDUE
    val isActive: Boolean get() = status == BorrowingStatus.ACTIVE || status == BorrowingStatus.RENEWED
}

/**
 * Borrowing status enum.
 */
enum class BorrowingStatus {
    ACTIVE,
    RETURNED,
    OVERDUE,
    RENEWED;

    companion object {
        fun fromString(value: String): BorrowingStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }

    fun displayName(): String = when (this) {
        ACTIVE -> "Active"
        RETURNED -> "Returned"
        OVERDUE -> "Overdue"
        RENEWED -> "Renewed"
    }
}
