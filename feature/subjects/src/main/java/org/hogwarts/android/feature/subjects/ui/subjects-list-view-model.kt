package org.hogwarts.android.feature.subjects.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.subjects.domain.model.Subject
import org.hogwarts.android.feature.subjects.domain.model.SubjectLevel
import org.hogwarts.android.feature.subjects.domain.usecase.GetSubjectsUseCase
import javax.inject.Inject

data class SubjectsListUiState(
    val isLoading: Boolean = true,
    val subjects: List<Subject> = emptyList(),
    /** The stages the school runs — the level tabs need two or more. */
    val schoolLevels: Set<SubjectLevel> = emptySet(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

/**
 * The list arrives already in the web grid's order (lowest grade, then name
 * in the reader's language), sorted server-side so every client agrees. It
 * is not re-sorted here: a second collator is a second chance to disagree.
 *
 * No search. `/subjects` has none, for any role.
 */
@HiltViewModel
class SubjectsListViewModel @Inject constructor(
    private val getSubjectsUseCase: GetSubjectsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectsListUiState())
    val uiState: StateFlow<SubjectsListUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val catalog = getSubjectsUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        subjects = catalog.subjects,
                        schoolLevels = catalog.schoolLevels,
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

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadSubjects()
    }
}
