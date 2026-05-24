package org.hogwarts.android.feature.library.data.repository

import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.Borrowing

/**
 * Repository interface for library data operations.
 */
interface LibraryRepository {
    suspend fun getBooks(category: String? = null, search: String? = null): List<Book>
    suspend fun getBookDetail(bookId: String): Book
    suspend fun borrowBook(bookId: String): Borrowing
    suspend fun renewBorrowing(borrowingId: String): Borrowing
    suspend fun getMyBorrowings(): List<Borrowing>
}
