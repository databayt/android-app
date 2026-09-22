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
import org.hogwarts.android.feature.library.domain.model.Book
import javax.inject.Inject

data class AllBooksUiState(
    val search: String = "",
    val genre: String = "",
    val gradeLevel: String = "",
    val books: List<Book> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 0,
    val genres: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * `/library/books` — the page's loader (`book-list/load.ts`). A filter change
 * starts again from page one, as the toolbar's `router.replace` does; See More
 * appends the next page.
 */
@HiltViewModel
class AllBooksViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AllBooksUiState(
            search = savedStateHandle.get<String>("search").orEmpty(),
            gradeLevel = savedStateHandle.get<String>("gradeLevel").orEmpty(),
        ),
    )
    val uiState: StateFlow<AllBooksUiState> = _uiState.asStateFlow()

    init {
        load(page = 1)
    }

    fun setSearch(value: String) = refilter { it.copy(search = value.trim()) }
    fun setGenre(value: String) = refilter { it.copy(genre = value) }
    fun setGradeLevel(value: String) = refilter { it.copy(gradeLevel = value) }

    fun seeMore() {
        val s = _uiState.value
        if (s.page < s.totalPages && !s.isLoading) load(page = s.page + 1)
    }

    private fun refilter(change: (AllBooksUiState) -> AllBooksUiState) {
        _uiState.update(change)
        load(page = 1)
    }

    private fun load(page: Int) {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getCatalog(page, s.search, s.genre, s.gradeLevel) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            books = if (page == 1) result.books else it.books + result.books,
                            total = result.total,
                            page = result.page,
                            totalPages = result.totalPages,
                            genres = result.genres,
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
