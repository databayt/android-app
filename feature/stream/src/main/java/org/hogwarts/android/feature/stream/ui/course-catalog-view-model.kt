package org.hogwarts.android.feature.stream.ui

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
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.usecase.GetCoursesUseCase
import javax.inject.Inject

data class CourseCatalogUiState(
    val allCourses: List<Course> = emptyList(),
    val filteredCourses: List<Course> = emptyList(),
    val activeGrade: Int? = DEFAULT_GRADE,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    /** When true the screen hides the grade picker — set by the locked-grade entry path. */
    val gradeFilterLocked: Boolean = false
) {
    companion object {
        /** Grade 1 is selected on first open; users can clear by tapping the active pill. */
        const val DEFAULT_GRADE = 1
    }
}

@HiltViewModel
class CourseCatalogViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Route args from StreamCatalog. Compose Navigation 2.8 populates the
    // SavedStateHandle with each route field by name. Both keys are optional —
    // missing values reproduce today's "browse with grade-1 default" behavior.
    private val lockGrade: Boolean = savedStateHandle["lockGrade"] ?: false
    private val initialGrade: Int? = savedStateHandle["initialGrade"]

    private val _uiState = MutableStateFlow(
        CourseCatalogUiState(
            activeGrade = if (lockGrade) initialGrade else CourseCatalogUiState.DEFAULT_GRADE,
            gradeFilterLocked = lockGrade
        )
    )
    val uiState: StateFlow<CourseCatalogUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getCoursesUseCase()) {
                is Result.Success -> {
                    val courses = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allCourses = courses,
                            filteredCourses = applyFilters(courses, it.activeGrade, it.searchQuery)
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectGrade(grade: Int?) {
        // When the entry path locked the grade (student direct-from-home) we ignore
        // any further selection — the picker isn't even rendered, but a future
        // caller (deep link, programmatic) shouldn't be able to override it either.
        if (_uiState.value.gradeFilterLocked) return
        _uiState.update { state ->
            state.copy(
                activeGrade = grade,
                filteredCourses = applyFilters(state.allCourses, grade, state.searchQuery)
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCourses = applyFilters(state.allCourses, state.activeGrade, query)
            )
        }
    }

    fun refresh() = loadCourses()

    private fun applyFilters(courses: List<Course>, grade: Int?, query: String): List<Course> {
        return courses.filter { course ->
            // A course passes the grade filter if:
            //   - no filter is active, OR
            //   - the selected grade appears in its grades list, OR
            //   - it's ungraded (empty list) — the backend may legitimately omit
            //     grades for catalog-wide subjects, and we'd rather show them
            //     under every pill than blackhole the default view.
            val gradeMatches = grade == null ||
                course.grades.isEmpty() ||
                grade in course.grades

            val queryMatches = query.isBlank() ||
                course.title.contains(query, ignoreCase = true) ||
                course.description.contains(query, ignoreCase = true)

            gradeMatches && queryMatches
        }
    }
}
