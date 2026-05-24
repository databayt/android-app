package org.hogwarts.android.feature.messaging.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.network.socket.SocketConnectionState
import org.hogwarts.android.core.network.socket.SocketManager
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.data.socket.MessagingSocketHandler
import org.hogwarts.android.feature.messaging.domain.config.getSidebarFilters
import org.hogwarts.android.feature.messaging.domain.model.Contact
import org.hogwarts.android.feature.messaging.domain.model.ContactGroup
import org.hogwarts.android.feature.messaging.domain.model.Conversation
import org.hogwarts.android.feature.messaging.domain.model.ConversationType
import org.hogwarts.android.feature.messaging.domain.model.SidebarFilter
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repository: MessagingRepository,
    private val socketManager: SocketManager,
    private val socketHandler: MessagingSocketHandler,
    private val tenantContext: TenantContext,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        val role = tenantContext.userRole?.name
        _uiState.update {
            it.copy(
                currentUserRole = role,
                availableFilters = getSidebarFilters(tenantContext.userRole),
            )
        }
        connectSocket()
        observeConnectionState()
        observePresence()
        observeTotalUnread()
        observeConversations()
        observeSearchAndContacts()
        observeTypingConversations()
    }

    private fun observeTypingConversations() {
        viewModelScope.launch {
            repository.observeTypingConversations().collect { typingIds ->
                val map = typingIds.associateWith { true }
                _uiState.update { current ->
                    val next = current.copy(typingConversations = map)
                    next.copy(contacts = enrichAndFilter(next.groups, next.conversations, next))
                }
            }
        }
    }

    private fun connectSocket() {
        val schoolId = tenantContext.schoolId ?: return
        val userId = tenantContext.userId ?: return
        socketManager.connect(userId, schoolId, userId)
        socketManager.joinRoom("school:$schoolId")
        socketManager.joinRoom("user:$userId")
        socketHandler.start()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            repository.observeConnectionState().collect { state ->
                _uiState.update { it.copy(isConnected = state == SocketConnectionState.CONNECTED) }
            }
        }
    }

    private fun observePresence() {
        viewModelScope.launch {
            repository.observePresence().collect { users ->
                _uiState.update { it.copy(onlineUsers = users) }
            }
        }
    }

    private fun observeTotalUnread() {
        viewModelScope.launch {
            repository.observeTotalUnreadCount().collect { count ->
                _uiState.update { it.copy(totalUnreadCount = count) }
            }
        }
    }

    private fun observeConversations() {
        viewModelScope.launch {
            repository.getConversations().collect { resource ->
                val list = resource.data ?: emptyList()
                _uiState.update { current ->
                    current.copy(
                        conversations = list,
                        contacts = enrichAndFilter(current.groups, list, current),
                    )
                }
            }
        }
    }

    private fun observeSearchAndContacts() {
        viewModelScope.launch {
            _searchQuery
                .debounce { if (it.isBlank() || it.length < 2) 0L else 300L }
                .distinctUntilChanged()
                .collectLatest { query ->
                    val effectiveSearch = query.takeIf { it.length >= 2 }
                    repository.getContacts(search = effectiveSearch).collect { resource ->
                        val groups = resource.data ?: emptyList()
                        _uiState.update { current ->
                            current.copy(
                                isLoading = resource is Resource.Loading,
                                groups = groups,
                                contacts = enrichAndFilter(groups, current.conversations, current),
                                error = (resource as? Resource.Error)?.error?.message,
                            )
                        }
                    }
                }
        }
    }

    fun onFilterChanged(filter: SidebarFilter) {
        _uiState.update { current ->
            current.copy(
                selectedFilter = filter,
                contacts = enrichAndFilter(current.groups, current.conversations, current.copy(selectedFilter = filter)),
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    suspend fun getOrCreateDirectConversationFor(userId: String): String =
        repository.getOrCreateDirectConversation(userId)

    /** Port of contacts-panel.tsx: allContacts (125-158) + filteredContacts (161-174) + sortedContacts (177-191) */
    private fun enrichAndFilter(
        groups: List<ContactGroup>,
        conversations: List<Conversation>,
        state: ContactsUiState,
    ): List<Contact> {
        val currentUserId = tenantContext.userId
        // Build direct-conversation lookup by other participant id
        val convByOtherUserId: Map<String, Conversation> = conversations
            .asSequence()
            .filter { it.type == ConversationType.DIRECT }
            .mapNotNull { conv ->
                conv.participants.firstOrNull { it.id != currentUserId }?.let { other ->
                    other.id to conv
                }
            }
            .toMap()

        // Flatten + enrich
        val seen = mutableSetOf<String>()
        val enriched = buildList {
            for (group in groups) {
                for (contact in group.contacts) {
                    if (!seen.add(contact.id)) continue
                    val conv = convByOtherUserId[contact.id]
                    val isTyping = conv?.id?.let { state.typingConversations[it] } ?: false
                    add(
                        contact.copy(
                            conversationId = conv?.id,
                            lastMessage = conv?.lastMessage?.content,
                            lastMessageAt = conv?.lastMessage?.sentAt ?: conv?.updatedAt,
                            lastMessageContentType = conv?.lastMessage?.contentType,
                            lastMessageHasAttachments = conv?.lastMessage?.attachments?.isNotEmpty() ?: false,
                            unreadCount = conv?.unreadCount ?: 0,
                            isPinned = conv?.isPinned ?: false,
                            isMuted = conv?.isMuted ?: false,
                            isTyping = isTyping,
                        )
                    )
                }
            }
        }

        // Filter
        val filtered = when (val f = state.selectedFilter) {
            SidebarFilter.All -> enriched
            SidebarFilter.Unread -> enriched.filter { it.unreadCount > 0 }
            SidebarFilter.Favourites -> enriched.filter { it.isPinned }
            is SidebarFilter.Category -> enriched.filter { it.category == f.category }
        }

        // Sort: pinned first, then by lastMessageAt desc, then alphabetical
        return filtered.sortedWith(
            compareByDescending<Contact> { it.isPinned }
                .thenByDescending { it.lastMessageAt?.toEpochMilli() ?: 0L }
                .thenBy { it.displayName.lowercase() }
        )
    }
}
