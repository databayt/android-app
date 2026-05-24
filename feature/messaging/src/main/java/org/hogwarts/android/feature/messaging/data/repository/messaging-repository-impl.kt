package org.hogwarts.android.feature.messaging.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.ConversationDao
import org.hogwarts.android.core.database.dao.MessageDao
import org.hogwarts.android.core.database.dao.PendingMessageDao
import org.hogwarts.android.core.database.entity.ConversationEntity
import org.hogwarts.android.core.database.entity.MessageEntity
import org.hogwarts.android.core.database.entity.PendingMessageEntity
import org.hogwarts.android.core.network.socket.SocketConnectionState
import org.hogwarts.android.core.network.socket.SocketManager
import org.hogwarts.android.feature.messaging.data.remote.MessagingApi
import org.hogwarts.android.feature.messaging.data.remote.dto.ArchiveBody
import org.hogwarts.android.feature.messaging.data.remote.dto.AttachmentDto
import org.hogwarts.android.feature.messaging.data.remote.dto.ContactDto
import org.hogwarts.android.feature.messaging.data.remote.dto.ContactGroupDto
import org.hogwarts.android.feature.messaging.data.remote.dto.ConversationDto
import org.hogwarts.android.feature.messaging.data.remote.dto.CreateConversationDto
import org.hogwarts.android.feature.messaging.data.remote.dto.EditMessageBody
import org.hogwarts.android.feature.messaging.data.remote.dto.ForwardBody
import org.hogwarts.android.feature.messaging.data.remote.dto.MessageDto
import org.hogwarts.android.feature.messaging.data.remote.dto.MuteBody
import org.hogwarts.android.feature.messaging.data.remote.dto.PinBody
import org.hogwarts.android.feature.messaging.data.remote.dto.ReactionBody
import org.hogwarts.android.feature.messaging.data.remote.dto.ReactionDto
import org.hogwarts.android.feature.messaging.data.remote.dto.SendMessageDto
import org.hogwarts.android.feature.messaging.data.remote.dto.StarBody
import org.hogwarts.android.feature.messaging.domain.model.Contact
import org.hogwarts.android.feature.messaging.domain.model.ContactCategory
import org.hogwarts.android.feature.messaging.domain.model.ContactGroup
import org.hogwarts.android.feature.messaging.domain.model.Conversation
import org.hogwarts.android.feature.messaging.domain.model.ConversationType
import org.hogwarts.android.feature.messaging.domain.model.Message
import org.hogwarts.android.feature.messaging.domain.model.MessageAttachment
import org.hogwarts.android.feature.messaging.domain.model.MessageReaction
import org.hogwarts.android.feature.messaging.domain.model.MessageStatus
import org.hogwarts.android.feature.messaging.domain.model.ReplyContext
import org.hogwarts.android.feature.messaging.domain.model.TypingIndicator
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagingRepositoryImpl @Inject constructor(
    private val api: MessagingApi,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val pendingMessageDao: PendingMessageDao,
    private val tenantContext: TenantContext,
    private val socketManager: SocketManager,
) : MessagingRepository {

    private val _typingIndicators = MutableStateFlow<Map<String, List<TypingIndicator>>>(emptyMap())
    private val _onlineUsers = MutableStateFlow<Set<String>>(emptySet())

    // In-memory cache for contacts (mirrors cachedGroupsRef in web contacts-panel.tsx:67)
    private data class ContactsCacheKey(val locale: String, val search: String?)
    private var contactsCache: Pair<ContactsCacheKey, List<ContactGroup>>? = null
    private var contactsCacheAt: Long = 0L
    private val contactsTtlMs = 60_000L

    override fun getContacts(locale: String, search: String?): Flow<Resource<List<ContactGroup>>> = flow {
        val key = ContactsCacheKey(locale, search?.takeIf { it.isNotBlank() })
        val cached = contactsCache
        val fresh = cached != null && cached.first == key &&
            (System.currentTimeMillis() - contactsCacheAt) < contactsTtlMs
        if (fresh) {
            emit(Resource.Success(cached!!.second))
            return@flow
        }
        emit(Resource.Loading(cached?.second ?: emptyList()))
        try {
            val response = api.getContacts(locale = locale, search = key.search)
            val groups = response.body()?.groups?.map { it.toDomain() } ?: emptyList()
            contactsCache = key to groups
            contactsCacheAt = System.currentTimeMillis()
            emit(Resource.Success(groups))
        } catch (t: Throwable) {
            emit(Resource.Error(t, cached?.second ?: emptyList()))
        }
    }

    override suspend fun getOrCreateDirectConversation(otherUserId: String): String {
        val response = api.createConversation(
            CreateConversationDto(
                type = "direct",
                participantIds = listOf(otherUserId),
            )
        )
        return response.body()?.id ?: throw IllegalStateException("Empty conversation response")
    }

    override fun getConversations(type: String?): Flow<Resource<List<Conversation>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                if (type != null) {
                    conversationDao.observeByType(schoolId, type)
                } else {
                    conversationDao.observeAll(schoolId)
                }.map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getConversations(type)
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                conversationDao.insertAll(dtos.map { it.toEntity(schoolId) })
            }
        )
    }

    override fun getMessages(conversationId: String): Flow<Resource<List<Message>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                messageDao.observeByConversation(schoolId, conversationId)
                    .map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getMessages(conversationId)
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                messageDao.insertAll(dtos.map { it.toEntity(schoolId) })
            }
        )
    }

    override suspend fun sendMessage(
        conversationId: String,
        content: String,
        replyToId: String?,
    ): Message {
        val schoolId = tenantContext.requireSchoolId()
        val nonce = UUID.randomUUID().toString()

        // Insert pending message for retry safety
        pendingMessageDao.insert(
            PendingMessageEntity(
                id = nonce,
                schoolId = schoolId,
                conversationId = conversationId,
                content = content,
                replyToId = replyToId,
                status = "SENDING",
                createdAt = Instant.now(),
            )
        )

        // Insert optimistic message into Room for immediate UI
        val optimisticEntity = MessageEntity(
            id = "pending_$nonce",
            schoolId = schoolId,
            conversationId = conversationId,
            senderId = tenantContext.userId ?: "",
            senderName = tenantContext.userName ?: "",
            content = content,
            status = "sending",
            sentAt = Instant.now(),
            replyToId = replyToId,
            nonce = nonce,
            lastSyncAt = Instant.now(),
        )
        messageDao.insert(optimisticEntity)

        return try {
            val response = api.sendMessage(
                conversationId,
                SendMessageDto(content = content, replyToId = replyToId, nonce = nonce),
            )
            val dto = response.body() ?: throw Exception("Failed to send message")
            val entity = dto.toEntity(schoolId)

            // Replace optimistic message with server response
            messageDao.insert(entity)
            // Clean up pending
            pendingMessageDao.delete(nonce)

            entity.toDomain()
        } catch (e: Exception) {
            // Mark as failed
            messageDao.updateStatus("pending_$nonce", "failed")
            pendingMessageDao.incrementRetry(nonce, "FAILED", Instant.now().toEpochMilli())
            throw e
        }
    }

    override suspend fun markAsRead(conversationId: String) {
        val schoolId = tenantContext.requireSchoolId()
        try {
            api.markAsRead(conversationId)
            socketManager.emit("message:read", mapOf("conversationId" to conversationId))
        } catch (_: Exception) { /* Best effort */ }
        messageDao.markAllRead(schoolId, conversationId)
        conversationDao.updateUnreadCount(schoolId, conversationId, 0)
    }

    override fun observeTypingIndicators(conversationId: String): Flow<List<TypingIndicator>> =
        _typingIndicators.map { it[conversationId] ?: emptyList() }

    override fun observeTypingConversations(): Flow<Set<String>> =
        _typingIndicators.map { map ->
            map.filterValues { it.isNotEmpty() }.keys
        }

    override fun observePresence(): Flow<Set<String>> = _onlineUsers.asStateFlow()

    override fun observeConnectionState(): StateFlow<SocketConnectionState> =
        socketManager.connectionState

    override fun sendTypingStart(conversationId: String) {
        socketManager.emit("typing:start", mapOf("conversationId" to conversationId))
    }

    override fun sendTypingStop(conversationId: String) {
        socketManager.emit("typing:stop", mapOf("conversationId" to conversationId))
    }

    override fun observeTotalUnreadCount(): Flow<Int> {
        val schoolId = tenantContext.requireSchoolId()
        return conversationDao.observeTotalUnreadCount(schoolId)
    }

    // --- Phase 2 action implementations ---

    override suspend fun editMessage(conversationId: String, messageId: String, content: String) {
        val resp = api.editMessage(conversationId, messageId, EditMessageBody(content))
        if (!resp.isSuccessful) throw IllegalStateException("Edit failed: ${resp.code()}")
    }

    override suspend fun deleteMessage(conversationId: String, messageId: String) {
        val resp = api.deleteMessage(conversationId, messageId)
        if (!resp.isSuccessful) throw IllegalStateException("Delete failed: ${resp.code()}")
    }

    override suspend fun addReaction(conversationId: String, messageId: String, emoji: String) {
        val resp = api.addReaction(conversationId, messageId, ReactionBody(emoji))
        if (!resp.isSuccessful) throw IllegalStateException("React failed: ${resp.code()}")
    }

    override suspend fun removeReaction(conversationId: String, messageId: String, emoji: String) {
        val resp = api.removeReaction(conversationId, messageId, emoji)
        if (!resp.isSuccessful) throw IllegalStateException("Un-react failed: ${resp.code()}")
    }

    override suspend fun toggleStar(conversationId: String, messageId: String, starred: Boolean) {
        val resp = api.toggleStar(conversationId, messageId, StarBody(starred))
        if (!resp.isSuccessful) throw IllegalStateException("Star toggle failed: ${resp.code()}")
    }

    override suspend fun forwardMessage(
        sourceConversationId: String,
        messageId: String,
        targetConversationIds: List<String>,
    ): List<String> {
        val resp = api.forwardMessage(
            sourceConversationId,
            messageId,
            ForwardBody(targetConversationIds),
        )
        if (!resp.isSuccessful) throw IllegalStateException("Forward failed: ${resp.code()}")
        return resp.body()?.forwarded ?: emptyList()
    }

    override suspend fun togglePin(conversationId: String, pinned: Boolean) {
        val resp = api.togglePin(conversationId, PinBody(pinned))
        if (!resp.isSuccessful) throw IllegalStateException("Pin toggle failed: ${resp.code()}")
        val schoolId = tenantContext.requireSchoolId()
        conversationDao.updatePinned(schoolId, conversationId, pinned)
    }

    override suspend fun toggleMute(conversationId: String, muted: Boolean) {
        val resp = api.toggleMute(conversationId, MuteBody(muted))
        if (!resp.isSuccessful) throw IllegalStateException("Mute toggle failed: ${resp.code()}")
        val schoolId = tenantContext.requireSchoolId()
        conversationDao.updateMuted(schoolId, conversationId, muted)
    }

    override suspend fun archiveConversation(conversationId: String, archived: Boolean) {
        val resp = api.archiveConversation(conversationId, ArchiveBody(archived))
        if (!resp.isSuccessful) throw IllegalStateException("Archive failed: ${resp.code()}")
    }

    override suspend fun leaveConversation(conversationId: String) {
        val resp = api.leaveConversation(conversationId)
        if (!resp.isSuccessful) throw IllegalStateException("Leave failed: ${resp.code()}")
    }

    override suspend fun searchMessages(
        query: String,
        limit: Int,
    ): List<org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult> {
        val resp = api.searchMessages(query, limit)
        val body = resp.body() ?: return emptyList()
        return body.data.map {
            org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult(
                id = it.id,
                conversationId = it.conversationId,
                conversationTitle = it.conversationTitle,
                conversationType = it.conversationType,
                senderId = it.senderId,
                senderName = it.senderName,
                content = it.content,
                sentAt = Instant.parse(it.sentAt),
            )
        }
    }

    override suspend fun searchConversationMessages(
        conversationId: String,
        query: String,
        limit: Int,
    ): List<org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult> {
        val resp = api.searchConversationMessages(conversationId, query, limit)
        val body = resp.body() ?: return emptyList()
        return body.data.map {
            org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult(
                id = it.id,
                conversationId = conversationId,
                senderId = it.senderId,
                senderName = it.senderName,
                content = it.content,
                sentAt = Instant.parse(it.sentAt),
            )
        }
    }

    override suspend fun getStarredMessages(
        limit: Int,
    ): List<org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult> {
        val resp = api.getStarredMessages(limit)
        val body = resp.body() ?: return emptyList()
        return body.data.map {
            org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult(
                id = it.message.id,
                conversationId = it.message.conversationId,
                conversationTitle = it.message.conversationTitle,
                conversationType = it.message.conversationType,
                senderId = it.message.senderId,
                senderName = it.message.senderName,
                content = it.message.content,
                contentType = it.message.contentType,
                sentAt = Instant.parse(it.message.sentAt),
                starredAt = Instant.parse(it.starredAt),
            )
        }
    }

    // Called by MessagingSocketHandler to update typing state
    fun updateTypingIndicators(conversationId: String, indicators: List<TypingIndicator>) {
        _typingIndicators.value = _typingIndicators.value.toMutableMap().apply {
            if (indicators.isEmpty()) remove(conversationId) else put(conversationId, indicators)
        }
    }

    // Called by MessagingSocketHandler to update presence
    fun updateOnlineUsers(users: Set<String>) {
        _onlineUsers.value = users
    }
}

