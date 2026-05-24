package org.hogwarts.android.feature.exams.data.remote

import org.hogwarts.android.feature.exams.data.remote.dto.BookmarkResponse
import org.hogwarts.android.feature.exams.data.remote.dto.ExamCertificateDto
import org.hogwarts.android.feature.exams.data.remote.dto.DetailedExamResultDto
import org.hogwarts.android.feature.exams.data.remote.dto.GenerateQuestionsRequest
import org.hogwarts.android.feature.exams.data.remote.dto.GenerateQuestionsResponse
import org.hogwarts.android.feature.exams.data.remote.dto.OnlineExamDto
import org.hogwarts.android.feature.exams.data.remote.dto.QuestionBankListResponse
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswersRequest
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswersResponse
import org.hogwarts.android.feature.exams.data.remote.dto.ViolationReportRequest
import org.hogwarts.android.feature.exams.data.remote.dto.ViolationReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface AdvancedExamsApi {

    @GET("api/mobile/exams/{examId}/online")
    suspend fun getOnlineExamSession(
        @Path("examId") examId: String,
        @Query("schoolId") schoolId: String
    ): Response<OnlineExamDto>

    @POST("api/mobile/exams/{examId}/answers")
    suspend fun submitAnswers(
        @Path("examId") examId: String,
        @Query("schoolId") schoolId: String,
        @Body request: SubmitAnswersRequest
    ): Response<SubmitAnswersResponse>

    @POST("api/mobile/exams/{examId}/violations")
    suspend fun reportViolation(
        @Path("examId") examId: String,
        @Query("schoolId") schoolId: String,
        @Body request: ViolationReportRequest
    ): Response<ViolationReportResponse>

    @GET("api/mobile/exams/question-bank")
    suspend fun getQuestionBank(
        @Query("schoolId") schoolId: String,
        @Query("subject") subject: String? = null,
        @Query("topic") topic: String? = null,
        @Query("difficulty") difficulty: String? = null
    ): Response<QuestionBankListResponse>

    @POST("api/mobile/exams/question-bank/generate")
    suspend fun generateQuestions(
        @Query("schoolId") schoolId: String,
        @Body request: GenerateQuestionsRequest
    ): Response<GenerateQuestionsResponse>

    @POST("api/mobile/exams/question-bank/{id}/bookmark")
    suspend fun toggleBookmark(
        @Path("id") questionId: String,
        @Query("schoolId") schoolId: String
    ): Response<BookmarkResponse>

    @GET("api/mobile/exams/{examId}/results")
    suspend fun getExamResults(
        @Path("examId") examId: String,
        @Query("schoolId") schoolId: String
    ): Response<DetailedExamResultDto>

    @GET("api/mobile/exams/{examId}/certificate")
    suspend fun getExamCertificate(
        @Path("examId") examId: String,
        @Query("schoolId") schoolId: String
    ): Response<ExamCertificateDto>
}
