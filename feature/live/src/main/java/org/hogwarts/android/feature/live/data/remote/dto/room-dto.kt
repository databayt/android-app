package org.hogwarts.android.feature.live.data.remote.dto

import kotlinx.serialization.Serializable

/** One saved room-chat message — hogwarts `live/chat-core.ts` `RoomChatMessage`. */
@Serializable
data class RoomChatMessageDto(
    val id: String,
    val identity: String,
    val name: String = "",
    val text: String,
    /** Epoch ms. */
    val at: Long,
)

@Serializable
data class RoomChatPageDto(
    val messages: List<RoomChatMessageDto> = emptyList(),
    val hasMore: Boolean = false,
)

/** `GET /api/mobile/conference/{id}/chat` */
@Serializable
data class RoomChatResponseDto(
    val success: Boolean = false,
    val data: RoomChatPageDto? = null,
    val error: String? = null,
)

/** `POST /api/mobile/conference/{id}/chat` — save a message already sent over the room. */
@Serializable
data class RecordChatRequestDto(val messageId: String, val text: String)

/** `POST /api/mobile/conference/{id}/mute` — every student, or one with [identity]. */
@Serializable
data class MuteRequestDto(val identity: String? = null)

@Serializable
data class SimpleResultDto(val success: Boolean = false, val error: String? = null)

/** `GET /api/mobile/conference/{id}/board` — the host's saved board; strokes are the wire's `Stroke` objects. */
@Serializable
data class BoardResponseDto(
    val success: Boolean = false,
    val data: BoardMemoryDto? = null,
    val error: String? = null,
)

@Serializable
data class BoardMemoryDto(val strokes: kotlinx.serialization.json.JsonArray = kotlinx.serialization.json.JsonArray(emptyList()))

/** `PUT /api/mobile/conference/{id}/board` — save the page (the last 400 strokes). */
@Serializable
data class BoardSaveDto(val strokes: kotlinx.serialization.json.JsonArray)
