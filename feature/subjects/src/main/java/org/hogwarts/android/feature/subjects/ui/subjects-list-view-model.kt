package org.hogwarts.android.feature.subjects.ui

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
import org.hogwarts.android.feature.subjects.domain.model.Subject
import org.hogwarts.android.feature.subjects.domain.usecase.GetSubjectsUseCase
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

data class SubjectsListUiState(
    val isLoading: Boolean = true,
    val subjects: List<Subject> = emptyList(),
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SubjectsListViewModel @Inject constructor(
    private val getSubjectsUseCase: GetSubjectsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectsListUiState())
    val uiState: StateFlow<SubjectsListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val query = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
                val subjects = getSubjectsUseCase(search = query).sortedByGradeThenName()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        subjects = subjects,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load subjects",
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadSubjects()
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadSubjects()
    }
}

/**
 * Sort mirrors catalog-subjects-grid.tsx: lowest grade first, then name
 * via a locale-aware collator so Arabic names order correctly under RTL.
 */
private fun List<Subject>.sortedByGradeThenName(): List<Subject> {
    val collator = Collator.getInstance(Locale.getDefault()).apply {
        strength = Collator.SECONDARY
    }
    return sortedWith(
        compareBy<Subject> { it.primaryGrade }
            .thenComparator { a, b -> collator.compare(a.name, b.name) },
    )
}
