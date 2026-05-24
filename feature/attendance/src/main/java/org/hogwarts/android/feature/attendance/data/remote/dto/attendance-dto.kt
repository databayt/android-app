package org.hogwarts.android.feature.attendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for attendance API responses.
 */
@Serializable
data class AttendanceRecordDto(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("class_id") val classId: String,
    @SerialName("class_name") val className: String,
    val date: String,
    val status: String,
    @SerialName("check_in_time") val checkInTime: String? = null,
    val note: String? = null,
    @SerialName("marked_by") val markedBy: String? = null
)

@Serializable
data class AttendanceListResponse(
    val data: List<AttendanceRecordDto>,
    val total: Int? = null
)

@Serializable
data class AttendanceSummaryDto(
    @SerialName("total_days") val totalDays: Int,
    @SerialName("present_days") val presentDays: Int,
    @SerialName("absent_days") val absentDays: Int,
    @SerialName("late_days") val lateDays: Int,
    @SerialName("excused_days") val excusedDays: Int
)

@Serializable
data class MarkAttendanceDto(
    @SerialName("student_id") val studentId: String,
    @SerialName("class_id") val classId: String,
    val date: String,
    val status: String,
    val note: String? = null
)

@Serializable
data class BulkAttendanceDto(
    @SerialName("class_id") val classId: String,
    val date: String,
    val records: List<BulkAttendanceRecordDto>
)

@Serializable
data class BulkAttendanceRecordDto(
    @SerialName("student_id") val studentId: String,
    val status: String,
    val note: String? = null
)
