package org.hogwarts.android.feature.admission.ui

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
import org.hogwarts.android.feature.admission.domain.model.AdmissionApplication
import org.hogwarts.android.feature.admission.domain.usecase.GetApplicationUseCase
import javax.inject.Inject

data class ApplicationStatusUiState(
    val application: AdmissionApplication? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationStatusViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getApplicationUseCase: GetApplicationUseCase
) : ViewModel() {

    private val applicationId: String = savedStateHandle["applicationId"] ?: ""

    private val _uiState = MutableStateFlow(ApplicationStatusUiState())
    val uiState: StateFlow<ApplicationStatusUiState> = _uiState.asStateFlow()

    init {
        loadApplication()
    }

    private fun loadApplication() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getApplicationUseCase(applicationId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, application = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }
}
