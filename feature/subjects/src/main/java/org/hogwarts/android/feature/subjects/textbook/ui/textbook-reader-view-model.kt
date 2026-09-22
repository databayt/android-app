package org.hogwarts.android.feature.subjects.textbook.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.subjects.textbook.data.ReaderAnchor
import org.hogwarts.android.feature.subjects.textbook.data.ReaderPrefs
import org.hogwarts.android.feature.subjects.textbook.data.ReaderPrefsStore
import org.hogwarts.android.feature.subjects.textbook.data.TextbookRepository
import org.hogwarts.android.feature.subjects.textbook.domain.TextbookLoad
import android.util.Log
import javax.inject.Inject

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Loaded(val load: TextbookLoad, val anchor: ReaderAnchor?) : ReaderUiState
}

@HiltViewModel
class TextbookReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TextbookRepository,
    private val store: ReaderPrefsStore,
) : ViewModel() {
    val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _state = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    val prefs: StateFlow<ReaderPrefs?> = store.prefs.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val bookmarks: StateFlow<List<Int>> = store.bookmarks(slug).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            val load = try {
                repository.load(slug, pdfUrl = null)
            } catch (e: Exception) {
                Log.w("TextbookReader", "Textbook $slug failed to load", e)
                TextbookLoad.Unavailable(null)
            }
            _state.value = ReaderUiState.Loaded(load, store.anchor(slug))
        }
    }

    fun updatePrefs(transform: (ReaderPrefs) -> ReaderPrefs) {
        viewModelScope.launch { store.update(transform) }
    }

    fun saveAnchor(anchor: ReaderAnchor) {
        viewModelScope.launch { store.saveAnchor(slug, anchor) }
    }

    /** Adds or removes the bookmark on PDF [page]; true when it was added. */
    fun toggleBookmark(page: Int): Boolean {
        val current = bookmarks.value
        val added = page !in current
        val next = if (added) (current + page).sorted() else current - page
        viewModelScope.launch { store.saveBookmarks(slug, next) }
        return added
    }
}
