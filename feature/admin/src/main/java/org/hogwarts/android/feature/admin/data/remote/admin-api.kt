package org.hogwarts.android.feature.admin.data.remote

import org.hogwarts.android.feature.admin.data.remote.dto.AnalysisResponseDto
import org.hogwarts.android.feature.admin.data.remote.dto.ClassRosterDto
import org.hogwarts.android.feature.admin.data.remote.dto.ReadinessResponseDto
import org.hogwarts.android.feature.admin.data.remote.dto.SchoolInfoDto
import org.hogwarts.android.feature.admin.data.remote.dto.SchoolStatsDto
import org.hogwarts.android.feature.admin.data.remote.dto.StaffMemberDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for school administration endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface AdminApi {

    @GET("api/mobile/admin/school")
    suspend fun getSchoolInfo(): SchoolInfoDto

    @PUT("api/mobile/admin/school")
    suspend fun updateSchoolInfo(
        @Body body: SchoolInfoDto
    ): SchoolInfoDto

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/admin/staff")
    suspend fun getStaff(
        @Query("role") role: String? = null,
        @Query("search") search: String? = null
    ): List<StaffMemberDto>

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/admin/staff/{id}")
    suspend fun getStaffMember(
        @Path("id") staffId: String
    ): StaffMemberDto

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/admin/classes")
    suspend fun getClassRosters(): List<ClassRosterDto>

    // NOTE: Web endpoint being added — will 404 until available
    @GET("api/mobile/admin/classes/{classId}")
    suspend fun getClassRoster(
        @Path("classId") classId: String
    ): ClassRosterDto

    // NOTE: Web endpoint being added — will 404 until available
    @POST("api/mobile/admin/classes/{classId}/students/{studentId}")
    suspend fun addStudentToClass(
        @Path("classId") classId: String,
        @Path("studentId") studentId: String
    )

    // NOTE: Web endpoint being added — will 404 until available
    @DELETE("api/mobile/admin/classes/{classId}/students/{studentId}")
    suspend fun removeStudentFromClass(
        @Path("classId") classId: String,
        @Path("studentId") studentId: String
    )

    /** Class readiness for a school day (`YYYY-MM-DD`); null = the school's today. 403 = not a class supervisor. */
    @GET("api/mobile/admin/readiness")
    suspend fun getReadiness(@Query("date") date: String? = null): ReadinessResponseDto

    /** The school's analysis report. 403 = not an admin. */
    @GET("api/mobile/admin/analysis")
    suspend fun getAnalysis(): AnalysisResponseDto

    @GET("api/mobile/admin/stats")
    suspend fun getSchoolStats(): SchoolStatsDto
}
