package org.hogwarts.android.feature.library.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.library.data.repository.LibraryRepository
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.Borrowing
import javax.inject.Inject

class GetBooksUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(
        category: String? = null,
        search: String? = null
    ): Result<List<Book>> {
        return try {
            Result.Success(repository.getBooks(category, search))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetBookDetailUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(bookId: String): Result<Book> {
        return try {
            Result.Success(repository.getBookDetail(bookId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class BorrowBookUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(bookId: String): Result<Borrowing> {
        return try {
            Result.Success(repository.borrowBook(bookId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class RenewBorrowingUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(borrowingId: String): Result<Borrowing> {
        return try {
            Result.Success(repository.renewBorrowing(borrowingId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetMyBorrowingsUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(): Result<List<Borrowing>> {
        return try {
            Result.Success(repository.getMyBorrowings())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
