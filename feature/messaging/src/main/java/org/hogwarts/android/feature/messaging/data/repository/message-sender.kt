package org.hogwarts.android.feature.messaging.data.repository

import org.hogwarts.android.core.database.dao.MessageDao
import org.hogwarts.android.core.database.dao.PendingMessageDao
import org.hogwarts.android.core.database.entity.MessageEntity
import org.hogwarts.android.core.database.entity.PendingMessageEntity
import org.hogwarts.android.feature.messaging.data.remote.MessagingApi
import org.hogwarts.android.feature.messaging.data.remote.dto.SendMessageBody
import org.hogwarts.android.feature.messaging.domain.model.ThreadMessage
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * The one place a queued message meets the server, shared by the send button
 * and the background worker. A message is marked sent only when the POST
 * answers 2xx; anything else leaves its pending row queued and its bubble
 * `failed`. The nonce rides along so a resend the server already stored comes
 * back as the same message instead of a second one.
 */
@Singleton
class MessageSender @Inject constructor(
    private val api: MessagingApi,
    private val messageDao: MessageDao,
    private val pendingMessageDao: PendingMessageDao,
) {
    /** True when the server took the message. */
    suspend fun deliver(pending: PendingMessageEntity): Boolean {
        val optimisticId = ThreadMessage.optimisticId(pending.id)
        messageDao.updateStatus(optimisticId, "sending")
        val response = try {
            api.sendMessage(
                pending.conversationId,
                SendMessageBody(content = pending.content, replyToId = pending.replyToId, nonce = pending.id),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "send %s failed before the server answered", pending.id)
            null
        }
        val body = response?.body()
        if (response == null || !response.isSuccessful || body == null) {
            messageDao.updateStatus(optimisticId, "failed")
            pendingMessageDao.incrementRetry(pending.id, "FAILED", Instant.now().toEpochMilli())
            return false
        }
        val optimistic = messageDao.findById(optimisticId)
        val confirmed = MessageEntity(
            id = body.id,
            schoolId = pending.schoolId,
            conversationId = body.conversationId.ifEmpty { pending.conversationId },
            senderId = body.senderId.ifEmpty { optimistic?.senderId.orEmpty() },
            senderName = body.senderName.ifEmpty { optimistic?.senderName.orEmpty() },
            senderAvatarUrl = body.senderAvatarUrl ?: optimistic?.senderAvatarUrl,
            content = body.content.ifEmpty { pending.content },
            contentType = body.contentType,
            // A fresh row says "sent"; a deduplicated answer carries no status at all.
            status = if (body.deduplicated) "sent" else body.status.ifEmpty { "sent" },
            sentAt = body.sentAt.toInstantOrNull() ?: optimistic?.sentAt ?: pending.createdAt,
            replyToId = pending.replyToId,
            nonce = pending.id,
            lastSyncAt = Instant.now(),
        )
        messageDao.insert(confirmed)
        messageDao.deleteById(optimisticId)
        pendingMessageDao.delete(pending.id)
        return true
    }
}

internal fun String.toInstantOrNull(): Instant? =
    if (isEmpty()) null else runCatching { Instant.parse(this) }.getOrNull()
