package org.hogwarts.android.feature.library.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.library.data.remote.LibraryApi
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.Borrowing
import javax.inject.Inject

/**
 * Library repository implementation with multi-tenant isolation.
 * All API calls include schoolId from TenantContext.
 */
class LibraryRepositoryImpl @Inject constructor(
    private val api: LibraryApi,
    private val tenantContext: TenantContext
) : LibraryRepository {

    override suspend fun getBooks(category: String?, search: String?): List<Book> {
        return api.getBooks(
            category = category,
            search = search
        ).data.map { it.toDomain() }
    }

    override suspend fun getBookDetail(bookId: String): Book {
        return api.getBookDetail(bookId).toDomain()
    }

    override suspend fun borrowBook(bookId: String): Borrowing {
        return api.borrowBook(bookId).toDomain()
    }

    override suspend fun renewBorrowing(borrowingId: String): Borrowing {
        return api.renewBorrowing(borrowingId).toDomain()
    }

    override suspend fun getMyBorrowings(): List<Borrowing> {
        return api.getMyBorrowings().data.map { it.toDomain() }
    }
}
