package org.hogwarts.android.feature.admission.ui

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
import org.hogwarts.android.feature.admission.domain.usecase.GetApplicationsUseCase
import javax.inject.Inject

data class ApplicationsUiState(
    val applications: List<AdmissionApplication> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val getApplicationsUseCase: GetApplicationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    init {
        loadApplications()
    }

    private fun loadApplications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getApplicationsUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, applications = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh() = loadApplications()
}
