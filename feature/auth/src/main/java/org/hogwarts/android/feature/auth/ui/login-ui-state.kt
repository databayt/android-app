package org.hogwarts.android.feature.auth.ui

/**
 * UI state for Login screen.
 */
sealed interface LoginUiState {
    /** Initial state */
    data object Idle : LoginUiState

    /** Loading state during authentication */
    data object Loading : LoginUiState

    /** Authentication successful */
    data object Success : LoginUiState

    /** Authentication failed */
    data class Error(val message: String) : LoginUiState
}
