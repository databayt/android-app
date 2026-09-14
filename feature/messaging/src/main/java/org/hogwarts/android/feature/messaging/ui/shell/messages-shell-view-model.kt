package org.hogwarts.android.feature.messaging.ui.shell

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.domain.model.ChatSummary
import org.hogwarts.android.feature.messaging.domain.model.MessagingViewer
import org.hogwarts.android.feature.messaging.ui.chats.ChatFilter
import javax.inject.Inject

/** `IosTabId`, in the web's order. */
enum class MessagesTab { Updates, Calls, Communities, Chats, Settings }

@Immutable
data class ShellUiState(
    val chats: List<ChatSummary> = emptyList(),
    val viewer: MessagingViewer? = null,
    /** The first list request has answered, either way. */
    val loaded: Boolean = false,
    val loadFailed: Boolean = false,
    val tab: MessagesTab = MessagesTab.Chats,
    val query: String = "",
    val filter: ChatFilter = ChatFilter.All,
    val selecting: Boolean = false,
    val selected: Set<String> = emptySet(),
) {
    val totalUnread: Int get() = chats.sumOf { it.unreadCount }
}

private data class LocalState(
    val tab: MessagesTab,
    val query: String = "",
    val filter: ChatFilter = ChatFilter.All,
    val selecting: Boolean = false,
    val selected: Set<String> = emptySet(),
)

private data class RemoteState(
    val chats: List<ChatSummary> = emptyList(),
    val loaded: Boolean = false,
    val loadFailed: Boolean = false,
)

/**
 * The Messages shell: which tab shows, the conversation list, and the chat
 * list's own controls (search, filter chips, Select chats, Read all).
 *
 * The list is refetched every [LIST_POLL_MS] while the screen is observed —
 * `LIST_POLL_MS` in `messaging-client.tsx`, the web's fallback while no socket
 * is connected, which on balqalam.com is always.
 */
@HiltViewModel
class MessagesShellViewModel @Inject constructor(
    private val repository: MessagingRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val local = MutableStateFlow(
        LocalState(tab = savedStateHandle.get<String>(KEY_TAB)?.let { runCatching { MessagesTab.valueOf(it) }.getOrNull() } ?: MessagesTab.Chats)
    )
    private val viewer = MutableStateFlow<MessagingViewer?>(null)

    private val remote = channelFlow {
        val state = MutableStateFlow(RemoteState())
        launch { repository.observeConversations().collect { rows -> state.update { it.copy(chats = rows) } } }
        launch { if (viewer.value == null) viewer.value = repository.viewer() }
        launch {
            while (isActive) {
                val ok = repository.refreshConversations()
                state.update { it.copy(loaded = true, loadFailed = !ok) }
                delay(LIST_POLL_MS)
            }
        }
        state.collect { send(it) }
    }

    val uiState: StateFlow<ShellUiState> = combine(remote, local, viewer) { r, l, v ->
        ShellUiState(
            chats = r.chats,
            viewer = v,
            loaded = r.loaded,
            loadFailed = r.loadFailed,
            tab = l.tab,
            query = l.query,
            filter = l.filter,
            selecting = l.selecting,
            selected = l.selected,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShellUiState(tab = local.value.tab))

    fun selectTab(tab: MessagesTab) {
        savedStateHandle[KEY_TAB] = tab.name
        local.update { it.copy(tab = tab) }
    }

    fun setQuery(query: String) = local.update { it.copy(query = query) }

    fun setFilter(filter: ChatFilter) = local.update { it.copy(filter = filter) }

    fun startSelecting() = local.update { it.copy(selecting = true, selected = emptySet()) }

    fun stopSelecting() = local.update { it.copy(selecting = false, selected = emptySet()) }

    fun toggleSelected(id: String) = local.update {
        it.copy(selected = if (id in it.selected) it.selected - id else it.selected + id)
    }

    /** Select chats → Read: the picked rows clear at once, the server follows. */
    fun readSelected() {
        val ids = local.value.selected
        stopSelecting()
        markRead(ids)
    }

    /** ⋯ → Read all. */
    fun readAll() = markRead(uiState.value.chats.filter { it.unreadCount > 0 }.map { it.id })

    private fun markRead(ids: Collection<String>) {
        viewModelScope.launch { ids.forEach { repository.markAsRead(it) } }
    }

    companion object {
        const val LIST_POLL_MS = 15_000L
        private const val KEY_TAB = "messages_tab"
    }
}
