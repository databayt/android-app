package org.hogwarts.android.feature.reportcards.data.remote

import okhttp3.ResponseBody
import org.hogwarts.android.feature.reportcards.data.remote.dto.ReportCardDetailDto
import org.hogwarts.android.feature.reportcards.data.remote.dto.ReportCardListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Retrofit API interface for report cards endpoints.
 *
 * Integrates with Hogwarts backend.
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface ReportCardsApi {

    /**
     * Get report cards list for the authenticated user / a specific student.
     *
     * @param termId Optional filter by term
     * @param studentId Optional filter by student (for guardians/admins)
     */
    @GET("api/mobile/report-cards")
    suspend fun getReportCards(
        @Query("schoolId") schoolId: String,
        @Query("term_id") termId: String? = null,
        @Query("student_id") studentId: String? = null
    ): Response<ReportCardListResponse>

    /**
     * Get a single report card with full subject details.
     *
     * @param id Report card ID
     */
    @GET("api/mobile/report-cards/{id}")
    suspend fun getReportCardDetail(
        @Path("id") id: String,
        @Query("schoolId") schoolId: String
    ): Response<ReportCardDetailDto>

    /**
     * Download report card as PDF.
     *
     * @param id Report card ID
     * @return Streaming ResponseBody for PDF content
     */
    @Streaming
    @GET("api/mobile/report-cards/{id}/pdf")
    suspend fun downloadReportCardPdf(
        @Path("id") id: String,
        @Query("schoolId") schoolId: String
    ): Response<ResponseBody>
}
