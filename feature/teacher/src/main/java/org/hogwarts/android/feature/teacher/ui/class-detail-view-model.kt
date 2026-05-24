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
import org.hogwarts.android.feature.teacher.domain.model.TeacherClass
import org.hogwarts.android.feature.teacher.domain.usecase.GetClassStudentsUseCase
import org.hogwarts.android.feature.teacher.domain.usecase.GetMyClassesUseCase
import javax.inject.Inject

data class ClassDetailUiState(
    val teacherClass: TeacherClass? = null,
    val students: List<ClassStudent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMyClassesUseCase: GetMyClassesUseCase,
    private val getClassStudentsUseCase: GetClassStudentsUseCase
) : ViewModel() {

    private val classId: String = savedStateHandle["classId"] ?: ""

    private val _uiState = MutableStateFlow(ClassDetailUiState())
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    init {
        loadClassDetail()
    }

    private fun loadClassDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load class info
            when (val classesResult = getMyClassesUseCase()) {
                is Result.Success -> {
                    val tc = classesResult.data.find { it.id == classId }
                    _uiState.update { it.copy(teacherClass = tc) }
                }
                is Result.Error -> _uiState.update { it.copy(error = classesResult.exception.message) }
                is Result.Loading -> {}
            }

            // Load students
            when (val studentsResult = getClassStudentsUseCase(classId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, students = studentsResult.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = studentsResult.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
