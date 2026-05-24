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
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.students.domain.model.Student
import org.hogwarts.android.feature.students.domain.model.StudentStatus
import org.hogwarts.android.feature.students.domain.usecase.CreateStudentUseCase
import org.hogwarts.android.feature.students.domain.usecase.GetStudentDetailUseCase
import org.hogwarts.android.feature.students.domain.usecase.UpdateStudentUseCase
import javax.inject.Inject

data class StudentFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val studentId: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val classId: String = "",
    val section: String = "",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val status: String = "ACTIVE"
)

@HiltViewModel
class StudentFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStudentDetailUseCase: GetStudentDetailUseCase,
    private val createStudentUseCase: CreateStudentUseCase,
    private val updateStudentUseCase: UpdateStudentUseCase
) : ViewModel() {

    private val studentId: String? = savedStateHandle["studentId"]

    private val _uiState = MutableStateFlow(
        StudentFormUiState(isEditMode = studentId != null, studentId = studentId)
    )
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    init {
        if (studentId != null) {
            loadStudent(studentId)
        }
    }

    private fun loadStudent(id: String) {
        viewModelScope.launch {
            getStudentDetailUseCase(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val student = resource.data
                        if (student != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    firstName = student.firstName,
                                    lastName = student.lastName,
                                    email = student.email ?: "",
                                    phone = student.phone ?: "",
                                    gender = student.gender ?: "",
                                    classId = student.classId ?: "",
                                    section = student.section ?: "",
                                    guardianName = student.guardianName ?: "",
                                    guardianPhone = student.guardianPhone ?: "",
                                    status = student.status.name
                                )
                            }
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.error?.message)
                    }
                }
            }
        }
    }

    fun onFirstNameChanged(value: String) = _uiState.update { it.copy(firstName = value) }
    fun onLastNameChanged(value: String) = _uiState.update { it.copy(lastName = value) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value) }
    fun onPhoneChanged(value: String) = _uiState.update { it.copy(phone = value) }
    fun onGenderChanged(value: String) = _uiState.update { it.copy(gender = value) }
    fun onClassIdChanged(value: String) = _uiState.update { it.copy(classId = value) }
    fun onSectionChanged(value: String) = _uiState.update { it.copy(section = value) }
    fun onGuardianNameChanged(value: String) = _uiState.update { it.copy(guardianName = value) }
    fun onGuardianPhoneChanged(value: String) = _uiState.update { it.copy(guardianPhone = value) }
    fun onStatusChanged(value: String) = _uiState.update { it.copy(status = value) }

    fun onSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val state = _uiState.value
                val student = Student(
                    id = state.studentId ?: "",
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email.takeIf { it.isNotBlank() },
                    phone = state.phone.takeIf { it.isNotBlank() },
                    gender = state.gender.takeIf { it.isNotBlank() },
                    classId = state.classId.takeIf { it.isNotBlank() },
                    section = state.section.takeIf { it.isNotBlank() },
                    guardianName = state.guardianName.takeIf { it.isNotBlank() },
                    guardianPhone = state.guardianPhone.takeIf { it.isNotBlank() },
                    status = StudentStatus.fromString(state.status)
                )
                if (state.isEditMode) {
                    updateStudentUseCase(student)
                } else {
                    createStudentUseCase(student)
                }
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
