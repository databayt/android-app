package org.hogwarts.android.feature.auth.ui

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
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.FieldError
import org.hogwarts.android.feature.auth.domain.model.toAuthError
import org.hogwarts.android.feature.auth.domain.usecase.SetNewPasswordUseCase
import org.hogwarts.android.feature.auth.domain.validation.AuthValidator
import javax.inject.Inject

data class NewPasswordUiState(
    val password: String = "",
    val passwordVisible: Boolean = false,
    val passwordError: FieldError? = null,
    val error: AuthError? = null,
    val isLoading: Boolean = false,
    val updated: Boolean = false,
)

/** New password — mirrors hogwarts `auth/password/form.tsx` (one field, min 6). */
@HiltViewModel
class NewPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val setNewPassword: SetNewPasswordUseCase,
) : ViewModel() {

    private val email: String = savedStateHandle.get<String>("email").orEmpty()
    private val otp: String = savedStateHandle.get<String>("otp").orEmpty()

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun onTogglePasswordVisible() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading || state.updated) return
        val passwordError = AuthValidator.newPassword(state.password)
        if (passwordError != null) {
            _uiState.update { it.copy(passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = setNewPassword(email, otp, state.password)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, updated = true, password = "") }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.toAuthError()) }
                is Result.Loading -> Unit
            }
        }
    }
}
