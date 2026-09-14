package org.hogwarts.android.feature.messaging.ui.thread

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.messaging.data.local.ConversationDrafts
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.data.repository.PageResult
import org.hogwarts.android.feature.messaging.domain.model.ChatSummary
import org.hogwarts.android.feature.messaging.domain.model.ThreadMessage
import javax.inject.Inject

@Immutable
data class ThreadUiState(
    val conversationId: String = "",
    val conversation: ChatSummary? = null,
    val messages: List<ThreadMessage> = emptyList(),
    val currentUserId: String = "",
    /** The first page request has answered. */
    val loaded: Boolean = false,
    /** Older pages remain; the encryption card waits until they are all in. */
    val hasMore: Boolean = false,
    val loadingOlder: Boolean = false,
)

private data class PagingState(
    val loaded: Boolean = false,
    val olderCursor: String? = null,
    val pagedBack: Boolean = false,
    val loadingOlder: Boolean = false,
)

/**
 * One open thread: its messages from Room, the newest page refetched on the
 * web's thread cadence (`use-thread-sync.ts`: every 5 s, every 12 s after two
 * idle minutes), older pages on demand, sends through the queued sender, and a
 * debounced mark-as-read whenever something new arrives from the other side.
 */
@HiltViewModel
class ThreadViewModel @Inject constructor(
    private val repository: MessagingRepository,
    private val drafts: ConversationDrafts,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val conversationId: String = checkNotNull(savedStateHandle.get<String>("conversationId"))

    private val paging = MutableStateFlow(PagingState())
    private var lastActivityAt = System.currentTimeMillis()
    private var readJob: Job? = null

    private val synced = channelFlow {
        launch {
            while (isActive) {
                refreshNewest()
                val idle = System.currentTimeMillis() - lastActivityAt > IDLE_AFTER_MS
                delay(if (idle) IDLE_POLL_MS else ACTIVE_POLL_MS)
            }
        }
        launch {
            // New incoming messages mark the thread read, debounced as on the web.
            repository.observeMessages(conversationId)
                .map { list -> list.lastOrNull { it.senderId != repository.currentUserId }?.id }
                .distinctUntilChanged()
                .collect { scheduleRead() }
        }
        repository.observeMessages(conversationId).collect { send(it) }
    }

    val uiState: StateFlow<ThreadUiState> = combine(
        synced,
        repository.observeConversation(conversationId),
        paging,
    ) { messages, conversation, page ->
        ThreadUiState(
            conversationId = conversationId,
            conversation = conversation,
            messages = messages,
            currentUserId = repository.currentUserId.orEmpty(),
            loaded = page.loaded,
            hasMore = page.olderCursor != null,
            loadingOlder = page.loadingOlder,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThreadUiState(conversationId = conversationId))

    private suspend fun refreshNewest() {
        when (val result = repository.refreshMessages(conversationId)) {
            is PageResult.Loaded -> paging.update {
                // Once the reader has paged back, the newest page's cursor would
                // point at history they already hold.
                if (it.pagedBack) it.copy(loaded = true) else it.copy(loaded = true, olderCursor = result.olderCursor)
            }
            PageResult.Failed -> paging.update { it.copy(loaded = true) }
        }
    }

    /** The reader neared the top of what is loaded. */
    fun loadOlder() {
        val page = paging.value
        val cursor = page.olderCursor ?: return
        if (page.loadingOlder) return
        touch()
        paging.update { it.copy(loadingOlder = true) }
        viewModelScope.launch {
            when (val result = repository.loadOlderMessages(conversationId, cursor)) {
                is PageResult.Loaded -> paging.update {
                    it.copy(loadingOlder = false, pagedBack = true, olderCursor = result.olderCursor)
                }
                PageResult.Failed -> paging.update { it.copy(loadingOlder = false) }
            }
        }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        touch()
        viewModelScope.launch {
            drafts.save(conversationId, "")
            repository.sendMessage(conversationId, text)
        }
    }

    /** A tap on a failed bubble. */
    fun retry(messageId: String) {
        touch()
        viewModelScope.launch { repository.retryMessage(messageId) }
    }

    suspend fun initialDraft(): String = drafts.observe(conversationId).first()

    fun saveDraft(text: String) {
        touch()
        viewModelScope.launch { drafts.save(conversationId, text) }
    }

    /** Scrolling and typing count as activity for the poll cadence. */
    fun touch() {
        lastActivityAt = System.currentTimeMillis()
    }

    private fun scheduleRead() {
        readJob?.cancel()
        readJob = viewModelScope.launch {
            delay(READ_DEBOUNCE_MS)
            repository.markAsRead(conversationId)
        }
    }

    companion object {
        const val ACTIVE_POLL_MS = 5_000L
        const val IDLE_POLL_MS = 12_000L
        const val IDLE_AFTER_MS = 2 * 60_000L
        const val READ_DEBOUNCE_MS = 800L
    }
}
