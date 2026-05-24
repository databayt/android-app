package org.hogwarts.android.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.domain.usecase.RequestPasswordResetUseCase
import org.hogwarts.android.feature.auth.domain.usecase.SetNewPasswordUseCase
import org.hogwarts.android.feature.auth.domain.usecase.VerifyOtpUseCase
import org.hogwarts.android.feature.auth.domain.validation.SignupValidator
import javax.inject.Inject

enum class ResetStep { EMAIL, OTP, NEW_PASSWORD, SUCCESS }

data class ForgotPasswordUiState(
    val step: ResetStep = ResetStep.EMAIL,
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendCooldown: Int = 0
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val setNewPasswordUseCase: SetNewPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onOtpChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(otp = value, error = null) }
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, error = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun requestReset() {
        val email = _uiState.value.email
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Please enter a valid email") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = requestPasswordResetUseCase(email)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, step = ResetStep.OTP) }
                    startResendCooldown()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message ?: "Failed to send reset email")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun verifyOtp() {
        val state = _uiState.value
        if (state.otp.length != 6) {
            _uiState.update { it.copy(error = "Please enter the 6-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = verifyOtpUseCase(state.email, state.otp)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, step = ResetStep.NEW_PASSWORD) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message ?: "Invalid code")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun setNewPassword() {
        val state = _uiState.value

        val passwordError = SignupValidator.validatePassword(state.newPassword)
        if (passwordError != null) {
            _uiState.update { it.copy(error = passwordError) }
            return
        }

        if (state.confirmPassword.isBlank()) {
            _uiState.update { it.copy(error = "Please confirm your password") }
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = setNewPasswordUseCase(state.email, state.otp, state.newPassword)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, step = ResetStep.SUCCESS) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message ?: "Failed to set new password")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resendOtp() {
        if (_uiState.value.resendCooldown > 0) return
        requestReset()
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(resendCooldown = i) }
                delay(1000)
            }
        }
    }
}
