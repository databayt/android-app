package org.hogwarts.android.feature.exams.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.domain.usecase.GetExamDetailUseCase
import javax.inject.Inject

data class ExamDetailUiState(
    val isLoading: Boolean = true,
    val exam: Exam? = null,
    val error: String? = null
)

@HiltViewModel
class ExamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getExamDetailUseCase: GetExamDetailUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val examId: String = savedStateHandle["examId"] ?: ""

    private val _uiState = MutableStateFlow(ExamDetailUiState())
    val uiState: StateFlow<ExamDetailUiState> = _uiState.asStateFlow()

    init {
        loadExamDetail()
    }

    private fun loadExamDetail() {
        if (examId.isBlank()) {
            _uiState.value = ExamDetailUiState(isLoading = false, error = "Invalid exam ID")
            return
        }

        viewModelScope.launch {
            getExamDetailUseCase(examId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> ExamDetailUiState(
                        isLoading = true,
                        exam = resource.data
                    )
                    is Resource.Success -> ExamDetailUiState(
                        isLoading = false,
                        exam = resource.data
                    )
                    is Resource.Error -> ExamDetailUiState(
                        isLoading = false,
                        exam = resource.data,
                        error = resource.error?.message ?: "Failed to load exam"
                    )
                }
            }
        }
    }

    fun retry() {
        _uiState.value = ExamDetailUiState()
        loadExamDetail()
    }
}
