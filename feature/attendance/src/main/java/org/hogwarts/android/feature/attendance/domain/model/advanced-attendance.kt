package org.hogwarts.android.feature.attendance.domain.model

import java.time.Instant

/**
 * EPIC-27: Advanced Attendance domain models.
 *
 * Covers gamification (badges/streaks), hall pass system,
 * interventions, attendance methods, and enhanced analytics.
 */

// ─── Gamification ────────────────────────────────────────────────

/**
 * A badge earned through attendance achievements.
 */
data class AttendanceBadge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val earnedAt: Instant? = null,
    val type: BadgeType
) {
    val isEarned: Boolean get() = earnedAt != null
}

/**
 * Types of attendance badges that can be earned.
 */
enum class BadgeType {
    PERFECT_WEEK,
    MONTH_STREAK,
    PERFECT_MONTH,
    SEMESTER_STAR,
    EARLY_BIRD;

    companion object {
        fun fromString(value: String): BadgeType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PERFECT_WEEK
    }
}

/**
 * Tracks consecutive attendance streaks.
 */
data class AttendanceStreak(
    val currentStreak: Int,
    val longestStreak: Int,
    val streakStartDate: Instant? = null,
    val streakType: StreakType
)

/**
 * Type of streak being tracked.
 */
enum class StreakType {
    DAILY,
    WEEKLY;

    companion object {
        fun fromString(value: String): StreakType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: DAILY
    }
}

// ─── Hall Pass ───────────────────────────────────────────────────

/**
 * A hall pass request and its lifecycle.
 */
data class HallPass(
    val id: String,
    val studentId: String,
    val studentName: String,
    val teacherId: String,
    val destination: DestinationType,
    val reason: String,
    val requestedAt: Instant,
    val approvedAt: Instant? = null,
    val expiresAt: Instant? = null,
    val status: HallPassStatus
) {
    /**
     * Whether the hall pass is currently active and not expired.
     */
    val isActive: Boolean
        get() = status == HallPassStatus.ACTIVE &&
                (expiresAt == null || Instant.now().isBefore(expiresAt))

    /**
     * Remaining time in seconds, or null if not active.
     */
    val remainingSeconds: Long?
        get() = if (isActive && expiresAt != null) {
            val remaining = expiresAt.epochSecond - Instant.now().epochSecond
            if (remaining > 0) remaining else 0
        } else null
}

/**
 * Where the student is heading with their hall pass.
 */
enum class DestinationType {
    RESTROOM,
    NURSE,
    OFFICE,
    LIBRARY,
    OTHER;

    companion object {
        fun fromString(value: String): DestinationType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

/**
 * Hall pass lifecycle status.
 */
enum class HallPassStatus {
    REQUESTED,
    APPROVED,
    ACTIVE,
    EXPIRED,
    DENIED;

    companion object {
        fun fromString(value: String): HallPassStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: REQUESTED
    }
}

// ─── Interventions ───────────────────────────────────────────────

/**
 * An intervention triggered when a student's attendance drops below threshold.
 */
data class AttendanceIntervention(
    val id: String,
    val studentId: String,
    val studentName: String,
    val type: InterventionType,
    val threshold: Float,
    val currentRate: Float,
    val status: InterventionStatus,
    val createdAt: Instant,
    val resolvedAt: Instant? = null
) {
    /**
     * How far below threshold the student is (as percentage points).
     */
    val deficit: Float get() = (threshold - currentRate).coerceAtLeast(0f)
}

/**
 * Type of intervention action to take.
 */
enum class InterventionType {
    PARENT_ALERT,
    COUNSELOR_REFERRAL,
    ADMIN_REVIEW;

    companion object {
        fun fromString(value: String): InterventionType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PARENT_ALERT
    }
}

/**
 * Current status of the intervention.
 */
enum class InterventionStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED;

    companion object {
        fun fromString(value: String): InterventionStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}

// ─── Attendance Methods ──────────────────────────────────────────

/**
 * Represents an available attendance capture method and its configuration.
 */
data class AttendanceMethod(
    val type: MethodType,
    val enabled: Boolean,
    val isDefault: Boolean
)

/**
 * Available methods for capturing attendance.
 */
enum class MethodType {
    MANUAL,
    GEOFENCE,
    NFC,
    BLUETOOTH,
    BARCODE,
    QR;

    companion object {
        fun fromString(value: String): MethodType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: MANUAL
    }
}

// ─── Enhanced Analytics ──────────────────────────────────────────

/**
 * Advanced attendance analytics with heatmap, patterns, and correlations.
 */
data class AdvancedAttendanceAnalytics(
    val heatmapData: Map<String, Float>,
    val dayOfWeekPattern: Map<String, Float>,
    val subjectCorrelation: Map<String, Float>
)
