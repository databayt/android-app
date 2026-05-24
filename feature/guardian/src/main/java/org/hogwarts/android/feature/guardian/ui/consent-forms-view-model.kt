package org.hogwarts.android.feature.guardian.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsentForm(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val signedAt: Long? = null
)

data class ConsentFormsUiState(
    val pendingForms: List<ConsentForm> = emptyList(),
    val signedForms: List<ConsentForm> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConsentFormsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ConsentFormsUiState())
    val uiState: StateFlow<ConsentFormsUiState> = _uiState.asStateFlow()

    init { loadForms() }

    private fun loadForms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API call placeholder
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun signForm(formId: String, signatureData: List<Offset>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API call to submit signed form
            _uiState.update { state ->
                val form = state.pendingForms.find { it.id == formId }
                if (form != null) {
                    state.copy(
                        pendingForms = state.pendingForms.filter { it.id != formId },
                        signedForms = state.signedForms + form.copy(signedAt = System.currentTimeMillis()),
                        isLoading = false
                    )
                } else state.copy(isLoading = false)
            }
        }
    }
}
