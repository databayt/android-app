package org.hogwarts.android.feature.attendance.ui

import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import org.hogwarts.android.feature.attendance.domain.model.AttendanceSummary

/**
 * UI state for the Attendance screen.
 */
data class AttendanceUiState(
    val isLoading: Boolean = true,
    val records: List<AttendanceRecord> = emptyList(),
    val summary: AttendanceSummary? = null,
    val error: String? = null,
    val isTeacher: Boolean = false,
    val selectedFilter: AttendanceFilter = AttendanceFilter.ALL
)

enum class AttendanceFilter {
    ALL, PRESENT, ABSENT, LATE, EXCUSED
}
