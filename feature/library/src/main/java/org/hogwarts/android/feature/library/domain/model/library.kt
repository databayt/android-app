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
    /**
     * The school's own word for the shelf — "Fiction", "شعر الجاهلية". The
     * web groups its rows on this raw string, not on [category], which is a
     * coarse enum that collapses most of them to OTHER.
     */
    val genre: String = "",
    val description: String,
    val coverImageUrl: String?,
    val availableCopies: Int,
    val totalCopies: Int,
    val shelfLocation: String,
    val sectionName: String,
    /** The jacket's ground when the art is missing (`coverColor`). */
    val coverColor: String? = null,
    val rating: Double = 0.0,
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

/**
 * `/library/books/[id]` — everything the web's book page shows, built by the
 * same server loader (`book-detail/load.ts`).
 */
data class BookPage(
    /** The CATALOG id — what the page and the shelves link by. */
    val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val rating: Double,
    val coverUrl: String?,
    val coverColor: String?,
    /** The eyebrow's word; null for a general-audience book. */
    val gradeLabel: String?,
    val gradeLevel: String?,
    val publicationYear: Int?,
    val pageCount: Int?,
    val digitalFileUrl: String?,
    /** The school's lending copy — what borrowing takes. */
    val schoolBookId: String,
    val availableCopies: Int,
    val totalCopies: Int,
    /** The reader's open loan of this book, if any — what returning takes. */
    val borrowRecordId: String?,
    val about: List<String>,
    /** The Information list, labels already in the reader's language. */
    val info: List<Pair<String, String>>,
    val moreByAuthor: List<Book>,
    val similar: List<Book>,
)

/** One page of `/library/books`. */
data class CatalogPage(
    val books: List<Book>,
    val total: Int,
    val page: Int,
    val totalPages: Int,
    val genres: List<String>,
)

/** `/library`'s featured book and four shelves, as the page cuts them. */
data class LibraryHome(
    val total: Int,
    val featured: Book?,
    val latest: List<Book>,
    val featuredShelf: List<Book>,
    val literature: List<Book>,
    val science: List<Book>,
)
