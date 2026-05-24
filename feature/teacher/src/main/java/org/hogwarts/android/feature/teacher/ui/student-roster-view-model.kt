package org.hogwarts.android.feature.teacher.ui

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
import org.hogwarts.android.feature.teacher.domain.model.ClassStudent
import org.hogwarts.android.feature.teacher.domain.usecase.GetClassStudentsUseCase
import javax.inject.Inject

data class StudentRosterUiState(
    val allStudents: List<ClassStudent> = emptyList(),
    val filteredStudents: List<ClassStudent> = emptyList(),
    val searchQuery: String = "",
    val sortOption: StudentSortOption = StudentSortOption.NAME_AZ,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StudentRosterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getClassStudentsUseCase: GetClassStudentsUseCase
) : ViewModel() {

    private val classId: String = savedStateHandle["classId"] ?: ""

    private val _uiState = MutableStateFlow(StudentRosterUiState())
    val uiState: StateFlow<StudentRosterUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getClassStudentsUseCase(classId)) {
                is Result.Success -> {
                    val students = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allStudents = students,
                            filteredStudents = applyFilters(students, it.searchQuery, it.sortOption)
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredStudents = applyFilters(state.allStudents, query, state.sortOption)
            )
        }
    }

    fun setSortOption(option: StudentSortOption) {
        _uiState.update { state ->
            state.copy(
                sortOption = option,
                filteredStudents = applyFilters(state.allStudents, state.searchQuery, option)
            )
        }
    }

    private fun applyFilters(
        students: List<ClassStudent>,
        query: String,
        sort: StudentSortOption
    ): List<ClassStudent> {
        val filtered = if (query.isBlank()) students
        else students.filter {
            it.displayName.contains(query, ignoreCase = true) ||
                (it.studentNumber?.contains(query, ignoreCase = true) == true)
        }

        return when (sort) {
            StudentSortOption.NAME_AZ -> filtered.sortedBy { it.displayName }
            StudentSortOption.NAME_ZA -> filtered.sortedByDescending { it.displayName }
            StudentSortOption.ATTENDANCE -> filtered.sortedByDescending { it.attendanceRate }
            StudentSortOption.GRADE -> filtered.sortedByDescending { it.latestGrade }
        }
    }
}
