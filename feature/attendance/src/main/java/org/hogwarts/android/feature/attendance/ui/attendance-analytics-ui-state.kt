package org.hogwarts.android.feature.attendance.ui

import org.hogwarts.android.feature.attendance.domain.model.AdvancedAttendanceAnalytics

/**
 * UI state for the Advanced Attendance Analytics screen.
 */
data class AttendanceAnalyticsUiState(
    val isLoading: Boolean = true,
    val analytics: AdvancedAttendanceAnalytics? = null,
    val error: String? = null,
    val selectedTab: AnalyticsTab = AnalyticsTab.HEATMAP
)

enum class AnalyticsTab {
    HEATMAP,
    DAY_PATTERN,
    SUBJECT_CORRELATION
}
