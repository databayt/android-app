package org.hogwarts.android.feature.exams.data.remote

import org.hogwarts.android.feature.exams.data.remote.dto.AdminStatsDto
import org.hogwarts.android.feature.exams.data.remote.dto.ChildrenResponse
import org.hogwarts.android.feature.exams.data.remote.dto.ExamDto
import org.hogwarts.android.feature.exams.data.remote.dto.ExamListResponse
import org.hogwarts.android.feature.exams.data.remote.dto.ExamResultDto
import org.hogwarts.android.feature.exams.data.remote.dto.GradesResponse
import org.hogwarts.android.feature.exams.data.remote.dto.OnlineExamDto
import org.hogwarts.android.feature.exams.data.remote.dto.ProfileDto
import org.hogwarts.android.feature.exams.data.remote.dto.QuestionListResponse
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswersRequest
import org.hogwarts.android.feature.exams.data.remote.dto.SubmitAnswersResponse
import org.hogwarts.android.feature.exams.data.remote.dto.TeacherClassesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** The hogwarts mobile routes behind `/exams`. The school comes from the JWT. */
interface ExamsApi {

    @GET("api/mobile/exams")
    suspend fun getExams(
        @Query("status") status: String? = null,
        @Query("upcoming") upcoming: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): Response<ExamListResponse>

    @GET("api/mobile/exams/{id}")
    suspend fun getExam(@Path("id") examId: String): Response<ExamDto>

    @GET("api/mobile/exams/{id}/results")
    suspend fun getExamResult(@Path("id") examId: String): Response<ExamResultDto>

    /** Not a pure read: starts or resumes the student's exam session. Call it only on "Take Exam". */
    @GET("api/mobile/exams/{id}/online")
    suspend fun startOnlineExam(@Path("id") examId: String): Response<OnlineExamDto>

    @POST("api/mobile/exams/{id}/answers")
    suspend fun submitAnswers(@Path("id") examId: String, @Body body: SubmitAnswersRequest): Response<SubmitAnswersResponse>

    @GET("api/mobile/exams/question-bank")
    suspend fun getQuestionBank(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): Response<QuestionListResponse>

    @GET("api/mobile/admin/stats")
    suspend fun getAdminStats(): Response<AdminStatsDto>

    @GET("api/mobile/teacher/classes")
    suspend fun getTeacherClasses(): Response<TeacherClassesResponse>

    @GET("api/mobile/profile")
    suspend fun getProfile(): Response<ProfileDto>

    @GET("api/mobile/guardian/children")
    suspend fun getChildren(): Response<ChildrenResponse>

    @GET("api/mobile/grades/student/{id}")
    suspend fun getStudentGrades(@Path("id") studentId: String, @Query("per_page") perPage: Int? = null): Response<GradesResponse>

    @GET("api/mobile/guardian/children/{id}/grades")
    suspend fun getChildGrades(@Path("id") childId: String, @Query("per_page") perPage: Int? = null): Response<GradesResponse>
}
