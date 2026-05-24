package org.hogwarts.android.feature.attendance.data.remote

import org.hogwarts.android.feature.attendance.data.remote.dto.AdvancedAnalyticsResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceBadgeListResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceMethodListResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceStreakDto
import org.hogwarts.android.feature.attendance.data.remote.dto.HallPassDto
import org.hogwarts.android.feature.attendance.data.remote.dto.HallPassListResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.HallPassRequestDto
import org.hogwarts.android.feature.attendance.data.remote.dto.InterventionListResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.UpdateMethodsRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for EPIC-27: Advanced Attendance endpoints.
 *
 * X-School-Id header is added automatically by TenantInterceptor.
 */
interface AdvancedAttendanceApi {

    // ─── Gamification ────────────────────────────────────────────

    /**
     * Get all available badges and which ones the student has earned.
     */
    @GET("api/mobile/attendance/badges")
    suspend fun getBadges(
        @Query("student_id") studentId: String? = null
    ): Response<AttendanceBadgeListResponse>

    /**
     * Get current streak information for a student.
     */
    @GET("api/mobile/attendance/streaks")
    suspend fun getStreaks(
        @Query("student_id") studentId: String? = null
    ): Response<AttendanceStreakDto>

    // ─── Hall Pass ───────────────────────────────────────────────

    /**
     * Request a new hall pass.
     */
    @POST("api/mobile/attendance/hall-pass")
    suspend fun requestHallPass(
        @Body request: HallPassRequestDto
    ): Response<HallPassDto>

    /**
     * Approve or deny a hall pass (teacher/admin action).
     */
    @PUT("api/mobile/attendance/hall-pass/{id}/approve")
    suspend fun approveHallPass(
        @Path("id") hallPassId: String,
        @Query("approved") approved: Boolean
    ): Response<HallPassDto>

    /**
     * Get hall passes with optional status filter.
     */
    @GET("api/mobile/attendance/hall-passes")
    suspend fun getHallPasses(
        @Query("status") status: String? = null,
        @Query("student_id") studentId: String? = null
    ): Response<HallPassListResponse>

    // ─── Interventions ───────────────────────────────────────────

    /**
     * Get attendance interventions for students below threshold.
     */
    @GET("api/mobile/attendance/interventions")
    suspend fun getInterventions(
        @Query("status") status: String? = null
    ): Response<InterventionListResponse>

    // ─── Analytics ───────────────────────────────────────────────

    /**
     * Get advanced attendance analytics (heatmap, patterns, correlations).
     */
    @GET("api/mobile/attendance/analytics")
    suspend fun getAnalytics(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("student_id") studentId: String? = null
    ): Response<AdvancedAnalyticsResponse>

    // ─── Methods ─────────────────────────────────────────────────

    /**
     * Get available attendance methods and their configuration.
     */
    @GET("api/mobile/attendance/methods")
    suspend fun getMethods(): Response<AttendanceMethodListResponse>

    /**
     * Update attendance method configuration.
     */
    @PUT("api/mobile/attendance/methods")
    suspend fun updateMethods(
        @Body request: UpdateMethodsRequestDto
    ): Response<AttendanceMethodListResponse>
}
