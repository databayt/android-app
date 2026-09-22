package org.hogwarts.android.feature.live.domain.model

import java.time.Instant

/**
 * Everything `/live` shows one reader — the web's `loadLiveLanding`, which the
 * page and `/api/mobile/live/landing` share, so this is the browser's data and
 * not a phone's reconstruction of it.
 */
data class LiveLanding(
    val viewer: LandingViewer,
    val policy: LandingPolicy,
    val live: List<LandingSession>,
    val upcoming: List<LandingSession>,
    /** Classes that already ran and this reader missed, newest first. */
    val catchUp: List<LandingSession>,
    /** Two recordings, a missed class's ranked above an attended one's. */
    val recordings: List<LandingSession>,
)

/** What the reader may do here — `landing/viewer.ts`, resolved on the server. */
data class LandingViewer(
    val role: String,
    val canSchedule: Boolean,
    val canConfigure: Boolean,
    val isHost: Boolean,
    val canJoin: Boolean,
    val canViewRecordings: Boolean,
    /** False when every row is this teacher's own class. */
    val showsTeacher: Boolean,
    /** False for a student, whose every row is their own section. */
    val showsSection: Boolean,
)

/** How the school teaches right now; it decides what the hero says. */
data class LandingPolicy(
    val deliveryMode: String,
    val isOnline: Boolean,
    val windowActive: Boolean,
    val provider: String,
    val degraded: Boolean,
)

/** Where a class sits in its own clock, resolved server-side. */
enum class LandingPhase {
    SOON, STARTED, ENDING, SCHEDULED, PAST;

    val isRunning: Boolean get() = this == STARTED || this == ENDING

    companion object {
        fun from(value: String): LandingPhase =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: SCHEDULED
    }
}

/** One class, already localized and with its time formatted in the school's zone. */
data class LandingSession(
    val id: String,
    val title: String,
    val teacherName: String,
    val teacherPhotoUrl: String?,
    val subjectName: String?,
    val sectionName: String?,
    val gradeName: String?,
    val chapterName: String?,
    val lessonName: String?,
    /** "10:54 ص" for today's classes, "8 سبتمبر" for past ones. */
    val scheduledStart: String,
    val isLive: Boolean,
    val phase: LandingPhase,
    val progressDone: Int?,
    val progressTotal: Int?,
    val imageUrl: String?,
    val color: String?,
    val hasRecording: Boolean,
)

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
