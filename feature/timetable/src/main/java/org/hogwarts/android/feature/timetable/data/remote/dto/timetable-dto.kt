package org.hogwarts.android.feature.timetable.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One weekly slot from `GET /api/mobile/timetable/:userId` and
 * `GET /api/mobile/guardian/children/:childId/timetable`. Every field but the
 * id may be null on the wire; `live_class` is only sent by the first route and
 * only for today's slots. Times are wall-clock `1970-01-01THH:mm:00.000Z`.
 */
@Serializable
data class SlotDto(
    val id: String,
    @SerialName("day_of_week") val dayOfWeek: Int = 0,
    @SerialName("subject_name") val subjectName: String? = null,
    @SerialName("teacher_name") val teacherName: String? = null,
    @SerialName("section_name") val sectionName: String? = null,
    @SerialName("grade_name") val gradeName: String? = null,
    val classroom: String? = null,
    @SerialName("period_name") val periodName: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("live_class") val liveClass: LiveClassDto? = null,
)

@Serializable
data class LiveClassDto(
    @SerialName("session_id") val sessionId: String? = null,
    val provider: String? = null,
    @SerialName("meeting_url") val meetingUrl: String? = null,
    val status: String? = null,
)

@Serializable
data class SlotListResponse(val data: List<SlotDto> = emptyList())

/** Only the part of `GET /api/mobile/dashboard` this screen reads: the school's day. */
@Serializable
data class DashboardDayResponse(
    @SerialName("today_timetable") val todayTimetable: TodayTimetableDto? = null,
)

/** `getTodaySchedule` for STUDENT / TEACHER: every period of the day, breaks and free periods included. */
@Serializable
data class TodayTimetableDto(
    @SerialName("day_of_week") val dayOfWeek: Int = 0,
    val date: String? = null,
    val closure: ClosureDto? = null,
    val periods: List<TodayPeriodDto> = emptyList(),
)

@Serializable
data class ClosureDto(val title: String? = null, val type: String? = null)

@Serializable
data class TodayPeriodDto(
    @SerialName("period_id") val periodId: String = "",
    @SerialName("period_name") val periodName: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val subject: String? = null,
    @SerialName("class_name") val className: String? = null,
    val teacher: String? = null,
    val room: String? = null,
    @SerialName("is_break") val isBreak: Boolean = false,
    @SerialName("timetable_id") val timetableId: String? = null,
    @SerialName("live_class") val liveClass: LiveClassDto? = null,
)

/** `GET /api/mobile/guardian/children`. */
@Serializable
data class ChildDto(
    val id: String,
    @SerialName("given_name") val givenName: String? = null,
    @SerialName("family_name") val familyName: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val section: String? = null,
    val grade: String? = null,
)

@Serializable
data class ChildListResponse(val data: List<ChildDto> = emptyList())

/** What one user's timetable screen was built from — the offline cache stores it whole. */
@Serializable
data class TimetableBundle(
    val slots: List<SlotDto> = emptyList(),
    val today: TodayTimetableDto? = null,
    val children: List<ChildDto> = emptyList(),
    val childId: String? = null,
)
