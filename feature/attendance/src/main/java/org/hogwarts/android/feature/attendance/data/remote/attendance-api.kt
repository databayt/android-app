package org.hogwarts.android.feature.attendance.data.remote

import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceListResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceRecordDto
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceSummaryDto
import org.hogwarts.android.feature.attendance.data.remote.dto.BulkAttendanceDto
import org.hogwarts.android.feature.attendance.data.remote.dto.MarkAttendanceDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for attendance endpoints.
 *
 * Integrates with Hogwarts backend.
 * X-School-Id header is added automatically by TenantInterceptor.
 */
interface AttendanceApi {

    /**
     * Get attendance records for a student.
     */
    @GET("api/mobile/attendance/student/{studentId}")
    suspend fun getStudentAttendance(
        @Path("studentId") studentId: String,
        @Query("from") startDate: String? = null,
        @Query("to") endDate: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<AttendanceListResponse>

    /**
     * Get attendance for a class on a specific date.
     */
    @GET("api/mobile/attendance/class/{classId}")
    suspend fun getClassAttendance(
        @Path("classId") classId: String,
        @Query("date") date: String
    ): Response<AttendanceListResponse>

    /**
     * Get attendance summary/statistics for a student.
     */
    @GET("api/mobile/attendance/summary/{studentId}")
    suspend fun getAttendanceSummary(
        @Path("studentId") studentId: String
    ): Response<AttendanceSummaryDto>

    /**
     * Mark attendance for a single student.
     */
    @POST("api/mobile/attendance/mark")
    suspend fun markAttendance(
        @Body request: MarkAttendanceDto
    ): Response<AttendanceRecordDto>

    /**
     * Mark attendance for an entire class (bulk).
     */
    @POST("api/mobile/attendance/bulk")
    suspend fun markBulkAttendance(
        @Body request: BulkAttendanceDto
    ): Response<AttendanceListResponse>
}
