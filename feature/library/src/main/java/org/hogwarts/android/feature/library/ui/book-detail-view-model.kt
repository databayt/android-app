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
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.usecase.BorrowBookUseCase
import org.hogwarts.android.feature.library.domain.usecase.GetBookDetailUseCase
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = false,
    val isBorrowing: Boolean = false,
    val borrowSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val borrowBookUseCase: BorrowBookUseCase
) : ViewModel() {

    private val bookId: String = savedStateHandle["bookId"] ?: ""

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        loadBookDetail()
    }

    private fun loadBookDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getBookDetailUseCase(bookId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, book = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun borrowBook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBorrowing = true, error = null, borrowSuccess = false) }
            when (val result = borrowBookUseCase(bookId)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isBorrowing = false,
                            borrowSuccess = true,
                            book = state.book?.copy(
                                availableCopies = (state.book.availableCopies - 1).coerceAtLeast(0)
                            )
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isBorrowing = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearBorrowSuccess() {
        _uiState.update { it.copy(borrowSuccess = false) }
    }
}
