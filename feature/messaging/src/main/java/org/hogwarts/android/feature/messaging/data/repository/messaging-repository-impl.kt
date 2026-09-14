package org.hogwarts.android.feature.messaging.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.ConversationDao
import org.hogwarts.android.core.database.dao.MessageDao
import org.hogwarts.android.core.database.dao.PendingMessageDao
import org.hogwarts.android.core.database.entity.ConversationEntity
import org.hogwarts.android.core.database.entity.MessageEntity
import org.hogwarts.android.core.database.entity.PendingMessageEntity
import org.hogwarts.android.feature.messaging.data.remote.MessagingApi
import org.hogwarts.android.feature.messaging.data.remote.dto.ConversationDto
import org.hogwarts.android.feature.messaging.data.remote.dto.MessageDto
import org.hogwarts.android.feature.messaging.data.worker.PendingSendScheduler
import org.hogwarts.android.feature.messaging.domain.model.ChatSummary
import org.hogwarts.android.feature.messaging.domain.model.LastMessage
import org.hogwarts.android.feature.messaging.domain.model.MessagingViewer
import org.hogwarts.android.feature.messaging.domain.model.ThreadMessage
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class MessagingRepositoryImpl @Inject constructor(
    private val api: MessagingApi,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val pendingMessageDao: PendingMessageDao,
    private val tenantContext: TenantContext,
    private val sender: MessageSender,
    private val scheduler: PendingSendScheduler,
) : MessagingRepository {

    /**
     * Ids in the last list the server returned. The list route answers with the
     * whole inbox, so a conversation missing from it (left, archived) is dropped
     * from what the screen shows without deleting and re-inserting the table.
     */
    private val serverIds = MutableStateFlow<Set<String>?>(null)
    private val viewerLock = Mutex()
    private var cachedViewer: MessagingViewer? = null

    override val currentUserId: String? get() = tenantContext.userId

    override fun observeConversations(): Flow<List<ChatSummary>> {
        val schoolId = tenantContext.schoolId ?: return flowOf(emptyList())
        return combine(conversationDao.observeAll(schoolId), serverIds) { rows, ids ->
            rows.asSequence()
                .filter { ids == null || it.id in ids }
                .map { it.toSummary() }
                .sortedByDescending { it.lastMessageAt }
                .toList()
        }
    }

    override fun observeConversation(conversationId: String): Flow<ChatSummary?> {
        val schoolId = tenantContext.schoolId ?: return flowOf(null)
        return conversationDao.observeById(schoolId, conversationId).map { it?.toSummary() }
    }

    override suspend fun refreshConversations(): Boolean {
        val schoolId = tenantContext.schoolId ?: return false
        val body = request { api.getConversations() } ?: return false
        conversationDao.insertAll(body.data.map { it.toEntity(schoolId) })
        serverIds.value = body.data.mapTo(HashSet()) { it.id }
        return true
    }

    override fun observeMessages(conversationId: String): Flow<List<ThreadMessage>> {
        val schoolId = tenantContext.schoolId ?: return flowOf(emptyList())
        return messageDao.observeByConversation(schoolId, conversationId)
            .map { rows -> rows.map { it.toThreadMessage() } }
    }

    override suspend fun refreshMessages(conversationId: String): PageResult =
        fetchPage(conversationId, cursor = null)

    override suspend fun loadOlderMessages(conversationId: String, cursor: String): PageResult =
        fetchPage(conversationId, cursor)

    private suspend fun fetchPage(conversationId: String, cursor: String?): PageResult {
        val schoolId = tenantContext.schoolId ?: return PageResult.Failed
        val body = request { api.getMessages(conversationId, cursor = cursor) } ?: return PageResult.Failed
        messageDao.insertAll(body.data.map { it.toEntity(schoolId) })
        return PageResult.Loaded(body.nextCursor)
    }

    override suspend fun sendMessage(conversationId: String, content: String, replyToId: String?) {
        val schoolId = tenantContext.requireSchoolId()
        val nonce = UUID.randomUUID().toString()
        val now = Instant.now()
        val pending = PendingMessageEntity(
            id = nonce,
            schoolId = schoolId,
            conversationId = conversationId,
            content = content,
            replyToId = replyToId,
            status = "QUEUED",
            createdAt = now,
        )
        pendingMessageDao.insert(pending)
        messageDao.insert(
            MessageEntity(
                id = ThreadMessage.optimisticId(nonce),
                schoolId = schoolId,
                conversationId = conversationId,
                senderId = tenantContext.userId.orEmpty(),
                senderName = cachedViewer?.username ?: tenantContext.userName.orEmpty(),
                senderAvatarUrl = cachedViewer?.avatarUrl,
                content = content,
                status = "sending",
                sentAt = now,
                replyToId = replyToId,
                nonce = nonce,
                lastSyncAt = now,
            )
        )
        bumpConversation(schoolId, conversationId, content, now, "sending")
        if (!sender.deliver(pending)) scheduler.schedule()
    }

    override suspend fun retryMessage(messageId: String) {
        val row = messageDao.findById(messageId) ?: return
        val nonce = row.nonce ?: return
        val pending = pendingMessageDao.findById(nonce) ?: return
        if (!sender.deliver(pending)) scheduler.schedule()
    }

    override suspend fun markAsRead(conversationId: String) {
        val schoolId = tenantContext.schoolId ?: return
        conversationDao.updateUnreadCount(schoolId, conversationId, 0)
        messageDao.markAllRead(schoolId, conversationId)
        request { api.markAsRead(conversationId) }
    }

    override suspend fun viewer(): MessagingViewer? = viewerLock.withLock {
        cachedViewer?.let { return@withLock it }
        val body = request { api.getProfile() } ?: return@withLock null
        MessagingViewer(
            userId = body.id,
            username = body.username,
            avatarUrl = body.avatarUrl,
            bio = body.bio,
            schoolName = body.school?.name,
            schoolNameEn = body.school?.nameEn,
        ).also { cachedViewer = it }
    }

    private suspend fun bumpConversation(schoolId: String, conversationId: String, content: String, at: Instant, status: String) {
        conversationDao.updateLastMessage(
            schoolId = schoolId,
            conversationId = conversationId,
            content = content,
            senderName = cachedViewer?.username ?: tenantContext.userName.orEmpty(),
            sentAt = at.toEpochMilli(),
            status = status,
        )
    }

    /** The body of a 2xx, or null for anything else — including no network. */
    private suspend fun <T> request(call: suspend () -> retrofit2.Response<T>): T? = try {
        val response = call()
        if (response.isSuccessful) response.body() ?: @Suppress("UNCHECKED_CAST") (Unit as T) else null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.w(e, "messaging request failed")
        null
    }
}

