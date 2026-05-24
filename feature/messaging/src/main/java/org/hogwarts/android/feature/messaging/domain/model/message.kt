package org.hogwarts.android.feature.messaging.domain.model

import java.time.Instant

data class Conversation(
    val id: String,
    val title: String,
    val type: ConversationType,
    val participants: List<Participant>,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val updatedAt: Instant,
    val avatarUrl: String? = null,
    val isWhatsAppEnabled: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
)

enum class ConversationType {
    DIRECT,
    GROUP,
    CLASS,
    DEPARTMENT,
    ANNOUNCEMENT;

    companion object {
        fun fromString(value: String): ConversationType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: DIRECT
    }
}

data class Participant(
    val id: String,
    val name: String,
    val role: String? = null,
    val avatarUrl: String? = null,
)

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED;

    companion object {
        fun fromString(value: String): MessageStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: SENT
    }
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String? = null,
    val content: String,
    val contentType: String = "text",
    val sentAt: Instant,
    val status: MessageStatus = MessageStatus.SENT,
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val replyToId: String? = null,
    val replyTo: ReplyContext? = null,
    val attachments: List<MessageAttachment> = emptyList(),
    val reactions: List<MessageReaction> = emptyList(),
    val whatsappStatus: String? = null,
    val nonce: String? = null,
) {
    val preview: String
        get() = when {
            isDeleted -> "This message was deleted"
            contentType != "text" -> contentType.replaceFirstChar { it.uppercase() }
            content.length > 80 -> content.take(80) + "..."
            else -> content
        }
}

data class ReplyContext(
    val id: String,
    val content: String,
    val senderName: String,
)

data class MessageReaction(
    val messageId: String,
    val userId: String,
    val userName: String,
    val emoji: String,
)
