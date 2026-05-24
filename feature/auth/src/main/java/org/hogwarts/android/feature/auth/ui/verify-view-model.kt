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
import org.hogwarts.android.feature.auth.domain.usecase.VerifyOtpUseCase
import javax.inject.Inject

data class VerifyUiState(
    val email: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val resendCooldown: Int = 60
)

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val verifyOtpUseCase: VerifyOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        startCooldown()
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value, error = null) }
    }

    fun verify() {
        val state = _uiState.value
        if (state.otp.length != 4) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = verifyOtpUseCase(email = state.email, otp = state.otp)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Verification failed",
                            otp = ""
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resendOtp() {
        if (_uiState.value.resendCooldown > 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(otp = "", error = null) }
            startCooldown()
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update { it.copy(resendCooldown = 60) }
            for (i in 59 downTo 0) {
                delay(1000)
                _uiState.update { it.copy(resendCooldown = i) }
            }
        }
    }
}
