package org.hogwarts.android.feature.attendance.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Domain model for attendance records.
 */
data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val classId: String,
    val className: String,
    val date: LocalDate,
    val status: AttendanceStatus,
    val checkInTime: LocalTime? = null,
    val note: String? = null,
    val markedBy: String? = null
)

/**
 * Attendance status types matching Hogwarts backend.
 */
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED;

    companion object {
        fun fromString(value: String): AttendanceStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ABSENT
    }
}

/**
 * Summary statistics for attendance.
 */
data class AttendanceSummary(
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,
    val excusedDays: Int
) {
    val attendancePercentage: Float
        get() = if (totalDays > 0) (presentDays + lateDays).toFloat() / totalDays * 100 else 0f
}

/**
 * Request to mark attendance for a student.
 */
data class MarkAttendanceRequest(
    val studentId: String,
    val classId: String,
    val date: LocalDate,
    val status: AttendanceStatus,
    val note: String? = null
)

/**
 * Bulk attendance marking for an entire class.
 */
data class BulkAttendanceRequest(
    val classId: String,
    val date: LocalDate,
    val records: List<StudentAttendanceMark>
)

data class StudentAttendanceMark(
    val studentId: String,
    val status: AttendanceStatus,
    val note: String? = null
)
