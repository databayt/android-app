package org.hogwarts.android.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.BookCategory
import org.hogwarts.android.feature.library.domain.usecase.GetBooksUseCase
import javax.inject.Inject

data class BookCatalogUiState(
    val allBooks: List<Book> = emptyList(),
    val filteredBooks: List<Book> = emptyList(),
    val selectedCategory: BookCategory? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookCatalogViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookCatalogUiState())
    val uiState: StateFlow<BookCatalogUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val category = _uiState.value.selectedCategory?.name
            val search = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            when (val result = getBooksUseCase(category = category, search = search)) {
                is Result.Success -> {
                    val books = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allBooks = books,
                            filteredBooks = books
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun selectCategory(category: BookCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadBooks()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Debounce search input
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadBooks()
        }
    }

    fun refresh() = loadBooks()
}
