package org.hogwarts.android.feature.grades.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.grades.domain.model.AssessmentType
import org.hogwarts.android.feature.grades.domain.model.GradeRecord
import org.hogwarts.android.feature.grades.domain.usecase.GetGradesUseCase
import javax.inject.Inject

/**
 * ViewModel for the Grades screen.
 */
@HiltViewModel
class GradesViewModel @Inject constructor(
    private val getGradesUseCase: GetGradesUseCase,
    private val tenantContext: TenantContext
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    init {
        loadGrades()
    }

    private fun loadGrades() {
        val userId = tenantContext.userId ?: return

        viewModelScope.launch {
            getGradesUseCase(studentId = userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                records = resource.data ?: emptyList()
                            )
                        }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = resource.data ?: emptyList(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = resource.data ?: emptyList(),
                                error = resource.error?.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun setFilter(filter: GradesFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun getFilteredRecords(): List<GradeRecord> {
        val state = _uiState.value
        return when (state.selectedFilter) {
            GradesFilter.ALL -> state.records
            GradesFilter.EXAM -> state.records.filter { it.assessmentType == AssessmentType.EXAM }
            GradesFilter.QUIZ -> state.records.filter { it.assessmentType == AssessmentType.QUIZ }
            GradesFilter.ASSIGNMENT -> state.records.filter { it.assessmentType == AssessmentType.ASSIGNMENT }
            GradesFilter.MIDTERM -> state.records.filter { it.assessmentType == AssessmentType.MIDTERM }
            GradesFilter.FINAL -> state.records.filter { it.assessmentType == AssessmentType.FINAL }
        }
    }

    fun retry() {
        loadGrades()
    }
}
