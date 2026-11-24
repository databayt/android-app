package org.hogwarts.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.preferences.AppPreferences
import org.hogwarts.android.core.database.HogwartsDatabase
import org.hogwarts.android.core.push.DeviceTokenRegistrar
import org.hogwarts.android.core.security.CredentialManager
import org.hogwarts.android.core.security.TokenManager
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
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
    private val database: HogwartsDatabase,
    private val deviceTokenRegistrar: DeviceTokenRegistrar,
    private val dashboardRepository: DashboardRepository,
    appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /** "light", "dark" or "system" — the web's mode switcher, persisted. */
    val themeMode: StateFlow<String> = appPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

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
                            // A refresh the server could not answer (offline, a bad minute) leaves the
                            // tokens in place: the token may still work, and the authenticator retries
                            // on the first 401. A refresh the server REFUSED has cleared them.
                        }
                    }
                    // Read the tokens again instead of assuming them. A refused refresh ends the session
                    // (the school re-issued the PIN, the password changed): this used to say "signed in"
                    // regardless, so the reader sat on yesterday's home screen with every request
                    // failing — shown as "offline" — until they force-closed the app.
                    _uiState.update { it.copy(isAuthenticated = tokenManager.hasTokens) }
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
            // StateFlow already skips repeats; registering on each launch is idempotent server-side.
            // The first value is the app start (already signed in or not); a later false -> true is a sign-in.
            var firstValue = true
            tokenManager.isAuthenticated.collect { isAuthenticated ->
                _uiState.update { it.copy(isAuthenticated = isAuthenticated) }
                if (!isAuthenticated) {
                    dashboardRepository.clearSession()
                } else if (firstValue) {
                    // Start: only when this app version has not registered its token yet (a failed run retries here).
                    deviceTokenRegistrar.registerIfVersionChanged(signedIn = true)
                } else {
                    deviceTokenRegistrar.registerCurrentToken()
                }
                firstValue = false
            }
        }
    }

    /**
     * The web's "open the app" brought a one-time [code]: sign in with it, then [onDone] (success or not —
     * a failed code is ignored and the link routes as today). Waits for the startup session check first,
     * and holds the splash meanwhile so the login screen never flashes.
     */
    fun signInWithHandoff(code: String, onDone: () -> Unit) {
        _uiState.update { it.copy(handingOff = true) }
        viewModelScope.launch {
            try {
                _uiState.first { !it.isLoading }
                try {
                    authRepository.signInWithHandoff(code, onReplacingSession = {
                        // Another person's session: local clean-up only, no server logout call.
                        database.clearAllTables()
                        dashboardRepository.clearSession()
                    })?.let {
                        // Same user: nothing changed for the push token. A replaced session registers the new one.
                        if (tokenManager.hasTokens) deviceTokenRegistrar.registerCurrentToken()
                        _uiState.update { it.copy(isAuthenticated = true) }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Ignored: route as if there were no code.
                }
            } finally {
                _uiState.update { it.copy(handingOff = false) }
                onDone()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            credentialManager.clearCredentials()
            deviceTokenRegistrar.unregister()
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
    val isLoading: Boolean = true,
    /** A browser hand-off is signing in; hold the splash and the first frame. */
    val handingOff: Boolean = false
)
