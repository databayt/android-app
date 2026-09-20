package org.hogwarts.android.feature.live.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.live.domain.model.LiveRecording
import org.hogwarts.android.feature.live.domain.model.LiveSession
import java.time.Instant

@Serializable
data class LiveSessionDto(
    val id: String,
    val title: String? = null,
    val status: String,
    @SerialName("scheduled_start") val scheduledStart: String,
    @SerialName("scheduled_end") val scheduledEnd: String,
    @SerialName("actual_start") val actualStart: String? = null,
    @SerialName("actual_end") val actualEnd: String? = null,
    val provider: String = "livekit",
    @SerialName("meeting_url") val meetingUrl: String? = null,
    val visibility: String? = null,
    val subject: SubjectRefDto? = null,
    val section: SectionRefDto? = null,
    val teacher: TeacherRefDto? = null,
    @SerialName("has_recording") val hasRecording: Boolean = false,
) {
    fun toDomain() = LiveSession(
        id = id,
        title = title,
        status = status,
        scheduledStart = Instant.parse(scheduledStart),
        scheduledEnd = Instant.parse(scheduledEnd),
        actualStart = actualStart?.let(Instant::parse),
        actualEnd = actualEnd?.let(Instant::parse),
        provider = provider,
        meetingUrl = meetingUrl,
        subjectName = subject?.name,
        subjectThumbnail = subject?.thumbnail,
        subjectColor = subject?.color,
        sectionName = section?.name,
        gradeName = section?.grade,
        teacherName = teacher?.name,
        teacherPhotoUrl = teacher?.photoUrl,
        hasRecording = hasRecording,
    )
}

@Serializable
data class SubjectRefDto(
    val id: String,
    val name: String,
    val thumbnail: String? = null,
    val color: String? = null,
)

@Serializable
data class SectionRefDto(
    val id: String,
    val name: String,
    val grade: String? = null,
)

@Serializable
data class TeacherRefDto(
    val id: String,
    val name: String,
    @SerialName("photo_url") val photoUrl: String? = null,
)

@Serializable
data class LiveSessionListResponse(
    val data: List<LiveSessionDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
)

@Serializable
data class LiveRecordingDto(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    val status: String,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("completed_at") val completedAt: String? = null,
) {
    fun toDomain() = LiveRecording(
        id = id,
        sessionId = sessionId,
        status = status,
        durationSeconds = durationSeconds,
        completedAt = completedAt?.let(Instant::parse),
    )
}

@Serializable
data class LiveRecordingListResponse(val data: List<LiveRecordingDto> = emptyList())

@Serializable
data class RecordingUrlResponse(
    val url: String,
    @SerialName("expires_in") val expiresIn: Int = 0,
)
