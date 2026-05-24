package org.hogwarts.android.feature.subjects.data.remote

import org.hogwarts.android.feature.subjects.data.remote.dto.MySubjectsResponse
import org.hogwarts.android.feature.subjects.data.remote.dto.SubjectDetailDto
import org.hogwarts.android.feature.subjects.data.remote.dto.SubjectListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API for subject endpoints.
 */
interface SubjectsApi {

    @GET("api/mobile/subjects")
    suspend fun getSubjects(
        @Query("search") search: String? = null,
        @Query("department") department: String? = null,
        @Query("lang") lang: String? = null,
    ): Response<SubjectListResponse>

    @GET("api/mobile/subjects/{subjectId}")
    suspend fun getSubjectDetail(
        @Path("subjectId") subjectId: String,
        @Query("lang") lang: String? = null,
    ): Response<SubjectDetailDto>

    @GET("api/mobile/subjects/my-subjects")
    suspend fun getMySubjects(
        @Query("lang") lang: String? = null,
    ): Response<MySubjectsResponse>
}
