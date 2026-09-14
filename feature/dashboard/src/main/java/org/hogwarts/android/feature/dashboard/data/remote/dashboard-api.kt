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

    // Role stats (flat, role-dependent)
    @SerialName("attendance_percentage") val attendancePercentage: Double? = null,
    @SerialName("upcoming_exams") val upcomingExams: Int? = null,
    @SerialName("today_classes") val todayClasses: Int? = null,
    @SerialName("total_classes") val totalClasses: Int? = null,
    @SerialName("children_count") val childrenCount: Int? = null,
    @SerialName("total_students") val totalStudents: Int? = null,
    @SerialName("total_teachers") val totalTeachers: Int? = null,
    @SerialName("pending_invoices") val pendingInvoices: Int? = null,
    @SerialName("pending_amount") val pendingAmount: Double? = null,
    @SerialName("overdue_invoices") val overdueInvoices: Int? = null,
    @SerialName("overdue_amount") val overdueAmount: Double? = null,
    @SerialName("collected_today") val collectedToday: Double? = null,
    @SerialName("present_today") val presentToday: Int? = null,
    @SerialName("upcoming_events") val upcomingEvents: Int? = null,
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
    val closure: ClosureDto? = null,
    val periods: List<PeriodDto> = emptyList(),
)

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
