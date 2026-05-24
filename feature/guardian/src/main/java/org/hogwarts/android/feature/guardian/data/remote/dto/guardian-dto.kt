package org.hogwarts.android.feature.guardian.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChildDto(
    val id: String,
    val studentId: String,
    val givenName: String,
    val familyName: String,
    val grade: String,
    val section: String = "",
    val avatarUrl: String? = null,
    val attendanceRate: Float = 0f,
    val latestGrade: String? = null,
    val feeBalance: Double = 0.0,
    val className: String = ""
)

@Serializable
data class ChildAttendanceDto(
    val id: String,
    val date: String,
    val status: String,
    val period: String? = null,
    val subject: String? = null,
    val note: String? = null,
    val markedBy: String? = null
)

@Serializable
data class ChildGradeDto(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val assessmentName: String,
    val score: Float,
    val maxScore: Float,
    val percentage: Float,
    val gradeLetter: String? = null,
    val term: String? = null,
    val date: String? = null
)

@Serializable
data class ChildFeeDto(
    val id: String,
    val name: String,
    val amount: Double,
    val dueDate: String,
    val status: String,
    val paidAmount: Double = 0.0,
    val paidDate: String? = null
)

@Serializable
data class ChildTimetableDto(
    val id: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subjectName: String,
    val teacherName: String,
    val roomNumber: String? = null
)

@Serializable
data class ChildTeacherDto(
    val id: String,
    val givenName: String,
    val familyName: String,
    val subject: String,
    val avatarUrl: String? = null,
    val unreadCount: Int = 0
)
