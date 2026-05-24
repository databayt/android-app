package org.hogwarts.android.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.core.security.CredentialManager
import org.hogwarts.android.feature.auth.domain.usecase.FacebookAuthUseCase
import org.hogwarts.android.feature.auth.domain.usecase.GoogleAuthUseCase
import org.hogwarts.android.feature.auth.domain.usecase.LoginUseCase
import javax.inject.Inject

/**
 * ViewModel for Login screen.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val facebookAuthUseCase: FacebookAuthUseCase,
    private val biometricHelper: BiometricHelper,
    private val credentialManager: CredentialManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var email: String = ""
    private var password: String = ""

    val isBiometricAvailable: Boolean
        get() = biometricHelper.isBiometricAvailable

    val canUseBiometricLogin: Boolean
        get() = isBiometricAvailable && credentialManager.hasSavedCredentials

    fun onEmailChange(value: String) {
        email = value
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    fun onPasswordChange(value: String) {
        password = value
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    fun login() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = loginUseCase(email, password)) {
                is Result.Success -> {
                    if (isBiometricAvailable) {
                        credentialManager.saveCredentials(email, password)
                    }
                    _uiState.value = LoginUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Login failed"
                    )
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    /**
     * Authenticate with Google ID token from Credential Manager API.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = googleAuthUseCase(idToken)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Google sign-in failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Authenticate with Facebook access token.
     */
    fun signInWithFacebook(accessToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = facebookAuthUseCase(accessToken)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(
                        result.exception.message ?: "Facebook sign-in failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Called after biometric authentication succeeds.
     * Retrieves stored credentials and logs in with them.
     */
    fun onBiometricSuccess() {
        val credentials = credentialManager.getCredentials()
        if (credentials == null) {
            _uiState.value = LoginUiState.Error("No saved credentials. Please login with email and password.")
            return
        }

        val (savedEmail, savedPassword) = credentials
        email = savedEmail
        password = savedPassword

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = loginUseCase(savedEmail, savedPassword)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success
                }
                is Result.Error -> {
                    credentialManager.clearCredentials()
                    _uiState.value = LoginUiState.Error(
                        "Biometric login failed. Please login with email and password."
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onBiometricError(message: String) {
        if (message != "Authentication cancelled") {
            _uiState.value = LoginUiState.Error(message)
        }
    }

    private fun validateInput(): Boolean {
        return when {
            email.isBlank() -> {
                _uiState.value = LoginUiState.Error("Email is required")
                false
            }
            !email.contains("@") -> {
                _uiState.value = LoginUiState.Error("Invalid email format")
                false
            }
            password.isBlank() -> {
                _uiState.value = LoginUiState.Error("Password is required")
                false
            }
            else -> true
        }
    }
}
