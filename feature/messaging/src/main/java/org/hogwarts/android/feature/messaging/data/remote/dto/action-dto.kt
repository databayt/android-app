package org.hogwarts.android.feature.messaging.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditMessageBody(val content: String)

@Serializable
data class ReactionBody(val emoji: String)

@Serializable
data class StarBody(val starred: Boolean)

@Serializable
data class ForwardBody(
    @SerialName("target_conversation_ids") val targetConversationIds: List<String>,
)

@Serializable
data class PinBody(val pinned: Boolean)

@Serializable
data class MuteBody(val muted: Boolean)

@Serializable
data class ArchiveBody(val archived: Boolean)

@Serializable
data class ActionSuccessResponse(val success: Boolean = true)

@Serializable
data class ForwardResponse(
    val success: Boolean = true,
    val forwarded: List<String> = emptyList(),
)

@Serializable
data class MessageSearchResultDto(
    val id: String,
    @SerialName("conversationId") val conversationId: String,
    @SerialName("conversationTitle") val conversationTitle: String = "",
    @SerialName("conversationType") val conversationType: String = "direct",
    @SerialName("senderId") val senderId: String = "",
    @SerialName("senderName") val senderName: String = "",
    val content: String,
    @SerialName("sentAt") val sentAt: String,
)

@Serializable
data class MessageSearchResponse(
    val data: List<MessageSearchResultDto>,
    val total: Int = 0,
)

@Serializable
data class StarredMessageDto(
    val id: String,
    @SerialName("starredAt") val starredAt: String,
    val message: StarredMessageInnerDto,
)

@Serializable
data class StarredMessageInnerDto(
    val id: String,
    @SerialName("conversationId") val conversationId: String,
    @SerialName("conversationTitle") val conversationTitle: String = "",
    @SerialName("conversationType") val conversationType: String = "direct",
    @SerialName("senderId") val senderId: String = "",
    @SerialName("senderName") val senderName: String = "",
    val content: String,
    @SerialName("contentType") val contentType: String = "text",
    @SerialName("sentAt") val sentAt: String,
)

@Serializable
data class StarredMessagesResponse(
    val data: List<StarredMessageDto>,
    val total: Int = 0,
)
