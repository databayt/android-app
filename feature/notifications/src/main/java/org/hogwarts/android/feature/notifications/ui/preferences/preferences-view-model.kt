package org.hogwarts.android.feature.notifications.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.notifications.data.repository.NotificationsRepository
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.domain.model.PreferenceMatrix
import javax.inject.Inject

enum class SaveOutcome { Saved, Failed }

data class PreferencesUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    /** What the server holds — Reset returns here. */
    val saved: PreferenceMatrix? = null,
    val matrix: PreferenceMatrix? = null,
    val isSaving: Boolean = false,
    val outcome: SaveOutcome? = null,
    val unreadCount: Int = 0,
    val markingAll: Boolean = false,
)

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val repository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.unreadCount.collect { count -> if (count != null) _uiState.update { it.copy(unreadCount = count) } }
        }
        viewModelScope.launch {
            // The layout's badge needs the unread total even when the list was never opened.
            if (repository.unreadCount.value == null) repository.page(unreadOnly = false, page = 1)
        }
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch {
            repository.preferences()
                .onSuccess { matrix -> _uiState.update { it.copy(isLoading = false, saved = matrix, matrix = matrix) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, loadFailed = true) } }
        }
    }

    fun toggle(kind: NotificationKind, channel: NotificationChannel, on: Boolean) {
        _uiState.update { state -> state.copy(matrix = state.matrix?.with(kind, channel, on), outcome = null) }
    }

    fun reset() = _uiState.update { it.copy(matrix = it.saved, outcome = null) }

    fun save() {
        val matrix = _uiState.value.matrix ?: return
        _uiState.update { it.copy(isSaving = true, outcome = null) }
        viewModelScope.launch {
            val result = repository.savePreferences(matrix)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isSaving = false, saved = matrix, outcome = SaveOutcome.Saved)
                } else {
                    it.copy(isSaving = false, outcome = SaveOutcome.Failed)
                }
            }
        }
    }

    fun markAllRead() {
        _uiState.update { it.copy(markingAll = true) }
        viewModelScope.launch {
            repository.markAllRead()
            _uiState.update { it.copy(markingAll = false) }
        }
    }
}
