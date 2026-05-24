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
import org.hogwarts.android.feature.teacher.domain.model.Assessment
import org.hogwarts.android.feature.teacher.domain.model.ClassStudent
import org.hogwarts.android.feature.teacher.domain.model.GradeEntry
import org.hogwarts.android.feature.teacher.domain.usecase.GetAssessmentsUseCase
import org.hogwarts.android.feature.teacher.domain.usecase.GetClassStudentsUseCase
import org.hogwarts.android.feature.teacher.domain.usecase.SubmitBatchGradesUseCase
import javax.inject.Inject

data class GradeEntryUiState(
    val assessments: List<Assessment> = emptyList(),
    val selectedAssessmentId: String? = null,
    val studentGrades: List<Pair<ClassStudent, GradeEntry>> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GradeEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getClassStudentsUseCase: GetClassStudentsUseCase,
    private val getAssessmentsUseCase: GetAssessmentsUseCase,
    private val submitBatchGradesUseCase: SubmitBatchGradesUseCase
) : ViewModel() {

    private val classId: String = savedStateHandle["classId"] ?: ""

    private val _uiState = MutableStateFlow(GradeEntryUiState())
    val uiState: StateFlow<GradeEntryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load assessments
            when (val result = getAssessmentsUseCase(classId)) {
                is Result.Success -> _uiState.update { it.copy(assessments = result.data) }
                is Result.Error -> _uiState.update { it.copy(error = result.exception.message) }
                is Result.Loading -> {}
            }

            // Load students
            when (val result = getClassStudentsUseCase(classId)) {
                is Result.Success -> {
                    val grades = result.data.map { student ->
                        student to GradeEntry(studentId = student.id)
                    }
                    _uiState.update { it.copy(isLoading = false, studentGrades = grades) }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectAssessment(assessmentId: String) {
        _uiState.update { it.copy(selectedAssessmentId = assessmentId) }
    }

    fun setGrade(studentId: String, marks: Float?) {
        _uiState.update { state ->
            state.copy(
                studentGrades = state.studentGrades.map { (student, grade) ->
                    if (student.id == studentId) student to grade.copy(marks = marks)
                    else student to grade
                }
            )
        }
    }

    fun submit(onSuccess: () -> Unit) {
        val assessmentId = _uiState.value.selectedAssessmentId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val grades = _uiState.value.studentGrades.map { it.second }

            when (val result = submitBatchGradesUseCase(classId, assessmentId, grades)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
