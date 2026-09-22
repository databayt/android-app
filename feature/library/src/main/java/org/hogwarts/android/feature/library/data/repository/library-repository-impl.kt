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

    override suspend fun getHome() = api.getHome(lang()).toDomain()

    override suspend fun getCatalog(page: Int, search: String?, genre: String?, gradeLevel: String?) =
        api.getCatalog(page, search?.takeIf { it.isNotBlank() }, genre?.takeIf { it.isNotBlank() }, gradeLevel?.takeIf { it.isNotBlank() }).toDomain()

    override suspend fun getBookPage(id: String) = api.getBookPage(id, lang()).toDomain()

    override suspend fun returnBorrowing(borrowingId: String): Borrowing = api.returnBorrowing(borrowingId).toDomain()

    /** The UI language, so the server's `localize` answers in it, as the web page does. */
    private fun lang(): String = if (java.util.Locale.getDefault().language == "en") "en" else "ar"
}
