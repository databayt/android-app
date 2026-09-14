package org.hogwarts.android.feature.attendance.data.remote

import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceRecordsResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceSummaryDto
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceTotalsDto
import org.hogwarts.android.feature.attendance.data.remote.dto.DashboardTodayDto
import org.hogwarts.android.feature.attendance.data.remote.dto.GuardianChildrenResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncRequest
import org.hogwarts.android.feature.attendance.data.remote.dto.OfflineSyncResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.ProfileDto
import org.hogwarts.android.feature.attendance.data.remote.dto.SectionAttendanceResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherClassesResponse
import org.hogwarts.android.feature.attendance.data.remote.dto.TeacherScheduleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The hogwarts mobile routes behind the attendance landing. The tenant is the
 * JWT's school; non-2xx answers throw `HttpException`, no answer throws
 * `IOException`.
 */
interface AttendanceApi {

    @GET("api/mobile/teacher/classes")
    suspend fun teacherClasses(): TeacherClassesResponse

    /** [day] follows JS `getDay()`: 0 = Sunday. */
    @GET("api/mobile/teacher/schedule")
    suspend fun teacherSchedule(@Query("day") day: Int): TeacherScheduleResponse

    @GET("api/mobile/dashboard")
    suspend fun dashboard(): DashboardTodayDto

    /** The section's roster with each student's status on [date] (yyyy-MM-dd). */
    @GET("api/mobile/attendance/class/{sectionId}")
    suspend fun sectionAttendance(
        @Path("sectionId") sectionId: String,
        @Query("date") date: String,
    ): SectionAttendanceResponse

    @POST("api/mobile/offline/sync")
    suspend fun offlineSync(@Body body: OfflineSyncRequest): OfflineSyncResponse

    @GET("api/mobile/attendance/analytics")
    suspend fun totals(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): AttendanceTotalsDto

    @GET("api/mobile/profile")
    suspend fun profile(): ProfileDto

    @GET("api/mobile/attendance/summary/{studentId}")
    suspend fun summary(@Path("studentId") studentId: String): AttendanceSummaryDto

    @GET("api/mobile/attendance/student/{studentId}")
    suspend fun studentRecords(
        @Path("studentId") studentId: String,
        @Query("per_page") perPage: Int,
    ): AttendanceRecordsResponse

    @GET("api/mobile/guardian/children")
    suspend fun guardianChildren(): GuardianChildrenResponse

    @GET("api/mobile/guardian/children/{childId}/attendance")
    suspend fun childRecords(
        @Path("childId") childId: String,
        @Query("per_page") perPage: Int,
    ): AttendanceRecordsResponse
}
