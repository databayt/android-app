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
import org.hogwarts.android.feature.teacher.domain.model.AttendanceMark
import org.hogwarts.android.feature.teacher.domain.model.AttendanceStatus
import org.hogwarts.android.feature.teacher.domain.model.ClassStudent
import org.hogwarts.android.feature.teacher.domain.usecase.GetClassStudentsUseCase
import org.hogwarts.android.feature.teacher.domain.usecase.SubmitBatchAttendanceUseCase
import java.time.LocalDate
import javax.inject.Inject

data class BatchAttendanceUiState(
    val studentMarks: List<Pair<ClassStudent, AttendanceMark>> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class BatchAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getClassStudentsUseCase: GetClassStudentsUseCase,
    private val submitBatchAttendanceUseCase: SubmitBatchAttendanceUseCase
) : ViewModel() {

    private val classId: String = savedStateHandle["classId"] ?: ""

    private val _uiState = MutableStateFlow(BatchAttendanceUiState())
    val uiState: StateFlow<BatchAttendanceUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getClassStudentsUseCase(classId)) {
                is Result.Success -> {
                    val marks = result.data.map { student ->
                        student to AttendanceMark(studentId = student.id, status = AttendanceStatus.PRESENT)
                    }
                    _uiState.update { it.copy(isLoading = false, studentMarks = marks) }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun setMark(studentId: String, status: AttendanceStatus) {
        _uiState.update { state ->
            state.copy(
                studentMarks = state.studentMarks.map { (student, mark) ->
                    if (student.id == studentId) student to mark.copy(status = status)
                    else student to mark
                }
            )
        }
    }

    fun markAllPresent() {
        _uiState.update { state ->
            state.copy(
                studentMarks = state.studentMarks.map { (student, mark) ->
                    student to mark.copy(status = AttendanceStatus.PRESENT)
                }
            )
        }
    }

    fun submit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val marks = _uiState.value.studentMarks.map { it.second }
            val today = LocalDate.now().toString()

            when (val result = submitBatchAttendanceUseCase(classId, today, null, marks)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
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
