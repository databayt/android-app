package org.hogwarts.android.feature.students.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.students.domain.model.Student
import org.hogwarts.android.feature.students.domain.usecase.GetStudentDetailUseCase
import javax.inject.Inject

data class StudentDetailUiState(
    val student: Student? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStudentDetailUseCase: GetStudentDetailUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val studentId: String = checkNotNull(savedStateHandle["studentId"])

    private val _uiState = MutableStateFlow(StudentDetailUiState())
    val uiState: StateFlow<StudentDetailUiState> = _uiState.asStateFlow()

    init {
        loadStudent()
    }

    private fun loadStudent() {
        viewModelScope.launch {
            getStudentDetailUseCase(studentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, student = resource.data ?: it.student)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, student = resource.data, error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, student = resource.data ?: it.student, error = resource.error?.message)
                    }
                }
            }
        }
    }
}
