package org.hogwarts.android.feature.grades.data.remote

import org.hogwarts.android.feature.grades.data.remote.dto.GpaSummaryDto
import org.hogwarts.android.feature.grades.data.remote.dto.GradeListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for grades endpoints.
 *
 * Integrates with Hogwarts backend.
 * X-School-Id header is added automatically by TenantInterceptor.
 */
interface GradesApi {

    /**
     * Get grade records for a student.
     */
    @GET("api/mobile/grades/student/{studentId}")
    suspend fun getStudentGrades(
        @Path("studentId") studentId: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<GradeListResponse>

    /**
     * Get GPA summary for a student.
     */
    @GET("api/mobile/grades/summary/{studentId}")
    suspend fun getGpaSummary(
        @Path("studentId") studentId: String
    ): Response<GpaSummaryDto>
}
