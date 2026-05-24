package org.hogwarts.android.feature.exams.data.remote

import org.hogwarts.android.feature.exams.data.remote.dto.ExamDto
import org.hogwarts.android.feature.exams.data.remote.dto.ExamListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExamsApi {

    @GET("api/mobile/exams")
    suspend fun getExams(
        @Query("status") status: String? = null
    ): Response<ExamListResponse>

    @GET("api/mobile/exams/{id}")
    suspend fun getExam(
        @Path("id") examId: String
    ): Response<ExamDto>
}
