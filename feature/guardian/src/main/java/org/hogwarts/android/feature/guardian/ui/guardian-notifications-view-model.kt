package org.hogwarts.android.feature.guardian.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class GuardianNotificationsUiState(
    val notifications: List<GuardianNotification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GuardianNotificationsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GuardianNotificationsUiState())
    val uiState: StateFlow<GuardianNotificationsUiState> = _uiState.asStateFlow()
}
