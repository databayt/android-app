package org.hogwarts.android.feature.lumos.data.remote

import org.hogwarts.android.feature.lumos.data.remote.dto.CertificateDto
import org.hogwarts.android.feature.lumos.data.remote.dto.CourseDto
import org.hogwarts.android.feature.lumos.data.remote.dto.EnrollmentDto
import org.hogwarts.android.feature.lumos.data.remote.dto.LessonProgressDto
import org.hogwarts.android.feature.lumos.data.remote.dto.ProposeVideoRequestDto
import org.hogwarts.android.feature.lumos.data.remote.dto.QuizResultDto
import org.hogwarts.android.feature.lumos.data.remote.dto.QuizSubmissionDto
import org.hogwarts.android.feature.lumos.data.remote.dto.ReviewVideoRequestDto
import org.hogwarts.android.feature.lumos.data.remote.dto.VideoDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Lumos (LMS) endpoints matching Hogwarts backend.
 */
interface LumosApi {

    @GET("api/mobile/catalog/subjects")
    suspend fun getCourses(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("grade") grade: Int? = null,
        @Query("lang") lang: String? = null
    ): List<CourseDto>

    @GET("api/mobile/catalog/subjects/{slug}")
    suspend fun getCourse(
        @Path("slug") slug: String,
        @Query("lang") lang: String? = null
    ): CourseDto

    @POST("api/mobile/courses/{courseId}/enroll")
    suspend fun enrollCourse(
        @Path("courseId") courseId: String,
        @Query("schoolId") schoolId: String
    ): EnrollmentDto

    @POST("api/mobile/courses/{courseId}/lessons/{lessonId}/progress")
    suspend fun updateLessonProgress(
        @Path("courseId") courseId: String,
        @Path("lessonId") lessonId: String,
        @Query("schoolId") schoolId: String,
        @Body progress: LessonProgressDto
    ): LessonProgressDto

    @POST("api/mobile/courses/{courseId}/lessons/{lessonId}/quiz")
    suspend fun submitLessonQuiz(
        @Path("courseId") courseId: String,
        @Path("lessonId") lessonId: String,
        @Query("schoolId") schoolId: String,
        @Body submission: QuizSubmissionDto
    ): QuizResultDto

    @GET("api/mobile/courses/{courseId}/certificate")
    suspend fun getCertificate(
        @Path("courseId") courseId: String,
        @Query("schoolId") schoolId: String
    ): CertificateDto

    // Teacher Video Management Endpoints
    @GET("api/lumos/videos/mine")
    suspend fun getMyVideos(
        @Query("schoolId") schoolId: String
    ): List<VideoDto>

    @POST("api/lumos/videos/propose")
    suspend fun proposeVideo(
        @Body request: ProposeVideoRequestDto
    ): VideoDto

    // Admin Video Review Queue
    @GET("api/lumos/videos/pending")
    suspend fun getPendingVideos(
        @Query("schoolId") schoolId: String
    ): List<VideoDto>

    @POST("api/lumos/videos/review")
    suspend fun reviewVideo(
        @Body request: ReviewVideoRequestDto
    ): VideoDto
}
