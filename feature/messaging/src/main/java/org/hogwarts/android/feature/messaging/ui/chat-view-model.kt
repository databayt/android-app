package org.hogwarts.android.feature.messaging.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.network.socket.SocketManager
import org.hogwarts.android.feature.messaging.data.local.DraftStore
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.domain.model.Message
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MessagingRepository,
    private val socketManager: SocketManager,
    private val tenantContext: TenantContext,
    private val draftStore: DraftStore,
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ChatUiState(currentUserId = tenantContext.userId ?: ""))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _draftText = MutableStateFlow("")
    private var typingJob: Job? = null
    private var isTypingEmitted = false

    init {
        socketManager.subscribeToConversation(conversationId)
        loadMessages()
        loadConversationMeta()
        observeTyping()
        observeConnectionState()
        markAsRead()
        restoreDraft()
        observeDraftSave()
    }

    private fun restoreDraft() {
        viewModelScope.launch {
            val initial = runCatching {
                draftStore.observe(conversationId)
            }.getOrNull() ?: return@launch
            initial.collect { stored ->
                // Only apply the initial stored draft once, if the field is empty
                if (_uiState.value.messageText.isEmpty() && stored.isNotBlank()) {
                    _uiState.update { it.copy(messageText = stored) }
                }
                return@collect  // single read; subsequent writes are driven by observeDraftSave
            }
        }
    }

    private fun observeDraftSave() {
        viewModelScope.launch {
            _draftText
                .debounce(500)
                .distinctUntilChanged()
                .collect { text ->
                    draftStore.save(conversationId, text)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.unsubscribeFromConversation(conversationId)
        if (isTypingEmitted) {
            repository.sendTypingStop(conversationId)
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, messages = resource.data ?: it.messages)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = resource.data ?: emptyList(),
                            error = null,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = resource.data ?: it.messages,
                            error = resource.error?.message,
                        )
                    }
                }
            }
        }
    }

    /** Observe the conversation list to pull isPinned/isMuted/title for this conversation. */
    private fun loadConversationMeta() {
        viewModelScope.launch {
            repository.getConversations().collect { resource ->
                val conv = resource.data?.firstOrNull { it.id == conversationId } ?: return@collect
                _uiState.update {
                    it.copy(
                        conversationTitle = conv.title.ifEmpty { it.conversationTitle },
                        conversationType = conv.type.name,
                        isWhatsAppEnabled = conv.isWhatsAppEnabled,
                        isPinned = conv.isPinned,
                        isMuted = conv.isMuted,
                    )
                }
            }
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            repository.observeTypingIndicators(conversationId).collect { indicators ->
                _uiState.update {
                    it.copy(typingUsers = indicators.map { ti -> ti.userName })
                }
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            repository.observeConnectionState().collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            try { repository.markAsRead(conversationId) } catch (_: Exception) { }
        }
    }

    fun onMessageTextChanged(value: String) {
        _uiState.update { it.copy(messageText = value) }
        _draftText.value = value
        handleTypingEmission(value)
    }

    fun onSendMessage() {
        val content = _uiState.value.messageText.trim()
        if (content.isBlank()) return

        val editing = _uiState.value.editingMessage
        if (editing != null) {
            submitEdit(editing.id, content)
            return
        }

        val replyToId = _uiState.value.replyToMessage?.id

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, messageText = "", replyToMessage = null) }
            _draftText.value = ""
            draftStore.clear(conversationId)
            try {
                repository.sendMessage(conversationId, content, replyToId)
                _uiState.update { it.copy(isSending = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = e.message) }
            }
        }

        if (isTypingEmitted) {
            repository.sendTypingStop(conversationId)
            isTypingEmitted = false
        }
    }

    fun onReplyToMessage(message: Message) {
        _uiState.update { it.copy(replyToMessage = message, actionTarget = null) }
    }

    fun onCancelReply() {
        _uiState.update { it.copy(replyToMessage = null) }
    }

    fun onScrolledToBottom() {
        _uiState.update { it.copy(isAtBottom = true, newUnreadCount = 0) }
        markAsRead()
    }

    fun onScrolledAway() {
        _uiState.update { it.copy(isAtBottom = false) }
    }

    // ----- Phase 2 action handlers -----

    fun onMessageLongPress(message: Message) {
        _uiState.update { it.copy(actionTarget = message) }
    }

    fun onDismissActionSheet() {
        _uiState.update { it.copy(actionTarget = null) }
    }

    fun onBeginEdit(message: Message) {
        _uiState.update {
            it.copy(
                editingMessage = message,
                messageText = message.content,
                actionTarget = null,
                replyToMessage = null,
            )
        }
    }

    fun onCancelEdit() {
        _uiState.update { it.copy(editingMessage = null, messageText = "") }
    }

    private fun submitEdit(messageId: String, content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            try {
                repository.editMessage(conversationId, messageId, content)
                _uiState.update {
                    it.copy(isSending = false, editingMessage = null, messageText = "")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = e.message) }
            }
        }
    }

    fun onConfirmDelete(message: Message) {
        _uiState.update { it.copy(confirmingDelete = message, actionTarget = null) }
    }

    fun onDismissDelete() {
        _uiState.update { it.copy(confirmingDelete = null) }
    }

    fun onDeleteConfirmed() {
        val msg = _uiState.value.confirmingDelete ?: return
        viewModelScope.launch {
            try {
                repository.deleteMessage(conversationId, msg.id)
                _uiState.update { it.copy(confirmingDelete = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(confirmingDelete = null, error = e.message) }
            }
        }
    }

    fun onToggleReaction(message: Message, emoji: String) {
        val alreadyReacted = message.reactions.any {
            it.userId == tenantContext.userId && it.emoji == emoji
        }
        viewModelScope.launch {
            try {
                if (alreadyReacted) {
                    repository.removeReaction(conversationId, message.id, emoji)
                } else {
                    repository.addReaction(conversationId, message.id, emoji)
                }
                _uiState.update { it.copy(actionTarget = null, emojiPickerTarget = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onOpenEmojiPicker(message: Message) {
        _uiState.update { it.copy(emojiPickerTarget = message, actionTarget = null) }
    }

    fun onDismissEmojiPicker() {
        _uiState.update { it.copy(emojiPickerTarget = null) }
    }

    fun onToggleStar(message: Message) {
        val currentlyStarred = _uiState.value.starredMessageIds.contains(message.id)
        viewModelScope.launch {
            try {
                repository.toggleStar(conversationId, message.id, !currentlyStarred)
                _uiState.update {
                    val next = it.starredMessageIds.toMutableSet()
                    if (currentlyStarred) next.remove(message.id) else next.add(message.id)
                    it.copy(starredMessageIds = next, actionTarget = null)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onTogglePin() {
        val next = !_uiState.value.isPinned
        viewModelScope.launch {
            try {
                repository.togglePin(conversationId, next)
                _uiState.update {
                    it.copy(
                        isPinned = next,
                        toastResId = if (next) R.string.messaging_notifications_pinned_success
                        else R.string.messaging_notifications_unpinned_success,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onToggleMute() {
        val next = !_uiState.value.isMuted
        viewModelScope.launch {
            try {
                repository.toggleMute(conversationId, next)
                _uiState.update {
                    it.copy(
                        isMuted = next,
                        toastResId = if (next) R.string.messaging_notifications_muted_success
                        else R.string.messaging_notifications_unmuted_success,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onRequestArchive() {
        _uiState.update { it.copy(confirmingArchive = true) }
    }

    fun onDismissArchive() {
        _uiState.update { it.copy(confirmingArchive = false) }
    }

    fun onArchiveConfirmed() {
        viewModelScope.launch {
            try {
                repository.archiveConversation(conversationId, true)
                _uiState.update {
                    it.copy(
                        confirmingArchive = false,
                        toastResId = R.string.messaging_notifications_archived_success,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(confirmingArchive = false, error = e.message) }
            }
        }
    }

    fun onRequestLeave() {
        _uiState.update { it.copy(confirmingLeave = true) }
    }

    fun onDismissLeave() {
        _uiState.update { it.copy(confirmingLeave = false) }
    }

    fun onLeaveConfirmed() {
        viewModelScope.launch {
            try {
                repository.leaveConversation(conversationId)
                _uiState.update { it.copy(confirmingLeave = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(confirmingLeave = false, error = e.message) }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastResId = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleTypingEmission(text: String) {
        typingJob?.cancel()
        if (text.isNotBlank()) {
            if (!isTypingEmitted) {
                repository.sendTypingStart(conversationId)
                isTypingEmitted = true
            }
            typingJob = viewModelScope.launch {
                delay(3000)
                repository.sendTypingStop(conversationId)
                isTypingEmitted = false
            }
        } else if (isTypingEmitted) {
            repository.sendTypingStop(conversationId)
            isTypingEmitted = false
        }
    }
}
