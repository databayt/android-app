package org.hogwarts.android.feature.library.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.library.data.repository.LibraryRepository
import org.hogwarts.android.feature.library.domain.model.BookPage
import java.time.LocalDate
import javax.inject.Inject

/** The alert the web's `BorrowAlert` shows after a borrow or a return. */
sealed interface BorrowNotice {
    data class Borrowed(val dueDate: LocalDate) : BorrowNotice
    data object Returned : BorrowNotice
    /** [message] is kept for logs; the alert says the web's own sentence. */
    data class BorrowFailed(val message: String?) : BorrowNotice
    data class ReturnFailed(val message: String?) : BorrowNotice
}

data class BookDetailUiState(
    val page: BookPage? = null,
    val isLoading: Boolean = false,
    /** A borrow or a return is on its way — the pill says so. */
    val isWorking: Boolean = false,
    val notice: BorrowNotice? = null,
    val error: String? = null,
)

/**
 * `/library/books/[id]`, from the page's own loader (`book-detail/load.ts`).
 * Borrowing and returning go through the mobile routes that enforce the
 * actions' rules, then the page reloads, as the web's `router.refresh()` does.
 */
@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository,
) : ViewModel() {

    private val bookId: String = savedStateHandle["bookId"] ?: ""

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load(quiet: Boolean = false) {
        viewModelScope.launch {
            if (!quiet) _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getBookPage(bookId) }
                .onSuccess { page -> _uiState.update { it.copy(isLoading = false, page = page) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun borrow() {
        val page = _uiState.value.page ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true) }
            runCatching { repository.borrowBook(page.schoolBookId) }
                .onSuccess { loan ->
                    _uiState.update { it.copy(isWorking = false, notice = BorrowNotice.Borrowed(loan.dueDate)) }
                    load(quiet = true)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isWorking = false, notice = BorrowNotice.BorrowFailed(e.message)) }
                }
        }
    }

    fun giveBack() {
        val record = _uiState.value.page?.borrowRecordId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true) }
            runCatching { repository.returnBorrowing(record) }
                .onSuccess {
                    _uiState.update { it.copy(isWorking = false, notice = BorrowNotice.Returned) }
                    load(quiet = true)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isWorking = false, notice = BorrowNotice.ReturnFailed(e.message)) }
                }
        }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(notice = null) }
    }
}
