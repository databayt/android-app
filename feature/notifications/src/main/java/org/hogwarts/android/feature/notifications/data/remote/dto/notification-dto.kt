package org.hogwarts.android.feature.notifications.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** One row of `GET /api/mobile/notifications` — this route answers in snake_case. */
@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val priority: String? = null,
    val title: String = "",
    val body: String = "",
    /** `{ entityType, entityId, url? }` — the web card follows `url`. */
    val metadata: JsonElement? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("actor_name") val actorName: String? = null,
    @SerialName("actor_avatar") val actorAvatar: String? = null,
)

@Serializable
data class NotificationListResponse(
    val data: List<NotificationDto> = emptyList(),
    val total: Int = 0,
    @SerialName("unread_count") val unreadCount: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
)
