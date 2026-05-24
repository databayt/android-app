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
import org.hogwarts.android.feature.exams.domain.model.ExamCertificate
import org.hogwarts.android.feature.exams.domain.usecase.GetExamCertificateUseCase
import javax.inject.Inject

data class ExamCertificateUiState(
    val isLoading: Boolean = true,
    val certificate: ExamCertificate? = null,
    val error: String? = null
)

@HiltViewModel
class ExamCertificateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getExamCertificateUseCase: GetExamCertificateUseCase
) : ViewModel() {

    private val examId: String = savedStateHandle["examId"] ?: ""

    private val _uiState = MutableStateFlow(ExamCertificateUiState())
    val uiState: StateFlow<ExamCertificateUiState> = _uiState.asStateFlow()

    init {
        loadCertificate()
    }

    private fun loadCertificate() {
        if (examId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid exam ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getExamCertificateUseCase(examId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            certificate = result.data,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error?.message ?: "Failed to load certificate"
                        )
                    }
                }
                is Resource.Loading -> { /* handled above */ }
            }
        }
    }

    fun retry() {
        loadCertificate()
    }
}
