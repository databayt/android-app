package org.hogwarts.android.feature.exams.ui

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
import org.hogwarts.android.feature.exams.data.remote.dto.ExamAnswerDto
import org.hogwarts.android.feature.exams.domain.model.DetailedExamResult
import org.hogwarts.android.feature.exams.domain.usecase.GetExamResultsUseCase
import javax.inject.Inject

data class ExamResultsUiState(
    val isLoading: Boolean = true,
    val result: DetailedExamResult? = null,
    val answerDetails: List<ExamAnswerDto> = emptyList(),
    val selectedTab: ResultTab = ResultTab.SUMMARY,
    val error: String? = null
)

enum class ResultTab { SUMMARY, ANSWERS, DISTRIBUTION }

@HiltViewModel
class ExamResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getExamResultsUseCase: GetExamResultsUseCase
) : ViewModel() {

    private val examId: String = savedStateHandle["examId"] ?: ""

    private val _uiState = MutableStateFlow(ExamResultsUiState())
    val uiState: StateFlow<ExamResultsUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    private fun loadResults() {
        if (examId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid exam ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getExamResultsUseCase(examId)) {
                is Resource.Success -> {
                    val data = result.data!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = data.result,
                            answerDetails = data.answerDetails,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error?.message ?: "Failed to load results"
                        )
                    }
                }
                is Resource.Loading -> { /* handled above */ }
            }
        }
    }

    fun onTabSelected(tab: ResultTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retry() {
        loadResults()
    }
}
