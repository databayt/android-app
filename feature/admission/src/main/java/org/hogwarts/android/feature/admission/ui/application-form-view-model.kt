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
import org.hogwarts.android.feature.admission.domain.model.AcademicHistory
import org.hogwarts.android.feature.admission.domain.model.AdmissionApplication
import org.hogwarts.android.feature.admission.domain.model.ApplicationStep
import org.hogwarts.android.feature.admission.domain.model.ContactInfo
import org.hogwarts.android.feature.admission.domain.model.GuardianInfo
import org.hogwarts.android.feature.admission.domain.model.PersonalInfo
import org.hogwarts.android.feature.admission.domain.usecase.GetApplicationUseCase
import org.hogwarts.android.feature.admission.domain.usecase.SaveApplicationUseCase
import org.hogwarts.android.feature.admission.domain.usecase.SubmitApplicationUseCase
import javax.inject.Inject

data class ApplicationFormUiState(
    val application: AdmissionApplication = AdmissionApplication(guardians = listOf(GuardianInfo())),
    val currentStep: ApplicationStep = ApplicationStep.PERSONAL_INFO,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUploading: Boolean = false,
    val termsAccepted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getApplicationUseCase: GetApplicationUseCase,
    private val saveApplicationUseCase: SaveApplicationUseCase,
    private val submitApplicationUseCase: SubmitApplicationUseCase
) : ViewModel() {

    private val applicationId: String? = savedStateHandle["applicationId"]

    private val _uiState = MutableStateFlow(ApplicationFormUiState())
    val uiState: StateFlow<ApplicationFormUiState> = _uiState.asStateFlow()

    init {
        applicationId?.let { loadApplication(it) }
    }

    private fun loadApplication(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getApplicationUseCase(id)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        application = result.data,
                        currentStep = result.data.currentStep
                    )
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun updatePersonalInfo(info: PersonalInfo) {
        _uiState.update { it.copy(application = it.application.copy(personalInfo = info)) }
    }

    fun updateContactInfo(info: ContactInfo) {
        _uiState.update { it.copy(application = it.application.copy(contactInfo = info)) }
    }

    fun updateGuardian(index: Int, guardian: GuardianInfo) {
        _uiState.update { state ->
            val guardians = state.application.guardians.toMutableList()
            if (index < guardians.size) guardians[index] = guardian
            state.copy(application = state.application.copy(guardians = guardians))
        }
    }

    fun addGuardian() {
        _uiState.update { state ->
            state.copy(application = state.application.copy(guardians = state.application.guardians + GuardianInfo()))
        }
    }

    fun updateAcademicHistory(history: AcademicHistory) {
        _uiState.update { it.copy(application = it.application.copy(academicHistory = history)) }
    }

    fun setTermsAccepted(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted) }
    }

    fun requestDocumentUpload() {
        // File picker handled by UI layer - placeholder for upload trigger
        _uiState.update { it.copy(isUploading = false) }
    }

    fun nextStep() {
        val current = _uiState.value.currentStep
        val next = ApplicationStep.entries.find { it.number == current.number + 1 } ?: return
        autoSave()
        _uiState.update { it.copy(currentStep = next, application = it.application.copy(currentStep = next)) }
    }

    fun previousStep() {
        val current = _uiState.value.currentStep
        val prev = ApplicationStep.entries.find { it.number == current.number - 1 } ?: return
        _uiState.update { it.copy(currentStep = prev) }
    }

    private fun autoSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = saveApplicationUseCase(_uiState.value.application)) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false, application = result.data) }
                is Result.Error -> _uiState.update { it.copy(isSaving = false) }
                is Result.Loading -> {}
            }
        }
    }

    fun submit(onSuccess: () -> Unit) {
        val appId = _uiState.value.application.id
        if (appId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = submitApplicationUseCase(appId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, application = result.data) }
                    onSuccess()
                }
                is Result.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }
}
