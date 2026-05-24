package org.hogwarts.android.feature.notifications.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.notifications.domain.NotificationConfig
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationPreference
import org.hogwarts.android.feature.notifications.domain.model.NotificationType
import org.hogwarts.android.feature.notifications.domain.usecase.GetNotificationPreferencesUseCase
import org.hogwarts.android.feature.notifications.domain.usecase.UpdateNotificationPreferencesUseCase
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val getPreferences: GetNotificationPreferencesUseCase,
    private val updatePreferences: UpdateNotificationPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getPreferences()
                .onSuccess { prefs ->
                    val map = prefs.associateBy { it.type to it.channel }
                    _uiState.update { it.copy(isLoading = false, preferences = map) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun toggle(type: NotificationType, channel: NotificationChannel, enabled: Boolean) {
        val key = type to channel
        _uiState.update {
            val current = it.preferences[key]
                ?: NotificationPreference(type = type, channel = channel, enabled = enabled)
            it.copy(preferences = it.preferences + (key to current.copy(enabled = enabled)))
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            // Only push entries the user can configure (matrix of supported channels × types).
            val payload = NotificationConfig.configurableTypes.flatMap { type ->
                NotificationConfig.supportedChannels.map { channel ->
                    val existing = _uiState.value.preferences[type to channel]
                    NotificationPreference(
                        type = type,
                        channel = channel,
                        enabled = existing?.enabled ?: _uiState.value.isEnabled(type, channel)
                    )
                }
            }
            updatePreferences(payload)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, savedAt = System.currentTimeMillis()) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }
}
