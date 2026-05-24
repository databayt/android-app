package org.hogwarts.android.feature.dashboard.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

/**
 * Retrofit API interface for the dashboard endpoint.
 */
interface DashboardApi {

    @GET("api/mobile/dashboard")
    suspend fun getDashboard(): Response<DashboardDto>
}

/**
 * Dashboard response DTO — role-adaptive fields from the backend.
 */
@Serializable
data class DashboardDto(
    @SerialName("user_name") val userName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String = "",
    @SerialName("school_name") val schoolName: String = "",
    @SerialName("unread_notifications") val unreadNotifications: Int = 0,
    @SerialName("announcements_count") val announcementsCount: Int = 0,

    // Student-specific
    @SerialName("attendance_percentage") val attendancePercentage: Float? = null,
    @SerialName("upcoming_exams") val upcomingExams: Int? = null,
    @SerialName("today_classes") val todayClasses: Int? = null,

    // Teacher-specific
    @SerialName("total_classes") val totalClasses: Int? = null,

    // Guardian-specific
    @SerialName("children_count") val childrenCount: Int? = null,

    // Admin-specific
    @SerialName("total_students") val totalStudents: Int? = null,
    @SerialName("total_teachers") val totalTeachers: Int? = null,
)
