package org.hogwarts.android.feature.attendance.ui

import org.hogwarts.android.feature.attendance.domain.model.AttendanceBadge
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStreak
import org.hogwarts.android.feature.attendance.domain.model.StreakType

/**
 * UI state for the Gamification screen.
 */
data class GamificationUiState(
    val isLoading: Boolean = true,
    val badges: List<AttendanceBadge> = emptyList(),
    val streak: AttendanceStreak = AttendanceStreak(
        currentStreak = 0,
        longestStreak = 0,
        streakStartDate = null,
        streakType = StreakType.DAILY
    ),
    val error: String? = null
) {
    val earnedBadges: List<AttendanceBadge> get() = badges.filter { it.isEarned }
    val unearnedBadges: List<AttendanceBadge> get() = badges.filter { !it.isEarned }
    val earnedCount: Int get() = earnedBadges.size
    val totalBadges: Int get() = badges.size
}
