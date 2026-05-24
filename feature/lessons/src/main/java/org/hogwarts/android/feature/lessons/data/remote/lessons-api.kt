package org.hogwarts.android.feature.lessons.data.remote

import org.hogwarts.android.feature.lessons.data.remote.dto.CurriculumMapDto
import org.hogwarts.android.feature.lessons.data.remote.dto.LessonPlanDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for lessons & curriculum endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface LessonsApi {

    @GET("api/mobile/lessons")
    suspend fun getLessonPlans(
        @Query("classId") classId: String? = null,
        @Query("subjectId") subjectId: String? = null
    ): List<LessonPlanDto>

    @GET("api/mobile/lessons/{lessonId}")
    suspend fun getLessonPlan(
        @Path("lessonId") lessonId: String
    ): LessonPlanDto

    @POST("api/mobile/lessons")
    suspend fun createLessonPlan(
        @Body body: LessonPlanDto
    ): LessonPlanDto

    @PUT("api/mobile/lessons/{lessonId}")
    suspend fun updateLessonPlan(
        @Path("lessonId") lessonId: String,
        @Body body: LessonPlanDto
    ): LessonPlanDto

    @GET("api/mobile/curriculum")
    suspend fun getCurriculumMap(
        @Query("termId") termId: String? = null
    ): List<CurriculumMapDto>
}