// --- Mappers ---

internal fun ConversationDto.toEntity(schoolId: String): ConversationEntity {
    val sentAt = lastMessage?.sentAt?.toInstantOrNull()
    return ConversationEntity(
        id = id,
        schoolId = schoolId,
        title = title.orEmpty(),
        type = type,
        avatarUrl = avatarUrl,
        lastMessageContent = lastMessage?.content,
        lastMessageSenderName = lastMessage?.senderName,
        lastMessageSentAt = sentAt,
        lastMessageStatus = lastMessage?.status,
        unreadCount = unreadCount,
        isWhatsAppEnabled = whatsappEnabled,
        isPinned = isPinned,
        isMuted = isMuted,
        updatedAt = updatedAt.toInstantOrNull() ?: sentAt ?: Instant.EPOCH,
        lastSyncAt = Instant.now(),
    )
}

internal fun ConversationEntity.toSummary() = ChatSummary(
    id = id,
    type = type,
    title = title.ifEmpty { null },
    avatarUrl = avatarUrl,
    unreadCount = unreadCount,
    isPinned = isPinned,
    isMuted = isMuted,
    lastMessage = lastMessageContent?.let { content ->
        LastMessage(
            content = content,
            senderName = lastMessageSenderName.orEmpty(),
            status = lastMessageStatus ?: "sent",
            sentAt = lastMessageSentAt ?: updatedAt,
        )
    },
    lastMessageAt = updatedAt,
)

internal fun MessageDto.toEntity(schoolId: String) = MessageEntity(
    id = id,
    schoolId = schoolId,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    senderAvatarUrl = senderAvatarUrl,
    content = content,
    contentType = contentType,
    status = status,
    sentAt = sentAt.toInstantOrNull() ?: Instant.EPOCH,
    isRead = isRead,
    isEdited = isEdited,
    isDeleted = isDeleted,
    replyToId = replyToId,
    nonce = nonce,
    whatsappStatus = whatsappStatus,
    lastSyncAt = Instant.now(),
)

internal fun MessageEntity.toThreadMessage() = ThreadMessage(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    senderAvatarUrl = senderAvatarUrl,
    content = content,
    contentType = contentType,
    status = status,
    sentAt = sentAt,
    replyToId = replyToId,
    nonce = nonce,
)
