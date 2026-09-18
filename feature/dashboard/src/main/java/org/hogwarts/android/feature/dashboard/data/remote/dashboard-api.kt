package org.hogwarts.android.feature.dashboard.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

/** `GET /api/mobile/dashboard` — role-aware home data (hogwarts `api/mobile/dashboard/route.ts`). */
interface DashboardApi {
    @GET("api/mobile/dashboard")
    suspend fun getDashboard(): Response<DashboardDto>
}

@Serializable
data class DashboardDto(
    @SerialName("user_name") val userName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String = "",
    @SerialName("school_name") val schoolName: String = "",
    @SerialName("unread_notifications") val unreadNotifications: Int = 0,
    @SerialName("unread_messages") val unreadMessages: Int = 0,
    @SerialName("announcements_count") val announcementsCount: Int = 0,
    /** School events dated today — the calendar card's bottom line. */
    @SerialName("events_today") val eventsToday: Int? = null,
    val school: SchoolDto? = null,
    @SerialName("next_actions") val nextActions: List<NextActionDto> = emptyList(),
    @SerialName("quick_actions") val quickActions: List<QuickActionDto> = emptyList(),
    @SerialName("today_timetable") val todayTimetable: TodayTimetableDto? = null,
    // The route still sends the flat per-role counters (total_students,
    // collected_today, …). Nothing decodes them any more: the web dashboard
    // renders no stat panel under the phone sections, so the panel that read
    // them was deleted rather than kept alive on numbers the reader never sees.
)

@Serializable
data class SchoolDto(
    val id: String,
    val name: String = "",
    @SerialName("name_en") val nameEn: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    /** null = every module enabled. */
    @SerialName("enabled_modules") val enabledModules: List<String>? = null,
)

@Serializable
data class NextActionDto(val kind: String, val mark: String = "", val href: String = "")

@Serializable
data class QuickActionDto(
    val key: String = "",
    val label: String = "",
    val description: String? = null,
    val href: String = "",
    val icon: String? = null,
)

@Serializable
data class TodayTimetableDto(
    @SerialName("day_of_week") val dayOfWeek: Int = 0,
    val date: String? = null,
    /**
     * Whether the resolved day IS today. The route falls forward over a
     * weekend or a closure the way `today-timetable.tsx` does, so the day it
     * returns is not always today and the heading has to say which
     * ("Today's classes" vs "Next classes"). Null while the field is still
     * rolling out — [isTodayOr] then falls back to the weekday itself.
     */
    @SerialName("is_today") val isToday: Boolean? = null,
    val closure: ClosureDto? = null,
    val periods: List<PeriodDto> = emptyList(),
) {
    /** [isToday] when the server states it, else whether the day IS [weekday]. */
    fun isTodayOr(weekday: Int): Boolean = isToday ?: (dayOfWeek == weekday)
}

@Serializable
data class ClosureDto(val title: String? = null, val type: String? = null)

@Serializable
data class PeriodDto(
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
)
