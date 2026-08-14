package org.hogwarts.android.feature.lumos.ui

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
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.usecase.GetCoursesUseCase
import javax.inject.Inject

data class CourseCatalogUiState(
    val isLoading: Boolean = false,
    val courses: List<Course> = emptyList(),
    val filteredCourses: List<Course> = emptyList(),
    val activeGrade: Int? = 1,
    val gradeFilterLocked: Boolean = false,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class CourseCatalogViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialGrade: Int? = savedStateHandle.get<Int>("initialGrade")
    private val lockGrade: Boolean = savedStateHandle.get<Boolean>("lockGrade") ?: false

    private val _uiState = MutableStateFlow(
        CourseCatalogUiState(
            isLoading = true,
            activeGrade = if (lockGrade) initialGrade else (initialGrade ?: 1),
            gradeFilterLocked = lockGrade
        )
    )
    val uiState: StateFlow<CourseCatalogUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
    }

    fun selectGrade(grade: Int?) {
        if (_uiState.value.gradeFilterLocked) return
        _uiState.update { it.copy(activeGrade = grade) }
        applyFilters()
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun refresh() {
        loadCourses()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getCoursesUseCase()
            if (result is Result.Success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        courses = result.data
                    )
                }
                applyFilters()
            } else if (result is Result.Error) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
            }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.courses

        if (state.activeGrade != null) {
            filtered = filtered.filter { course ->
                course.grades.isEmpty() || course.grades.contains(state.activeGrade)
            }
        }

        if (!state.selectedCategory.isNullOrBlank()) {
            filtered = filtered.filter { course ->
                course.category.equals(state.selectedCategory, ignoreCase = true)
            }
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.trim()
            filtered = filtered.filter { course ->
                course.title.contains(query, ignoreCase = true) ||
                    course.description.contains(query, ignoreCase = true) ||
                    course.instructorName.contains(query, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(filteredCourses = filtered) }
    }
}
