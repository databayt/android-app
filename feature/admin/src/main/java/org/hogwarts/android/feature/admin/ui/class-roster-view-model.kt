package org.hogwarts.android.feature.admin.ui

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
import org.hogwarts.android.feature.admin.domain.model.ClassRoster
import org.hogwarts.android.feature.admin.domain.usecase.AddStudentToClassUseCase
import org.hogwarts.android.feature.admin.domain.usecase.GetClassRosterUseCase
import org.hogwarts.android.feature.admin.domain.usecase.GetClassRostersUseCase
import org.hogwarts.android.feature.admin.domain.usecase.RemoveStudentFromClassUseCase
import javax.inject.Inject

data class ClassRosterUiState(
    val rosters: List<ClassRoster> = emptyList(),
    val selectedRoster: ClassRoster? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null,
    val actionError: String? = null
)

@HiltViewModel
class ClassRosterViewModel @Inject constructor(
    private val getClassRostersUseCase: GetClassRostersUseCase,
    private val getClassRosterUseCase: GetClassRosterUseCase,
    private val addStudentToClassUseCase: AddStudentToClassUseCase,
    private val removeStudentFromClassUseCase: RemoveStudentFromClassUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classId: String? = savedStateHandle["classId"]

    private val _uiState = MutableStateFlow(ClassRosterUiState())
    val uiState: StateFlow<ClassRosterUiState> = _uiState.asStateFlow()

    init {
        if (classId != null) {
            loadClassRoster(classId)
        } else {
            loadAllRosters()
        }
    }

    private fun loadAllRosters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getClassRostersUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, rosters = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    private fun loadClassRoster(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getClassRosterUseCase(id)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, selectedRoster = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectClass(roster: ClassRoster) {
        loadClassRoster(roster.classId)
    }

    fun addStudent(studentId: String) {
        val currentClassId = _uiState.value.selectedRoster?.classId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            when (val result = addStudentToClassUseCase(currentClassId, studentId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadClassRoster(currentClassId)
                }
                is Result.Error -> _uiState.update { it.copy(isActionLoading = false, actionError = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun removeStudent(studentId: String) {
        val currentClassId = _uiState.value.selectedRoster?.classId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            when (val result = removeStudentFromClassUseCase(currentClassId, studentId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadClassRoster(currentClassId)
                }
                is Result.Error -> _uiState.update { it.copy(isActionLoading = false, actionError = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedRoster = null) }
    }

    fun refresh() {
        if (classId != null || _uiState.value.selectedRoster != null) {
            loadClassRoster(classId ?: _uiState.value.selectedRoster!!.classId)
        } else {
            loadAllRosters()
        }
    }
}
