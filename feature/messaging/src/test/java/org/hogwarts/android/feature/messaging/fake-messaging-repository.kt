package org.hogwarts.android.feature.messaging

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.hogwarts.android.feature.messaging.data.local.ConversationDrafts
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.data.repository.PageResult
import org.hogwarts.android.feature.messaging.domain.model.ChatSummary
import org.hogwarts.android.feature.messaging.domain.model.LastMessage
import org.hogwarts.android.feature.messaging.domain.model.MessagingViewer
import org.hogwarts.android.feature.messaging.domain.model.ThreadMessage
import java.time.Instant

/** In-memory stand-in for Room + the mobile API. */
class FakeMessagingRepository(
    override val currentUserId: String? = ME,
) : MessagingRepository {
    val chats = MutableStateFlow<List<ChatSummary>>(emptyList())
    val messages = MutableStateFlow<Map<String, List<ThreadMessage>>>(emptyMap())
    var refreshOk = true
    var refreshCount = 0
    var messageRefreshCount = 0
    var newestCursor: String? = null
    val olderPages = mutableMapOf<String, Pair<List<ThreadMessage>, String?>>()
    val markedRead = mutableListOf<String>()
    val sent = mutableListOf<String>()
    val retried = mutableListOf<String>()
    var serverAccepts = true
    var viewer: MessagingViewer? = MessagingViewer(ME, "Minerva", null, "Deputy head", "Hogwarts", null)

    override fun observeConversations(): Flow<List<ChatSummary>> = chats
    override fun observeConversation(conversationId: String): Flow<ChatSummary?> =
        chats.map { list -> list.firstOrNull { it.id == conversationId } }

    override suspend fun refreshConversations(): Boolean {
        refreshCount++
        return refreshOk
    }

    override fun observeMessages(conversationId: String): Flow<List<ThreadMessage>> =
        messages.map { it[conversationId].orEmpty() }

    override suspend fun refreshMessages(conversationId: String): PageResult {
        messageRefreshCount++
        return if (refreshOk) PageResult.Loaded(newestCursor) else PageResult.Failed
    }

    override suspend fun loadOlderMessages(conversationId: String, cursor: String): PageResult {
        val (page, next) = olderPages[cursor] ?: return PageResult.Failed
        messages.update { it + (conversationId to (page + it[conversationId].orEmpty())) }
        return PageResult.Loaded(next)
    }

    override suspend fun sendMessage(conversationId: String, content: String, replyToId: String?) {
        sent += content
        val row = message("pending_${sent.size}", conversationId, ME, content, Instant.parse("2026-09-14T10:00:00Z"), if (serverAccepts) "sent" else "failed")
        messages.update { it + (conversationId to (it[conversationId].orEmpty() + row)) }
    }

    override suspend fun retryMessage(messageId: String) {
        retried += messageId
    }

    override suspend fun markAsRead(conversationId: String) {
        markedRead += conversationId
        chats.update { list -> list.map { if (it.id == conversationId) it.copy(unreadCount = 0) else it } }
    }

    override suspend fun viewer(): MessagingViewer? = viewer

    companion object {
        const val ME = "user-me"
    }
}

class FakeDrafts : ConversationDrafts {
    val store = MutableStateFlow<Map<String, String>>(emptyMap())
    override fun observe(conversationId: String): Flow<String> = store.map { it[conversationId].orEmpty() }
    override suspend fun save(conversationId: String, text: String) = store.update { it + (conversationId to text) }
}

fun chat(
    id: String,
    title: String?,
    type: String = "direct",
    unread: Int = 0,
    pinned: Boolean = false,
    last: String = "",
    lastSender: String = "",
    status: String = "sent",
    at: String = "2026-09-14T08:00:00Z",
) = ChatSummary(
    id = id,
    type = type,
    title = title,
    avatarUrl = null,
    unreadCount = unread,
    isPinned = pinned,
    isMuted = false,
    lastMessage = LastMessage(last, lastSender, status, Instant.parse(at)),
    lastMessageAt = Instant.parse(at),
)

fun message(
    id: String,
    conversationId: String,
    senderId: String,
    content: String,
    at: Instant,
    status: String = "sent",
    senderName: String = if (senderId == FakeMessagingRepository.ME) "Minerva" else "Other",
    replyToId: String? = null,
    contentType: String = "text",
) = ThreadMessage(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    senderAvatarUrl = null,
    content = content,
    contentType = contentType,
    status = status,
    sentAt = at,
    replyToId = replyToId,
    nonce = null,
)
