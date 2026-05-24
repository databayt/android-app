package org.hogwarts.android.feature.messaging.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val title: String,
    val type: String,
    val participants: List<ParticipantDto> = emptyList(),
    @SerialName("last_message") val lastMessage: MessageDto? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("whatsapp_enabled") val whatsappEnabled: Boolean = false,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("is_muted") val isMuted: Boolean = false,
)

@Serializable
data class ParticipantDto(
    val id: String,
    val name: String,
    val role: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("sender_name") val senderName: String,
    @SerialName("sender_avatar_url") val senderAvatarUrl: String? = null,
    val content: String,
    @SerialName("content_type") val contentType: String = "text",
    val status: String = "sent",
    @SerialName("sent_at") val sentAt: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("is_edited") val isEdited: Boolean = false,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("reply_to_id") val replyToId: String? = null,
    @SerialName("reply_to") val replyTo: ReplyToDto? = null,
    val attachments: List<AttachmentDto> = emptyList(),
    val reactions: List<ReactionDto> = emptyList(),
    @SerialName("whatsapp_status") val whatsappStatus: String? = null,
    val nonce: String? = null,
)

@Serializable
data class ReplyToDto(
    val id: String,
    val content: String,
    @SerialName("sender_name") val senderName: String,
)

@Serializable
data class AttachmentDto(
    val id: String,
    @SerialName("file_name") val fileName: String,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("file_size") val fileSize: Long = 0,
    @SerialName("file_type") val fileType: String,
    val thumbnail: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class ReactionDto(
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    val emoji: String,
)

@Serializable
data class ConversationListResponse(
    val data: List<ConversationDto>,
)

@Serializable
data class MessageListResponse(
    val data: List<MessageDto>,
)

@Serializable
data class SendMessageDto(
    val content: String,
    @SerialName("content_type") val contentType: String = "text",
    @SerialName("reply_to_id") val replyToId: String? = null,
    val nonce: String? = null,
)

@Serializable
data class CreateConversationDto(
    val title: String? = null,
    val type: String = "direct",
    @SerialName("participant_ids") val participantIds: List<String>,
)

@Serializable
data class CreateConversationResponse(
    val id: String,
)
