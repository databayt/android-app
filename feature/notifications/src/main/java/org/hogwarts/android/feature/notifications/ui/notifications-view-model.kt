package org.hogwarts.android.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.notifications.domain.NotificationConfig
import org.hogwarts.android.feature.notifications.domain.model.NotificationType
import org.hogwarts.android.feature.notifications.domain.usecase.GetNotificationsUseCase
import org.hogwarts.android.feature.notifications.domain.usecase.MarkAllNotificationsReadUseCase
import org.hogwarts.android.feature.notifications.domain.usecase.MarkNotificationReadUseCase
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { loadNotifications() }

    private fun loadNotifications() {
        viewModelScope.launch {
            // Filtering by tab/type happens in NotificationsUiState.filtered, so we
            // always fetch the full list here and let the state derive subsets.
            getNotificationsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, notifications = resource.data ?: it.notifications)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, notifications = resource.data ?: emptyList(), error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = resource.data ?: it.notifications,
                            error = resource.error?.message
                        )
                    }
                }
            }
        }
    }

    fun onTabSelected(tab: NotificationConfig.FilterTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onTypeFilterSelected(type: NotificationType?) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch { markNotificationReadUseCase(notificationId) }
    }

    fun markAllRead() {
        viewModelScope.launch { markAllNotificationsReadUseCase() }
    }
}
