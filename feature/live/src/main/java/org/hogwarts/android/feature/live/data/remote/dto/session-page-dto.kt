package org.hogwarts.android.feature.live.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** `GET /api/mobile/live/sessions/{id}` — `/live/[id]`, display-ready. */
@Serializable
data class SessionPageDto(
    val id: String,
    val title: String,
    val `when`: String,
    val status: String,
    @SerialName("status_label") val statusLabel: String,
    @SerialName("scheduled_start") val scheduledStart: String,
    val description: String? = null,
    val recording: String = "none",
    @SerialName("can_join") val canJoin: Boolean = false,
    @SerialName("is_external") val isExternal: Boolean = false,
    @SerialName("meeting_url") val meetingUrl: String? = null,
    @SerialName("lesson_path") val lessonPath: String? = null,
    @SerialName("can_end") val canEnd: Boolean = false,
    val rows: List<RowDto> = emptyList(),
    @SerialName("attendance_note") val attendanceNote: String? = null,
    @SerialName("attendance_link") val attendanceLink: String? = null,
    val labels: Map<String, String> = emptyMap(),
    val references: ReferencesDto? = null,
)

@Serializable
data class RowDto(val label: String, val value: String)

@Serializable
data class LinkDto(
    val title: String,
    val url: String? = null,
    val path: String? = null,
    val badge: String? = null,
    val date: String? = null,
)

@Serializable
data class ReferencesDto(
    val title: String,
    @SerialName("lesson_label") val lessonLabel: String,
    val lesson: String? = null,
    val practice: String? = null,
    @SerialName("videos_title") val videosTitle: String,
    val videos: List<LinkDto> = emptyList(),
    @SerialName("materials_title") val materialsTitle: String,
    val materials: List<LinkDto> = emptyList(),
    @SerialName("exams_title") val examsTitle: String,
    val exams: List<LinkDto> = emptyList(),
    @SerialName("assignments_title") val assignmentsTitle: String,
    val assignments: List<LinkDto> = emptyList(),
    @SerialName("links_title") val linksTitle: String,
    val links: List<LinkDto> = emptyList(),
)

/** `POST /api/mobile/conference/{id}/join` — `performLiveClassJoin`'s result. */
@Serializable
data class JoinResponseDto(
    val success: Boolean = false,
    val data: JoinTicketDto? = null,
    val error: String? = null,
)

@Serializable
data class JoinTicketDto(
    val token: String,
    val wsUrl: String,
    val roomName: String,
    val identity: String,
    val role: String,
    val hostIdentity: String? = null,
    val roomConfig: JsonObject? = null,
    val expiresAt: String,
)
