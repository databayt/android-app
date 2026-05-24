package org.hogwarts.android.feature.lessons.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan
import org.hogwarts.android.feature.lessons.domain.usecase.GetLessonPlansUseCase
import javax.inject.Inject

data class LessonPlansUiState(
    val allPlans: List<LessonPlan> = emptyList(),
    val filteredPlans: List<LessonPlan> = emptyList(),
    val selectedClassId: String? = null,
    val selectedSubjectName: String? = null,
    val availableClasses: List<String> = emptyList(),
    val availableSubjects: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LessonPlansViewModel @Inject constructor(
    private val getLessonPlansUseCase: GetLessonPlansUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonPlansUiState())
    val uiState: StateFlow<LessonPlansUiState> = _uiState.asStateFlow()

    init {
        loadLessonPlans()
    }

    private fun loadLessonPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getLessonPlansUseCase(
                classId = _uiState.value.selectedClassId
            )) {
                is Result.Success -> {
                    val plans = result.data
                    val classes = plans.map { it.classId }.distinct()
                    val subjects = plans.map { it.subjectName }.distinct()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allPlans = plans,
                            filteredPlans = filterPlans(plans, it.selectedClassId, it.selectedSubjectName),
                            availableClasses = classes,
                            availableSubjects = subjects
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun selectClass(classId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedClassId = classId,
                filteredPlans = filterPlans(state.allPlans, classId, state.selectedSubjectName)
            )
        }
    }

    fun selectSubject(subjectName: String?) {
        _uiState.update { state ->
            state.copy(
                selectedSubjectName = subjectName,
                filteredPlans = filterPlans(state.allPlans, state.selectedClassId, subjectName)
            )
        }
    }

    fun refresh() = loadLessonPlans()

    private fun filterPlans(
        plans: List<LessonPlan>,
        classId: String?,
        subjectName: String?
    ): List<LessonPlan> {
        return plans
            .let { list -> classId?.let { id -> list.filter { it.classId == id } } ?: list }
            .let { list -> subjectName?.let { name -> list.filter { it.subjectName == name } } ?: list }
    }
}
