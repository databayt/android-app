package org.hogwarts.android.feature.teacher.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeacherClassDto(
    val id: String,
    val name: String,
    val grade: String,
    val section: String = "",
    val subjectId: String,
    val subjectName: String,
    val studentCount: Int = 0,
    val nextSessionTime: String? = null,
    val nextSessionDay: String? = null
)

@Serializable
data class ClassStudentDto(
    val id: String,
    val givenName: String,
    val familyName: String,
    val studentNumber: String? = null,
    val avatarUrl: String? = null,
    val attendanceRate: Float = 0f,
    val latestGrade: String? = null,
    val status: String = "Active"
)

@Serializable
data class ScheduleSlotDto(
    val id: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val className: String,
    val subjectName: String,
    val roomNumber: String? = null,
    val classId: String
)

@Serializable
data class AssessmentDto(
    val id: String,
    val name: String,
    val maxMarks: Float,
    val date: String? = null
)

@Serializable
data class BatchAttendanceRequestDto(
    val date: String,
    val period: String? = null,
    val records: List<AttendanceRecordDto>
)

@Serializable
data class AttendanceRecordDto(
    val studentId: String,
    val status: String,
    val note: String = ""
)

@Serializable
data class BatchGradeRequestDto(
    val assessmentId: String,
    val grades: List<GradeRecordDto>
)

@Serializable
data class GradeRecordDto(
    val studentId: String,
    val marks: Float,
    val note: String = ""
)