// --- Mappers ---

private fun ConversationDto.toEntity(schoolId: String) = ConversationEntity(
    id = id,
    schoolId = schoolId,
    title = title,
    type = type,
    avatarUrl = avatarUrl,
    lastMessageContent = lastMessage?.content,
    lastMessageSenderName = lastMessage?.senderName,
    lastMessageSentAt = lastMessage?.sentAt?.let { Instant.parse(it) },
    lastMessageStatus = lastMessage?.status,
    unreadCount = unreadCount,
    isWhatsAppEnabled = whatsappEnabled,
    isPinned = isPinned,
    isMuted = isMuted,
    updatedAt = Instant.parse(updatedAt),
    lastSyncAt = Instant.now(),
)

private fun ConversationEntity.toDomain(): Conversation {
    val msgContent = lastMessageContent
    return Conversation(
        id = id,
        title = title,
        type = ConversationType.fromString(type),
        participants = emptyList(),
        lastMessage = if (msgContent != null) {
            Message(
                id = "",
                conversationId = id,
                senderId = "",
                senderName = lastMessageSenderName ?: "",
                content = msgContent,
                status = lastMessageStatus?.let { MessageStatus.fromString(it) } ?: MessageStatus.SENT,
                sentAt = lastMessageSentAt ?: updatedAt,
            )
        } else null,
        unreadCount = unreadCount,
        updatedAt = updatedAt,
        avatarUrl = avatarUrl,
        isWhatsAppEnabled = isWhatsAppEnabled,
        isPinned = isPinned,
        isMuted = isMuted,
    )
}

