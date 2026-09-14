package org.hogwarts.android.feature.attendance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/*
 * Wire shapes of the hogwarts mobile routes the attendance landing reads.
 * Each block names its route; field names are the server's snake_case.
 */

// GET /api/mobile/teacher/classes
@Serializable
data class TeacherClassesResponse(val data: List<TeacherClassDto> = emptyList())

@Serializable
data class TeacherClassDto(
    @SerialName("section_id") val sectionId: String,
    @SerialName("section_name") val sectionName: String,
    @SerialName("grade_name") val gradeName: String? = null,
    @SerialName("subject_name") val subjectName: String? = null,
    @SerialName("student_count") val studentCount: Int = 0,
)

// GET /api/mobile/teacher/schedule?day=
@Serializable
data class TeacherScheduleResponse(val data: List<TeacherSlotDto> = emptyList())

@Serializable
data class TeacherSlotDto(
    val id: String,
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("section_name") val sectionName: String? = null,
    @SerialName("grade_name") val gradeName: String? = null,
    @SerialName("period_name") val periodName: String? = null,
    /** Prisma `@db.Time` serialised on the epoch date: "1970-01-01T08:00:00.000Z". */
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
)

// GET /api/mobile/dashboard — only today's timetable is read here.
@Serializable
data class DashboardTodayDto(
    @SerialName("today_timetable") val todayTimetable: TodayTimetableDto? = null,
)

@Serializable
data class TodayTimetableDto(
    @SerialName("day_of_week") val dayOfWeek: Int,
    val closure: ClosureDto? = null,
)

@Serializable
data class ClosureDto(val title: String? = null, val type: String? = null)

// GET /api/mobile/attendance/class/:sectionId?date=
@Serializable
data class SectionAttendanceResponse(
    val date: String,
    @SerialName("section_id") val sectionId: String,
    @SerialName("total_students") val totalStudents: Int = 0,
    val marked: Int = 0,
    val data: List<SectionStudentDto> = emptyList(),
)

@Serializable
data class SectionStudentDto(
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    val status: String? = null,
)

// POST /api/mobile/offline/sync
@Serializable
data class OfflineSyncRequest(val items: List<OfflineSyncItemDto>)

@Serializable
data class OfflineSyncItemDto(
    /** `^[A-Za-z0-9_-]{8,64}$` — a UUID minted when the teacher pressed save. */
    @SerialName("idempotency_key") val idempotencyKey: String,
    val kind: String,
    val payload: QuickAttendancePayloadDto,
    /** ISO instant with offset, e.g. `Instant.toString()`. */
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class QuickAttendancePayloadDto(
    @SerialName("section_id") val sectionId: String,
    val date: String,
    @SerialName("absent_student_ids") val absentStudentIds: List<String>,
    @SerialName("late_student_ids") val lateStudentIds: List<String>,
)

@Serializable
data class OfflineSyncResponse(
    val results: List<OfflineSyncResultDto> = emptyList(),
    @SerialName("server_time") val serverTime: String? = null,
)

@Serializable
data class OfflineSyncResultDto(
    @SerialName("idempotency_key") val idempotencyKey: String,
    /** applied | duplicate | rejected */
    val result: String,
    val code: String? = null,
    val data: JsonObject? = null,
)

/** `data` of an applied attendance item. */
@Serializable
data class QuickSummaryDto(
    val total: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0,
    @SerialName("guardians_notified") val guardiansNotified: Int = 0,
)

// GET /api/mobile/attendance/analytics?start_date=&end_date=
@Serializable
data class AttendanceTotalsDto(
    @SerialName("total_days") val totalDays: Int = 0,
    @SerialName("present_count") val presentCount: Int = 0,
    @SerialName("absent_count") val absentCount: Int = 0,
    @SerialName("late_count") val lateCount: Int = 0,
    @SerialName("excused_count") val excusedCount: Int = 0,
)

// GET /api/mobile/profile — only the student link is read here.
@Serializable
data class ProfileDto(val student: ProfileStudentDto? = null)

@Serializable
data class ProfileStudentDto(val id: String)

// GET /api/mobile/attendance/summary/:studentId
@Serializable
data class AttendanceSummaryDto(
    val total: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0,
    val excused: Int = 0,
)

// GET /api/mobile/attendance/student/:id and /guardian/children/:id/attendance
@Serializable
data class AttendanceRecordsResponse(
    val data: List<AttendanceRecordDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
data class AttendanceRecordDto(
    val id: String,
    val date: String,
    val status: String,
    @SerialName("period_name") val periodName: String? = null,
    val section: String? = null,
    val grade: String? = null,
)

// GET /api/mobile/guardian/children
@Serializable
data class GuardianChildrenResponse(val data: List<GuardianChildDto> = emptyList())

@Serializable
data class GuardianChildDto(
    val id: String,
    @SerialName("given_name") val givenName: String? = null,
    @SerialName("family_name") val familyName: String? = null,
    val section: String? = null,
    val grade: String? = null,
)
