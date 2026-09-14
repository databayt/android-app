package org.hogwarts.android.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.notifications.data.repository.NotificationsRepository
import org.hogwarts.android.feature.notifications.data.repository.PageResult
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationPage
import javax.inject.Inject

/** `notifications/permissions.ts` `getTabsForRole`: All, Unread (badged), Settings. */
enum class NotificationsTab { All, Unread, Settings }

data class NotificationsUiState(
    val tab: NotificationsTab = NotificationsTab.All,
    val isLoading: Boolean = true,
    val page: NotificationPage? = null,
    /** Showing the last saved first page because the network failed. */
    val isOffline: Boolean = false,
    val failed: Boolean = false,
    val unreadCount: Int = 0,
    val markingAll: Boolean = false,
    /** Rows whose X was pressed, faded until the list reloads. */
    val deleting: Set<String> = emptySet(),
) {
    val items: List<AppNotification> get() = page?.items.orEmpty()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private var started = false
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            repository.unreadCount.collect { count -> if (count != null) _uiState.update { it.copy(unreadCount = count) } }
        }
    }

    /** The route decides the first tab: `/notifications` or `/notifications/unread`. */
    fun start(tab: NotificationsTab) {
        if (started) return
        started = true
        _uiState.update { it.copy(tab = tab) }
        load(1)
    }

    fun selectTab(tab: NotificationsTab) {
        if (tab == NotificationsTab.Settings || tab == _uiState.value.tab) return
        _uiState.update { it.copy(tab = tab, page = null, isLoading = true) }
        load(1)
    }

    fun goToPage(page: Int) = load(page)

    fun retry() = load(_uiState.value.page?.page ?: 1)

    /** Tapping a card marks it read (optimistically, like the web) and hands back where it leads. */
    fun open(notification: AppNotification): NotificationTarget? {
        if (!notification.isRead) {
            replaceItems { items -> items.map { if (it.id == notification.id) it.copy(isRead = true) else it } }
            viewModelScope.launch { repository.markRead(notification.id) }
        }
        return notificationTarget(notification.url)
    }

    fun delete(notification: AppNotification) {
        _uiState.update { it.copy(deleting = it.deleting + notification.id) }
        viewModelScope.launch {
            repository.delete(notification.id)
                .onSuccess {
                    replaceItems { items -> items.filterNot { it.id == notification.id } }
                    load(_uiState.value.page?.page ?: 1, quiet = true)
                }
            _uiState.update { it.copy(deleting = it.deleting - notification.id) }
        }
    }

    fun markAllRead() {
        _uiState.update { it.copy(markingAll = true) }
        replaceItems { items -> items.map { it.copy(isRead = true) } }
        viewModelScope.launch {
            repository.markAllRead()
            _uiState.update { it.copy(markingAll = false) }
            load(_uiState.value.page?.page ?: 1, quiet = true)
        }
    }

    private fun replaceItems(transform: (List<AppNotification>) -> List<AppNotification>) {
        _uiState.update { state -> state.copy(page = state.page?.let { it.copy(items = transform(it.items)) }) }
    }

    private fun load(page: Int, quiet: Boolean = false) {
        val tab = _uiState.value.tab
        loadJob?.cancel()
        if (!quiet) _uiState.update { it.copy(isLoading = true, failed = false) }
        loadJob = viewModelScope.launch {
            val result = repository.page(unreadOnly = tab == NotificationsTab.Unread, page = page)
            _uiState.update {
                when (result) {
                    is PageResult.Fresh -> it.copy(isLoading = false, page = result.page, isOffline = false, failed = false)
                    is PageResult.Cached -> it.copy(isLoading = false, page = result.page, isOffline = true, failed = false)
                    is PageResult.Failed -> it.copy(isLoading = false, failed = it.page == null)
                }
            }
        }
    }
}
