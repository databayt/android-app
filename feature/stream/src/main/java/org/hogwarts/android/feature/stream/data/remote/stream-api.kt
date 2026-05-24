package org.hogwarts.android.feature.stream.data.remote

import org.hogwarts.android.feature.stream.data.remote.dto.CertificateDto
import org.hogwarts.android.feature.stream.data.remote.dto.ChapterDto
import org.hogwarts.android.feature.stream.data.remote.dto.CourseDto
import org.hogwarts.android.feature.stream.data.remote.dto.EnrollmentDto
import org.hogwarts.android.feature.stream.data.remote.dto.LessonDto
import org.hogwarts.android.feature.stream.data.remote.dto.LessonProgressDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for LMS/Stream endpoints.
 */
interface StreamApi {

    @GET("api/mobile/catalog/subjects")
    suspend fun getCourses(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
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

    @GET("api/mobile/courses/{courseId}/certificate")
    suspend fun getCertificate(
        @Path("courseId") courseId: String,
        @Query("schoolId") schoolId: String
    ): CertificateDto
}
