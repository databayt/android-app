package org.hogwarts.android.feature.messaging.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.network.socket.SocketConnectionState
import org.hogwarts.android.feature.messaging.domain.model.ContactGroup
import org.hogwarts.android.feature.messaging.domain.model.Conversation
import org.hogwarts.android.feature.messaging.domain.model.Message
import org.hogwarts.android.feature.messaging.domain.model.TypingIndicator

interface MessagingRepository {

    fun getContacts(locale: String = "en", search: String? = null): Flow<Resource<List<ContactGroup>>>

    suspend fun getOrCreateDirectConversation(otherUserId: String): String

    fun getConversations(type: String? = null): Flow<Resource<List<Conversation>>>

    fun getMessages(conversationId: String): Flow<Resource<List<Message>>>

    suspend fun sendMessage(conversationId: String, content: String, replyToId: String? = null): Message

    suspend fun markAsRead(conversationId: String)

    fun observeTypingIndicators(conversationId: String): Flow<List<TypingIndicator>>

    /** Set of conversation IDs where someone (other than the current user) is typing right now. */
    fun observeTypingConversations(): Flow<Set<String>>

    fun observePresence(): Flow<Set<String>>

    fun observeConnectionState(): StateFlow<SocketConnectionState>

    fun sendTypingStart(conversationId: String)

    fun sendTypingStop(conversationId: String)

    fun observeTotalUnreadCount(): Flow<Int>

    // Message-level actions
    suspend fun editMessage(conversationId: String, messageId: String, content: String)
    suspend fun deleteMessage(conversationId: String, messageId: String)
    suspend fun addReaction(conversationId: String, messageId: String, emoji: String)
    suspend fun removeReaction(conversationId: String, messageId: String, emoji: String)
    suspend fun toggleStar(conversationId: String, messageId: String, starred: Boolean)
    suspend fun forwardMessage(
        sourceConversationId: String,
        messageId: String,
        targetConversationIds: List<String>,
    ): List<String>

    // Conversation-level actions
    suspend fun togglePin(conversationId: String, pinned: Boolean)
    suspend fun toggleMute(conversationId: String, muted: Boolean)
    suspend fun archiveConversation(conversationId: String, archived: Boolean)
    suspend fun leaveConversation(conversationId: String)

    // Search + starred
    suspend fun searchMessages(query: String, limit: Int = 30): List<org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult>
    suspend fun searchConversationMessages(
        conversationId: String,
        query: String,
        limit: Int = 50,
    ): List<org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult>
    suspend fun getStarredMessages(limit: Int = 50): List<org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult>
}
