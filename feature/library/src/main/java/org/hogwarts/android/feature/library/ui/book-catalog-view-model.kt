package org.hogwarts.android.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.library.data.repository.LibraryRepository
import org.hogwarts.android.feature.library.domain.model.LibraryHome
import javax.inject.Inject

data class BookCatalogUiState(
    val home: LibraryHome? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * `/library` — the page's own shelves, from the same loader the page renders
 * (`library/load.ts`): the school's catalog in the reader's language, the
 * featured book, and the four rows already cut.
 */
@HiltViewModel
class BookCatalogViewModel @Inject constructor(
    private val repository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookCatalogUiState())
    val uiState: StateFlow<BookCatalogUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getHome() }
                .onSuccess { home -> _uiState.update { it.copy(isLoading = false, home = home) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