private fun MessageDto.toEntity(schoolId: String) = MessageEntity(
    id = id,
    schoolId = schoolId,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    senderAvatarUrl = senderAvatarUrl,
    content = content,
    contentType = contentType,
    status = status,
    sentAt = Instant.parse(sentAt),
    isRead = isRead,
    isEdited = isEdited,
    isDeleted = isDeleted,
    replyToId = replyToId,
    nonce = nonce,
    whatsappStatus = whatsappStatus,
    lastSyncAt = Instant.now(),
)

private fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    senderAvatarUrl = senderAvatarUrl,
    content = content,
    contentType = contentType,
    status = MessageStatus.fromString(status),
    sentAt = sentAt,
    isRead = isRead,
    isEdited = isEdited,
    isDeleted = isDeleted,
    replyToId = replyToId,
    nonce = nonce,
    whatsappStatus = whatsappStatus,
)

private fun AttachmentDto.toDomain(messageId: String) = MessageAttachment(
    id = id,
    messageId = messageId,
    fileName = fileName,
    fileUrl = fileUrl,
    mimeType = fileType,
    fileSizeBytes = fileSize,
    thumbnail = thumbnail,
    width = width,
    height = height,
)

private fun ReactionDto.toDomain(messageId: String) = MessageReaction(
    messageId = messageId,
    userId = userId,
    userName = userName,
    emoji = emoji,
)

private fun ContactGroupDto.toDomain(): ContactGroup {
    val cat = ContactCategory.fromKey(category) ?: ContactCategory.STAFF
    return ContactGroup(
        category = cat,
        contacts = contacts.map { it.toDomain(cat) },
    )
}

private fun ContactDto.toDomain(category: ContactCategory) = Contact(
    id = id,
    firstName = firstName,
    lastName = lastName,
    displayName = displayName,
    email = email,
    avatarUrl = image,
    role = role,
    category = ContactCategory.fromKey(this.category) ?: category,
    contextLabel = contextLabel,
    hasWhatsApp = hasWhatsApp,
)
