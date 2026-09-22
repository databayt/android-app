package org.hogwarts.android.feature.lumos.ui

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
import org.hogwarts.android.feature.lumos.data.repository.LumosRepository
import org.hogwarts.android.feature.lumos.domain.model.CatalogCourse
import org.hogwarts.android.feature.lumos.domain.model.LumosCoursesPage
import timber.log.Timber
import javax.inject.Inject

/** How many of the grade's courses lead as the shelf, before the grid. */
const val RECOMMENDED_COUNT = 6

/** How many of the remaining courses the grid shows before "See more". */
const val OTHERS_PAGE_SIZE = 12

data class LumosCoursesUiState(
    val isLoading: Boolean = true,
    val page: LumosCoursesPage? = null,
    /** The grade the reader picked; null lets the server choose (their own). */
    val level: Int? = null,
    val visibleOthers: Int = OTHERS_PAGE_SIZE,
    /** The submitted search — non-blank leaves the browse view for the grid. */
    val searchQuery: String = "",
    val searchResults: List<CatalogCourse> = emptyList(),
    val searchTotal: Int = 0,
    val searchPage: Int = 1,
    /** Typeahead inside the search sheet, before anything is submitted. */
    val typeahead: List<CatalogCourse> = emptyList(),
    val error: String? = null,
)

/**
 * `/lumos/courses`. The browse view is one read per grade — the web keeps the
 * grade in `?level=` and re-renders; the phone re-asks — and a submitted
 * search swaps it for the paginated grid until it is cleared.
 */
@HiltViewModel
class LumosCoursesViewModel @Inject constructor(
    private val repository: LumosRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LumosCoursesUiState())
    val uiState: StateFlow<LumosCoursesUiState> = _uiState.asStateFlow()

    private var typeaheadJob: Job? = null

    init { loadBrowse(level = null) }

    fun onGradeSelected(grade: Int) {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
        loadBrowse(level = grade)
    }

    fun refresh() {
        val state = _uiState.value
        if (state.searchQuery.isNotBlank()) submitSearch(state.searchQuery) else loadBrowse(state.level)
    }

    fun onSeeMoreOthers() {
        _uiState.update { it.copy(visibleOthers = it.visibleOthers + OTHERS_PAGE_SIZE) }
    }

    /** Typeahead while the sheet is open: debounced, results only. */
    fun onTypeahead(query: String) {
        typeaheadJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(typeahead = emptyList()) }
            return
        }
        typeaheadJob = viewModelScope.launch {
            delay(300)
            runCatching { repository.getCoursesPage(search = query) }
                .onSuccess { page -> _uiState.update { it.copy(typeahead = page.search.courses) } }
                .onFailure { Timber.w(it, "Lumos typeahead failed") }
        }
    }

    fun submitSearch(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return clearSearch()
        _uiState.update { it.copy(isLoading = true, searchQuery = q, searchPage = 1) }
        viewModelScope.launch {
            runCatching { repository.getCoursesPage(level = _uiState.value.level, search = q, page = 1) }
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = page.search.courses,
                            searchTotal = page.search.total,
                            error = null,
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun loadMoreResults() {
        val state = _uiState.value
        val next = state.searchPage + 1
        viewModelScope.launch {
            runCatching { repository.getCoursesPage(level = state.level, search = state.searchQuery, page = next) }
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(searchResults = it.searchResults + page.search.courses, searchPage = next)
                    }
                }
                .onFailure { Timber.w(it, "Lumos search page failed") }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), searchTotal = 0) }
        if (_uiState.value.page == null) loadBrowse(_uiState.value.level)
    }

    private fun loadBrowse(level: Int?) {
        _uiState.update { it.copy(isLoading = true, level = level, visibleOthers = OTHERS_PAGE_SIZE) }
        viewModelScope.launch {
            runCatching { repository.getCoursesPage(level = level) }
                .onSuccess { page -> _uiState.update { it.copy(isLoading = false, page = page, error = null) } }
                .onFailure { e ->
                    Timber.w(e, "Lumos courses failed to load")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
