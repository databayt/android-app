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
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.core.security.CredentialManager
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.DemoAccounts
import org.hogwarts.android.feature.auth.domain.model.DemoRole
import org.hogwarts.android.feature.auth.domain.model.FieldError
import org.hogwarts.android.feature.auth.domain.model.SocialLogin
import org.hogwarts.android.feature.auth.domain.model.toAuthError
import org.hogwarts.android.feature.auth.domain.usecase.GoogleAuthUseCase
import org.hogwarts.android.feature.auth.domain.usecase.LoginUseCase
import org.hogwarts.android.feature.auth.domain.validation.AuthValidator
import javax.inject.Inject

/** The web's two login faces: typed credentials, or the demo school's role picker. */
enum class LoginMode { Credentials, Demo }

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val identifierError: FieldError? = null,
    val passwordError: FieldError? = null,
    val error: AuthError? = null,
    val isLoading: Boolean = false,
    val mode: LoginMode = LoginMode.Credentials,
    /** Empty in release builds — the picker is never offered. */
    val demoRoles: List<DemoRole> = emptyList(),
    val demoRole: DemoRole? = null,
    /** Non-null while a Google identity waits for its school to be picked. */
    val schools: List<SchoolInfo>? = null,
    val canUseBiometric: Boolean = false,
    val signedIn: Boolean = false,
)

/**
 * Login — mirrors hogwarts `auth/login/form.tsx` and `demo-form.tsx`, plus the
 * app-only school picker for a Google identity that belongs to several schools.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val biometricHelper: BiometricHelper,
    private val credentialManager: CredentialManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(
            demoRoles = DemoAccounts.roles,
            // Admin is preselected, as on the web, so Login works on first tap.
            demoRole = DemoAccounts.roles.firstOrNull { it == DemoRole.Admin } ?: DemoAccounts.roles.firstOrNull(),
            canUseBiometric = biometricHelper.isBiometricAvailable && credentialManager.hasSavedCredentials,
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** The Google ID token awaiting a school choice. Kept out of UI state. */
    private var pendingIdToken: String? = null

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, identifierError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun onTogglePasswordVisible() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        val identifierError = AuthValidator.identifier(state.identifier)
        val passwordError = AuthValidator.password(state.password)
        if (identifierError != null || passwordError != null) {
            _uiState.update { it.copy(identifierError = identifierError, passwordError = passwordError, error = null) }
            return
        }
        val identifier = state.identifier.trim()
        val password = state.password
        signIn(identifier, password, rememberForBiometric = true)
    }

    fun showDemo() {
        if (_uiState.value.demoRoles.isEmpty()) return
        _uiState.update { it.copy(mode = LoginMode.Demo, error = null) }
    }

    fun showCredentials() {
        _uiState.update { it.copy(mode = LoginMode.Credentials, error = null) }
    }

    fun onDemoRoleSelected(role: DemoRole) {
        _uiState.update { it.copy(demoRole = role, error = null) }
    }

    fun loginAsDemo() {
        val state = _uiState.value
        val role = state.demoRole ?: return
        if (state.isLoading || role !in state.demoRoles) return
        signIn(role.email, DemoAccounts.password, rememberForBiometric = false)
    }

    /** A Google ID token from Credential Manager. */
    fun signInWithGoogle(idToken: String) {
        if (_uiState.value.isLoading) return
        pendingIdToken = idToken
        google(idToken, schoolId = null)
    }

    /** Credential Manager failed for a reason other than the user cancelling. */
    fun onGoogleUnavailable() {
        _uiState.update { it.copy(isLoading = false, error = AuthError.Generic) }
    }

    fun onSchoolSelected(schoolId: String) {
        val token = pendingIdToken ?: return
        if (_uiState.value.isLoading) return
        google(token, schoolId)
    }

    fun dismissSchoolPicker() {
        pendingIdToken = null
        _uiState.update { it.copy(schools = null, isLoading = false, error = null) }
    }

    /** Biometric prompt passed: sign in with the saved credentials. */
    fun onBiometricSuccess() {
        val credentials = credentialManager.getCredentials()
        if (credentials == null) {
            _uiState.update { it.copy(error = AuthError.BiometricFailed, canUseBiometric = false) }
            return
        }
        val (identifier, password) = credentials
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(identifier, password)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, signedIn = true) }
                is Result.Error -> {
                    credentialManager.clearCredentials()
                    val error = result.exception.toAuthError()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            canUseBiometric = false,
                            error = if (error == AuthError.Network) error else AuthError.BiometricFailed,
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** The prompt closed without success. Cancelling is not an error. */
    fun onBiometricError(message: String) {
        if (message == BIOMETRIC_CANCELLED) return
        _uiState.update { it.copy(error = AuthError.BiometricFailed) }
    }

    private fun signIn(identifier: String, password: String, rememberForBiometric: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(identifier, password)) {
                is Result.Success -> {
                    if (rememberForBiometric && biometricHelper.isBiometricAvailable) {
                        credentialManager.saveCredentials(identifier, password)
                    }
                    _uiState.update { it.copy(isLoading = false, signedIn = true) }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, password = "", error = result.exception.toAuthError())
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun google(idToken: String, schoolId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = googleAuthUseCase(idToken, schoolId)) {
                is Result.Success -> when (val outcome = result.data) {
                    is SocialLogin.Authenticated -> {
                        pendingIdToken = null
                        _uiState.update { it.copy(isLoading = false, schools = null, signedIn = true) }
                    }
                    is SocialLogin.NeedsSchool -> _uiState.update {
                        it.copy(isLoading = false, schools = outcome.schools)
                    }
                }
                is Result.Error -> {
                    val error = result.exception.toAuthError()
                    // A rejected identity cannot be retried with another school.
                    if (error != AuthError.Network && error != AuthError.TooManyRequests) pendingIdToken = null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            schools = if (pendingIdToken == null) null else it.schools,
                            error = error,
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    private companion object {
        /** What [BiometricHelper] reports for a user cancel or the negative button. */
        const val BIOMETRIC_CANCELLED = "Authentication cancelled"
    }
}
