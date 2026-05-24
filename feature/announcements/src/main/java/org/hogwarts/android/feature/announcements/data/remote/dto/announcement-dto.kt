package org.hogwarts.android.feature.announcements.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val content: String,
    val type: String,
    val category: String? = null,
    @SerialName("author_id") val authorId: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("target_audience") val targetAudience: String? = null,
    val date: String,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val venue: String? = null,
    @SerialName("is_important") val isImportant: Boolean = false,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class AnnouncementListResponse(
    val data: List<AnnouncementDto>
)
