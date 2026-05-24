package org.hogwarts.android.feature.guardian.data.remote

import org.hogwarts.android.feature.guardian.data.remote.dto.ChildAttendanceDto
import org.hogwarts.android.feature.guardian.data.remote.dto.ChildDto
import org.hogwarts.android.feature.guardian.data.remote.dto.ChildFeeDto
import org.hogwarts.android.feature.guardian.data.remote.dto.ChildGradeDto
import org.hogwarts.android.feature.guardian.data.remote.dto.ChildTeacherDto
import org.hogwarts.android.feature.guardian.data.remote.dto.ChildTimetableDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for guardian endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface GuardianApi {

    @GET("api/mobile/guardian/children")
    suspend fun getChildren(): List<ChildDto>

    @GET("api/mobile/guardian/children/{childId}")
    suspend fun getChild(
        @Path("childId") childId: String
    ): ChildDto

    @GET("api/mobile/guardian/children/{childId}/attendance")
    suspend fun getChildAttendance(
        @Path("childId") childId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): List<ChildAttendanceDto>

    @GET("api/mobile/guardian/children/{childId}/grades")
    suspend fun getChildGrades(
        @Path("childId") childId: String,
        @Query("termId") termId: String? = null
    ): List<ChildGradeDto>

    @GET("api/mobile/guardian/children/{childId}/fees")
    suspend fun getChildFees(
        @Path("childId") childId: String,
        @Query("status") status: String? = null
    ): List<ChildFeeDto>

    @GET("api/mobile/guardian/children/{childId}/timetable")
    suspend fun getChildTimetable(
        @Path("childId") childId: String
    ): List<ChildTimetableDto>

    @GET("api/mobile/guardian/children/{childId}/teachers")
    suspend fun getChildTeachers(
        @Path("childId") childId: String
    ): List<ChildTeacherDto>
}
