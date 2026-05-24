package org.hogwarts.android.feature.messaging.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.domain.model.Conversation
import org.hogwarts.android.feature.messaging.domain.model.ConversationType
import org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult
import javax.inject.Inject

data class ConversationInfoUiState(
    val isLoading: Boolean = true,
    val conversation: Conversation? = null,
    val starredMessages: List<MessageSearchResult> = emptyList(),
    val isMuted: Boolean = false,
    val confirmingExit: Boolean = false,
    val confirmingArchive: Boolean = false,
    val error: String? = null,
    val navigateAwayAfterAction: Boolean = false,
)

@HiltViewModel
class ConversationInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MessagingRepository,
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _state = MutableStateFlow(ConversationInfoUiState())
    val state: StateFlow<ConversationInfoUiState> = _state.asStateFlow()

    init {
        loadConversation()
        loadStarred()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            repository.getConversations().collect { resource ->
                val conv = resource.data?.firstOrNull { it.id == conversationId }
                if (conv != null) {
                    _state.update {
                        it.copy(isLoading = false, conversation = conv, isMuted = conv.isMuted)
                    }
                } else if (resource is org.hogwarts.android.core.data.util.Resource.Error) {
                    _state.update { it.copy(isLoading = false, error = resource.error?.message) }
                }
            }
        }
    }

    private fun loadStarred() {
        viewModelScope.launch {
            try {
                val starred = repository.getStarredMessages(limit = 50)
                    .filter { it.conversationId == conversationId }
                _state.update { it.copy(starredMessages = starred) }
            } catch (_: Exception) { /* soft-fail */ }
        }
    }

    fun onToggleMute() {
        val next = !_state.value.isMuted
        viewModelScope.launch {
            try {
                repository.toggleMute(conversationId, next)
                _state.update { it.copy(isMuted = next) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun onRequestExit() {
        _state.update { it.copy(confirmingExit = true) }
    }

    fun onDismissExit() {
        _state.update { it.copy(confirmingExit = false) }
    }

    fun onExitConfirmed() {
        viewModelScope.launch {
            try {
                repository.leaveConversation(conversationId)
                _state.update { it.copy(confirmingExit = false, navigateAwayAfterAction = true) }
            } catch (e: Exception) {
                _state.update { it.copy(confirmingExit = false, error = e.message) }
            }
        }
    }

    fun onRequestArchive() {
        _state.update { it.copy(confirmingArchive = true) }
    }

    fun onDismissArchive() {
        _state.update { it.copy(confirmingArchive = false) }
    }

    fun onArchiveConfirmed() {
        viewModelScope.launch {
            try {
                repository.archiveConversation(conversationId, true)
                _state.update { it.copy(confirmingArchive = false, navigateAwayAfterAction = true) }
            } catch (e: Exception) {
                _state.update { it.copy(confirmingArchive = false, error = e.message) }
            }
        }
    }

    fun consumeNavigateAway() {
        _state.update { it.copy(navigateAwayAfterAction = false) }
    }

    val isGroup: Boolean
        get() {
            val t = _state.value.conversation?.type ?: return false
            return t == ConversationType.GROUP ||
                t == ConversationType.CLASS ||
                t == ConversationType.DEPARTMENT
        }
}
