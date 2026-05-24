package org.hogwarts.android.feature.messaging.data.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.ConversationDao
import org.hogwarts.android.core.database.dao.MessageDao
import org.hogwarts.android.core.database.entity.MessageEntity
import org.hogwarts.android.core.network.socket.SocketManager
import org.hogwarts.android.core.network.socket.SocketMessageDeliveredEvent
import org.hogwarts.android.core.network.socket.SocketMessageEvent
import org.hogwarts.android.core.network.socket.SocketMessageReadEvent
import org.hogwarts.android.core.network.socket.SocketMessageUpdateEvent
import org.hogwarts.android.core.network.socket.SocketPresenceEvent
import org.hogwarts.android.core.network.socket.SocketTypingEvent
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepositoryImpl
import org.hogwarts.android.feature.messaging.domain.model.TypingIndicator
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Listens to Socket.IO events and writes them to Room.
 * Decoupled from the repository -- this is the "event sink".
 */
@Singleton
class MessagingSocketHandler @Inject constructor(
    private val socketManager: SocketManager,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val tenantContext: TenantContext,
    private val json: Json,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val disposables = mutableListOf<() -> Unit>()
    private val typingMap = mutableMapOf<String, MutableMap<String, TypingIndicator>>()
    private val onlineUsers = mutableSetOf<String>()

    // Set by the repository after construction
    var repository: MessagingRepositoryImpl? = null

    fun start() {
        stop()

        disposables += socketManager.on("message:new") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketMessageEvent>(data.toString())
                    handleNewMessage(event)
                } catch (e: Exception) {
                    Timber.e(e, "Error handling message:new")
                }
            }
        }

        disposables += socketManager.on("message:updated") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketMessageUpdateEvent>(data.toString())
                    handleMessageUpdate(event)
                } catch (e: Exception) {
                    Timber.e(e, "Error handling message:updated")
                }
            }
        }

        disposables += socketManager.on("message:deleted") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketMessageUpdateEvent>(data.toString())
                    messageDao.softDelete(event.id)
                } catch (e: Exception) {
                    Timber.e(e, "Error handling message:deleted")
                }
            }
        }

        disposables += socketManager.on("message:read") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketMessageReadEvent>(data.toString())
                    event.messageIds.forEach { messageDao.updateStatus(it, "read") }
                } catch (e: Exception) {
                    Timber.e(e, "Error handling message:read")
                }
            }
        }

        disposables += socketManager.on("message:delivered") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketMessageDeliveredEvent>(data.toString())
                    messageDao.updateStatus(event.messageId, "delivered")
                } catch (e: Exception) {
                    Timber.e(e, "Error handling message:delivered")
                }
            }
        }

        disposables += socketManager.on("typing:start") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketTypingEvent>(data.toString())
                    if (event.userId == tenantContext.userId) return@launch
                    handleTypingStart(event)
                } catch (e: Exception) {
                    Timber.e(e, "Error handling typing:start")
                }
            }
        }

        disposables += socketManager.on("typing:stop") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketTypingEvent>(data.toString())
                    handleTypingStop(event.conversationId, event.userId)
                } catch (e: Exception) {
                    Timber.e(e, "Error handling typing:stop")
                }
            }
        }

        disposables += socketManager.on("presence:online") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketPresenceEvent>(data.toString())
                    onlineUsers.add(event.userId)
                    repository?.updateOnlineUsers(onlineUsers.toSet())
                } catch (e: Exception) {
                    Timber.e(e, "Error handling presence:online")
                }
            }
        }

        disposables += socketManager.on("presence:offline") { data ->
            scope.launch {
                try {
                    val event = json.decodeFromString<SocketPresenceEvent>(data.toString())
                    onlineUsers.remove(event.userId)
                    repository?.updateOnlineUsers(onlineUsers.toSet())
                } catch (e: Exception) {
                    Timber.e(e, "Error handling presence:offline")
                }
            }
        }
    }

    fun stop() {
        disposables.forEach { it.invoke() }
        disposables.clear()
    }

    private suspend fun handleNewMessage(event: SocketMessageEvent) {
        val schoolId = tenantContext.schoolId ?: return

        // Check for optimistic dedup by nonce
        val nonce = event.nonce
        if (nonce != null) {
            val existing = messageDao.findByNonce(schoolId, nonce)
            if (existing != null) {
                // Replace the optimistic message with server version
                messageDao.insert(
                    existing.copy(
                        id = event.id,
                        status = event.status,
                        lastSyncAt = Instant.now(),
                    )
                )
                return
            }
        }

        messageDao.insert(
            MessageEntity(
                id = event.id,
                schoolId = schoolId,
                conversationId = event.conversationId,
                senderId = event.senderId,
                senderName = event.senderName,
                content = event.content,
                contentType = event.contentType,
                status = event.status,
                sentAt = Instant.parse(event.sentAt),
                nonce = event.nonce,
                whatsappStatus = event.whatsappStatus,
                lastSyncAt = Instant.now(),
            )
        )
    }

    private suspend fun handleMessageUpdate(event: SocketMessageUpdateEvent) {
        event.content?.let { messageDao.updateContent(event.id, it) }
        event.status?.let { messageDao.updateStatus(event.id, it) }
        if (event.isDeleted == true) messageDao.softDelete(event.id)
    }

    private fun handleTypingStart(event: SocketTypingEvent) {
        val convTyping = typingMap.getOrPut(event.conversationId) { mutableMapOf() }
        convTyping[event.userId] = TypingIndicator(
            conversationId = event.conversationId,
            userId = event.userId,
            userName = event.userName,
            isTyping = true,
        )
        publishTypingState(event.conversationId)

        // Auto-clear after 5 seconds
        scope.launch {
            delay(5000)
            handleTypingStop(event.conversationId, event.userId)
        }
    }

    private fun handleTypingStop(conversationId: String, userId: String) {
        typingMap[conversationId]?.remove(userId)
        if (typingMap[conversationId]?.isEmpty() == true) typingMap.remove(conversationId)
        publishTypingState(conversationId)
    }

    private fun publishTypingState(conversationId: String) {
        val indicators = typingMap[conversationId]?.values?.toList() ?: emptyList()
        repository?.updateTypingIndicators(conversationId, indicators)
    }
}
