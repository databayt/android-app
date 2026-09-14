package org.hogwarts.android.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.FieldError
import org.hogwarts.android.feature.auth.domain.model.toAuthError
import org.hogwarts.android.feature.auth.domain.usecase.RequestPasswordResetUseCase
import org.hogwarts.android.feature.auth.domain.validation.AuthValidator
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: FieldError? = null,
    val error: AuthError? = null,
    val isLoading: Boolean = false,
    /** Set once the code is sent; the screen moves to the code step and clears it. */
    val codeSentTo: String? = null,
)

/** Forgot password — mirrors hogwarts `auth/reset/form.tsx` (one email field). */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordReset: RequestPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, error = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        val emailError = AuthValidator.email(state.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }
        val email = state.email.trim().lowercase()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = requestPasswordReset(email)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, codeSentTo = email) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.toAuthError()) }
                is Result.Loading -> Unit
            }
        }
    }

    fun onCodeStepOpened() {
        _uiState.update { it.copy(codeSentTo = null) }
    }
}
