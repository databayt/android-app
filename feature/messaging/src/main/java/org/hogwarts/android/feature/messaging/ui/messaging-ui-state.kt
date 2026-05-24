package org.hogwarts.android.feature.messaging.ui

import org.hogwarts.android.core.network.socket.SocketConnectionState
import org.hogwarts.android.feature.messaging.domain.model.Contact
import org.hogwarts.android.feature.messaging.domain.model.ContactGroup
import org.hogwarts.android.feature.messaging.domain.model.Conversation
import org.hogwarts.android.feature.messaging.domain.model.Message
import org.hogwarts.android.feature.messaging.domain.model.SidebarFilter

data class ContactsUiState(
    val isLoading: Boolean = true,
    val groups: List<ContactGroup> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val error: String? = null,
    val selectedFilter: SidebarFilter = SidebarFilter.All,
    val availableFilters: List<SidebarFilter> = listOf(SidebarFilter.All),
    val searchQuery: String = "",
    val isConnected: Boolean = false,
    val onlineUsers: Set<String> = emptySet(),
    val typingConversations: Map<String, Boolean> = emptyMap(),
    val unreadCount: Int = 0,
    val totalUnreadCount: Int = 0,
    val currentUserRole: String? = null,
)

data class ChatUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    val messageText: String = "",
    val isSending: Boolean = false,
    val conversationTitle: String = "",
    val conversationType: String = "DIRECT",
    val isWhatsAppEnabled: Boolean = false,
    val connectionState: SocketConnectionState = SocketConnectionState.DISCONNECTED,
    val typingUsers: List<String> = emptyList(),
    val isOtherOnline: Boolean = false,
    val lastSeenAt: String? = null,
    val replyToMessage: Message? = null,
    val isAtBottom: Boolean = true,
    val newUnreadCount: Int = 0,
    val currentUserId: String = "",
    // Phase 2: action state
    val actionTarget: Message? = null,
    val editingMessage: Message? = null,
    val emojiPickerTarget: Message? = null,
    val confirmingDelete: Message? = null,
    val confirmingLeave: Boolean = false,
    val confirmingArchive: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val starredMessageIds: Set<String> = emptySet(),
    /** Android string resource id for a one-shot toast. Use stringResource(id) to render. */
    val toastResId: Int? = null,
)
