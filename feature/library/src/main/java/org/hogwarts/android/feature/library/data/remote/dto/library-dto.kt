package org.hogwarts.android.feature.library.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.BookCategory
import org.hogwarts.android.feature.library.domain.model.Borrowing
import org.hogwarts.android.feature.library.domain.model.BorrowingStatus
import java.time.LocalDate

@Serializable
data class BookDto(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val category: String,
    val description: String,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("available_copies") val availableCopies: Int,
    @SerialName("total_copies") val totalCopies: Int,
    @SerialName("shelf_location") val shelfLocation: String,
    @SerialName("section_name") val sectionName: String
) {
    fun toDomain() = Book(
        id = id,
        title = title,
        author = author,
        isbn = isbn,
        category = BookCategory.fromString(category),
        description = description,
        coverImageUrl = coverImageUrl,
        availableCopies = availableCopies,
        totalCopies = totalCopies,
        shelfLocation = shelfLocation,
        sectionName = sectionName
    )
}

@Serializable
data class BookListResponse(
    val data: List<BookDto>,
    val total: Int? = null,
    val page: Int? = null
)

@Serializable
data class BorrowingDto(
    val id: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("book_title") val bookTitle: String,
    @SerialName("user_id") val userId: String,
    @SerialName("borrowed_date") val borrowedDate: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("returned_date") val returnedDate: String? = null,
    val status: String,
    val fine: Double? = null
) {
    fun toDomain() = Borrowing(
        id = id,
        bookId = bookId,
        bookTitle = bookTitle,
        userId = userId,
        borrowedDate = LocalDate.parse(borrowedDate),
        dueDate = LocalDate.parse(dueDate),
        returnedDate = returnedDate?.let { LocalDate.parse(it) },
        status = BorrowingStatus.fromString(status),
        fine = fine
    )
}

@Serializable
data class BorrowingListResponse(
    val data: List<BorrowingDto>
)
