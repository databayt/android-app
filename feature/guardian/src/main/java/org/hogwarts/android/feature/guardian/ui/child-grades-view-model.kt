package org.hogwarts.android.feature.guardian.ui

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
import org.hogwarts.android.feature.guardian.data.repository.ChildGradeRecord
import org.hogwarts.android.feature.guardian.domain.model.Child
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildGradesUseCase
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildrenUseCase
import javax.inject.Inject

data class ChildGradesUiState(
    val children: List<Child> = emptyList(),
    val selectedChildId: String? = null,
    val grades: List<ChildGradeRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val averagePercentage: Float
        get() = if (grades.isEmpty()) 0f else grades.map { it.percentage }.average().toFloat()

    val subjectCount: Int
        get() = grades.map { it.subjectId }.distinct().size
}

@HiltViewModel
class ChildGradesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChildrenUseCase: GetChildrenUseCase,
    private val getChildGradesUseCase: GetChildGradesUseCase
) : ViewModel() {

    private val childId: String? = savedStateHandle["childId"]

    private val _uiState = MutableStateFlow(ChildGradesUiState())
    val uiState: StateFlow<ChildGradesUiState> = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            when (val result = getChildrenUseCase()) {
                is Result.Success -> {
                    val selected = childId ?: result.data.firstOrNull()?.id
                    _uiState.update { it.copy(children = result.data, selectedChildId = selected) }
                    selected?.let { loadGrades(it) }
                }
                is Result.Error -> _uiState.update { it.copy(error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun selectChild(id: String) {
        _uiState.update { it.copy(selectedChildId = id) }
        loadGrades(id)
    }

    private fun loadGrades(childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getChildGradesUseCase(childId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, grades = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }
}
