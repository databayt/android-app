package org.hogwarts.android.core.network.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocketMessageEvent(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("sender_name") val senderName: String,
    val content: String,
    @SerialName("content_type") val contentType: String = "text",
    val status: String = "sent",
    @SerialName("sent_at") val sentAt: String,
    val nonce: String? = null,
    @SerialName("whatsapp_status") val whatsappStatus: String? = null,
)

@Serializable
data class SocketMessageUpdateEvent(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    val content: String? = null,
    val status: String? = null,
    @SerialName("is_edited") val isEdited: Boolean? = null,
    @SerialName("is_deleted") val isDeleted: Boolean? = null,
)

@Serializable
data class SocketMessageReadEvent(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("message_ids") val messageIds: List<String> = emptyList(),
    @SerialName("reader_id") val readerId: String,
)

@Serializable
data class SocketMessageReactionEvent(
    @SerialName("message_id") val messageId: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    val emoji: String,
    val action: String, // "add" or "remove"
)

@Serializable
data class SocketTypingEvent(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
)

@Serializable
data class SocketPresenceEvent(
    @SerialName("user_id") val userId: String,
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
)

@Serializable
data class SocketConversationUpdateEvent(
    val id: String,
    val title: String? = null,
    @SerialName("unread_count") val unreadCount: Int? = null,
    @SerialName("last_message") val lastMessage: SocketMessageEvent? = null,
)

@Serializable
data class SocketMessageDeliveredEvent(
    @SerialName("message_id") val messageId: String,
    @SerialName("conversation_id") val conversationId: String,
)
