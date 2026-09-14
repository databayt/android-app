package org.hogwarts.android.feature.auth.ui

import androidx.lifecycle.SavedStateHandle
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
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.toAuthError
import org.hogwarts.android.feature.auth.domain.usecase.RequestPasswordResetUseCase
import javax.inject.Inject

data class VerifyOtpUiState(
    val email: String = "",
    val otp: String = "",
    val error: AuthError? = null,
    val isResending: Boolean = false,
    /** True for a moment after a resend succeeded. */
    val resent: Boolean = false,
    val resendCooldown: Int = 0,
    /** The complete code, set when the user may continue to the new password. */
    val confirmedOtp: String? = null,
)

/**
 * The code step — the web's verify view (`auth/login/form.tsx` verify mode):
 * "We sent a code to m***@x", slots, "Resend in Ns", back to login.
 *
 * It only collects the code. `new-password` checks it; calling `verify-otp`
 * first would delete the code and make that check fail.
 */
@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val requestPasswordReset: RequestPasswordResetUseCase,
) : ViewModel() {

    private val email: String = savedStateHandle.get<String>("email").orEmpty()

    private val _uiState = MutableStateFlow(VerifyOtpUiState(email = email))
    val uiState: StateFlow<VerifyOtpUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        // The code was just sent by the previous step.
        startCooldown()
    }

    fun onOtpChange(value: String) {
        val digits = value.filter(Char::isDigit).take(OTP_LENGTH)
        _uiState.update { it.copy(otp = digits, error = null, resent = false) }
    }

    /** Continue with a complete code. */
    fun submit() {
        val otp = _uiState.value.otp
        if (otp.length != OTP_LENGTH) {
            _uiState.update { it.copy(error = AuthError.InvalidCode) }
            return
        }
        _uiState.update { it.copy(confirmedOtp = otp) }
    }

    fun onNewPasswordOpened() {
        _uiState.update { it.copy(confirmedOtp = null) }
    }

    fun resend() {
        val state = _uiState.value
        if (state.resendCooldown > 0 || state.isResending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, error = null, resent = false) }
            when (val result = requestPasswordReset(email)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isResending = false, otp = "", resent = true) }
                    startCooldown()
                }
                is Result.Error -> _uiState.update { it.copy(isResending = false, error = result.exception.toAuthError()) }
                is Result.Loading -> Unit
            }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (seconds in RESEND_COOLDOWN_SECONDS downTo 0) {
                _uiState.update { it.copy(resendCooldown = seconds) }
                if (seconds > 0) delay(1_000)
            }
        }
    }

    companion object {
        const val OTP_LENGTH = 6

        /** The web's `setResendCooldown(90)`. */
        const val RESEND_COOLDOWN_SECONDS = 90
    }
}
