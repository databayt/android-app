package org.hogwarts.android.feature.messaging.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Wire shapes of hogwarts `src/app/api/mobile/conversations*` and `profile`,
 * checked against balqalam.com on 2026-09-14. The conversation and message
 * routes answer in camelCase (`unreadCount`, `sentAt`, `next_cursor` is the one
 * snake_case key); profile answers in snake_case. The previous DTOs asked for
 * snake_case everywhere and could not decode a single conversation.
 */

@Serializable
data class ConversationListResponse(
    val data: List<ConversationDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
data class ConversationDto(
    val id: String,
    val type: String = "direct",
    val title: String? = null,
    val avatarUrl: String? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val whatsappEnabled: Boolean = false,
    val updatedAt: String = "",
    val lastMessage: LastMessageDto? = null,
)

/** The list's last-message preview. It carries no sender id and no content type. */
@Serializable
data class LastMessageDto(
    val id: String = "",
    val content: String = "",
    val senderName: String = "",
    val status: String = "sent",
    val sentAt: String = "",
)

@Serializable
data class MessageListResponse(
    val data: List<MessageDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
data class MessageDto(
    val id: String,
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarUrl: String? = null,
    val content: String = "",
    val contentType: String = "text",
    val status: String = "sent",
    val sentAt: String = "",
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val replyToId: String? = null,
    val nonce: String? = null,
    val whatsappStatus: String? = null,
    /** Present when the server recognised the nonce and returned the stored id only. */
    val deduplicated: Boolean = false,
)

@Serializable
data class SendMessageBody(
    val content: String,
    @SerialName("reply_to_id") val replyToId: String? = null,
    val nonce: String? = null,
)

@Serializable
data class ProfileDto(
    val id: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    val school: ProfileSchoolDto? = null,
)

@Serializable
data class ProfileSchoolDto(
    val id: String = "",
    val name: String? = null,
    @SerialName("name_en") val nameEn: String? = null,
)
