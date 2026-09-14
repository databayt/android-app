package org.hogwarts.android.feature.announcements.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of `GET /api/mobile/announcements` (hogwarts
 * `src/app/api/mobile/announcements/route.ts`). Every field past `author_avatar`
 * was added in 2026-09 and defaults, so an older server still decodes.
 */
@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String? = null,
    val content: String? = null,
    val priority: String = "normal",
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_avatar") val authorAvatar: String? = null,
    val scope: String = "school",
    @SerialName("target_role") val targetRole: String? = null,
    @SerialName("class_id") val classId: String? = null,
    @SerialName("is_published") val isPublished: Boolean = true,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    val lang: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
)

@Serializable
data class AnnouncementListResponse(
    val data: List<AnnouncementDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
)
