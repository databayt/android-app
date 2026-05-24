package org.hogwarts.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.database.HogwartsDatabase
import org.hogwarts.android.core.security.CredentialManager
import org.hogwarts.android.core.security.TokenManager
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import javax.inject.Inject

/**
 * ViewModel for MainActivity.
 *
 * Manages app-wide state: authentication and session lifecycle. RTL/locale state
 * is read from [androidx.compose.ui.platform.LocalLayoutDirection] inside the theme,
 * which the framework derives from the activity's Configuration after AppCompat
 * (or the platform LocaleManager on 13+) applies the per-app locale.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val credentialManager: CredentialManager,
    private val database: HogwartsDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkSession()
        observeAuthState()
    }

    /**
     * Startup session check: verify tokens and refresh if needed.
     * Holds isLoading=true until auth state is determined.
     */
    private fun checkSession() {
        viewModelScope.launch {
            try {
                if (tokenManager.hasTokens) {
                    if (tokenManager.needsRefresh) {
                        try {
                            authRepository.refreshToken()
                        } catch (_: Exception) {
                            // Refresh failed — existing token may still work,
                            // let the auth interceptor handle 401s
                        }
                    }
                    _uiState.update { it.copy(isAuthenticated = true) }
                } else {
                    _uiState.update { it.copy(isAuthenticated = false) }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            tokenManager.isAuthenticated.collect { isAuthenticated ->
                _uiState.update { it.copy(isAuthenticated = isAuthenticated) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            credentialManager.clearCredentials()
            authRepository.logout()
            database.clearAllTables()
        }
    }
}

/**
 * Main UI state.
 */
data class MainUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true
)
