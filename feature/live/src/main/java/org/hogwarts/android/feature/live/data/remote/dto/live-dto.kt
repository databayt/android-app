package org.hogwarts.android.feature.live.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.live.domain.model.LandingPhase
import org.hogwarts.android.feature.live.domain.model.LandingPolicy
import org.hogwarts.android.feature.live.domain.model.LandingReadiness
import org.hogwarts.android.feature.live.domain.model.LandingSession
import org.hogwarts.android.feature.live.domain.model.LandingViewer
import org.hogwarts.android.feature.live.domain.model.LiveLanding
import org.hogwarts.android.feature.live.domain.model.LiveRecording
import java.time.Instant

/** `/api/mobile/live/landing` — the web's `loadLiveLanding`, as JSON. */
@Serializable
data class LiveLandingResponse(
    val viewer: LandingViewerDto,
    val policy: LandingPolicyDto,
    val live: List<LandingSessionDto> = emptyList(),
    val upcoming: List<LandingSessionDto> = emptyList(),
    @SerialName("catch_up") val catchUp: List<LandingSessionDto> = emptyList(),
    val recordings: List<LandingSessionDto> = emptyList(),
    val readiness: LandingReadinessDto? = null,
) {
    fun toDomain() = LiveLanding(
        viewer = viewer.toDomain(),
        policy = policy.toDomain(),
        live = live.map { it.toDomain() },
        upcoming = upcoming.map { it.toDomain() },
        catchUp = catchUp.map { it.toDomain() },
        recordings = recordings.map { it.toDomain() },
        readiness = readiness?.toDomain(),
    )
}

@Serializable
data class LandingCoverageDto(
    val total: Int = 0,
    val covered: Int = 0,
    @SerialName("gap_count") val gapCount: Int = 0,
)

@Serializable
data class LandingReadinessDto(
    @SerialName("livekit_ready") val livekitReady: Boolean = false,
    @SerialName("recording_ready") val recordingReady: Boolean = false,
    @SerialName("has_fallback") val hasFallback: Boolean = false,
    val coverage: LandingCoverageDto? = null,
) {
    fun toDomain() = LandingReadiness(
        livekitReady = livekitReady,
        recordingReady = recordingReady,
        hasFallback = hasFallback,
        coverageTotal = coverage?.total,
        coverageCovered = coverage?.covered,
        coverageGapCount = coverage?.gapCount,
    )
}

@Serializable
data class LandingViewerDto(
    val role: String = "",
    @SerialName("can_schedule") val canSchedule: Boolean = false,
    @SerialName("can_configure") val canConfigure: Boolean = false,
    @SerialName("is_host") val isHost: Boolean = false,
    @SerialName("can_join") val canJoin: Boolean = false,
    @SerialName("can_view_recordings") val canViewRecordings: Boolean = false,
    @SerialName("shows_teacher") val showsTeacher: Boolean = true,
    @SerialName("shows_section") val showsSection: Boolean = true,
) {
    fun toDomain() = LandingViewer(
        role = role,
        canSchedule = canSchedule,
        canConfigure = canConfigure,
        isHost = isHost,
        canJoin = canJoin,
        canViewRecordings = canViewRecordings,
        showsTeacher = showsTeacher,
        showsSection = showsSection,
    )
}

@Serializable
data class LandingPolicyDto(
    @SerialName("delivery_mode") val deliveryMode: String = "physical",
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("window_active") val windowActive: Boolean = false,
    val provider: String = "external",
    val degraded: Boolean = false,
) {
    fun toDomain() = LandingPolicy(
        deliveryMode = deliveryMode,
        isOnline = isOnline,
        windowActive = windowActive,
        provider = provider,
        degraded = degraded,
    )
}

@Serializable
data class LandingProgressDto(val done: Int, val total: Int)

@Serializable
data class LandingSessionDto(
    val id: String,
    val title: String = "",
    @SerialName("teacher_name") val teacherName: String = "",
    @SerialName("teacher_photo_url") val teacherPhotoUrl: String? = null,
    @SerialName("subject_name") val subjectName: String? = null,
    @SerialName("section_name") val sectionName: String? = null,
    @SerialName("grade_name") val gradeName: String? = null,
    @SerialName("chapter_name") val chapterName: String? = null,
    @SerialName("lesson_name") val lessonName: String? = null,
    @SerialName("scheduled_start") val scheduledStart: String = "",
    @SerialName("is_live") val isLive: Boolean = false,
    val phase: String = "scheduled",
    val progress: LandingProgressDto? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val color: String? = null,
    @SerialName("has_recording") val hasRecording: Boolean = false,
) {
    fun toDomain() = LandingSession(
        id = id,
        title = title,
        teacherName = teacherName,
        teacherPhotoUrl = teacherPhotoUrl,
        subjectName = subjectName,
        sectionName = sectionName,
        gradeName = gradeName,
        chapterName = chapterName,
        lessonName = lessonName,
        scheduledStart = scheduledStart,
        isLive = isLive,
        phase = LandingPhase.from(phase),
        progressDone = progress?.done,
        progressTotal = progress?.total,
        imageUrl = imageUrl,
        color = color,
        hasRecording = hasRecording,
    )
}

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
