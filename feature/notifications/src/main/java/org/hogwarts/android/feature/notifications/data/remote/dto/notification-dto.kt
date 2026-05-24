package org.hogwarts.android.feature.notifications.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val priority: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("deep_link") val deepLink: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("actor_name") val actorName: String? = null,
    @SerialName("actor_avatar") val actorAvatar: String? = null
)

@Serializable
data class NotificationListResponse(
    val data: List<NotificationDto>,
    val total: Int = 0,
    @SerialName("unread_count") val unreadCount: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 30
)
