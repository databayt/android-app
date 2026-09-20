package org.hogwarts.android.feature.live.domain.model

import java.time.Instant

/**
 * One online class, as `/live` lists them.
 *
 * `status` is the server's own `ConferenceStatus` string rather than an enum
 * of our own: the lifecycle lives in the web's state machine, and a phone that
 * narrowed it to the three states it knows would silently drop a fourth the
 * day the server grew one.
 */
data class LiveSession(
    val id: String,
    val title: String?,
    val status: String,
    val scheduledStart: Instant,
    val scheduledEnd: Instant,
    val actualStart: Instant?,
    val actualEnd: Instant?,
    val provider: String,
    /** Only an `external` session carries one; a room is reached with a ticket. */
    val meetingUrl: String?,
    val subjectName: String?,
    val subjectThumbnail: String?,
    val subjectColor: String?,
    val sectionName: String?,
    val gradeName: String?,
    val teacherName: String?,
    val teacherPhotoUrl: String?,
    val hasRecording: Boolean,
) {
    val isLive: Boolean get() = status == "live"
    val isScheduled: Boolean get() = status == "scheduled"
}

/** A recording of a past session. */
data class LiveRecording(
    val id: String,
    val sessionId: String,
    val status: String,
    val durationSeconds: Int?,
    val completedAt: Instant?,
) {
    /** Only a `ready` recording has an object behind it worth offering. */
    val isPlayable: Boolean get() = status == "ready"
}
