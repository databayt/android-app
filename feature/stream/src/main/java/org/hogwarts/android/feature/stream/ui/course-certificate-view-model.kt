package org.hogwarts.android.feature.stream.ui

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
import org.hogwarts.android.feature.stream.domain.model.CourseCertificate
import org.hogwarts.android.feature.stream.domain.usecase.GetCertificateUseCase
import javax.inject.Inject

data class CourseCertificateUiState(
    val certificate: CourseCertificate? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CourseCertificateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCertificateUseCase: GetCertificateUseCase
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""

    private val _uiState = MutableStateFlow(CourseCertificateUiState())
    val uiState: StateFlow<CourseCertificateUiState> = _uiState.asStateFlow()

    init {
        loadCertificate()
    }

    private fun loadCertificate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getCertificateUseCase(courseId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, certificate = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }
}
