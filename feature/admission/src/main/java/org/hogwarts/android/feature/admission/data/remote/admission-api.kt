package org.hogwarts.android.feature.admission.data.remote

import okhttp3.MultipartBody
import org.hogwarts.android.feature.admission.data.remote.dto.AdmissionApplicationDto
import org.hogwarts.android.feature.admission.data.remote.dto.DocumentUploadResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for admission endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface AdmissionApi {

    @GET("api/mobile/admission/applications")
    suspend fun getApplications(
        @Query("schoolId") schoolId: String
    ): List<AdmissionApplicationDto>

    @GET("api/mobile/admission/applications/{id}")
    suspend fun getApplication(
        @Path("id") applicationId: String,
        @Query("schoolId") schoolId: String
    ): AdmissionApplicationDto

    @POST("api/mobile/admission/applications")
    suspend fun createApplication(
        @Query("schoolId") schoolId: String,
        @Body application: AdmissionApplicationDto
    ): AdmissionApplicationDto

    @PUT("api/mobile/admission/applications/{id}")
    suspend fun updateApplication(
        @Path("id") applicationId: String,
        @Query("schoolId") schoolId: String,
        @Body application: AdmissionApplicationDto
    ): AdmissionApplicationDto

    @POST("api/mobile/admission/applications/{id}/submit")
    suspend fun submitApplication(
        @Path("id") applicationId: String,
        @Query("schoolId") schoolId: String
    ): AdmissionApplicationDto

    @Multipart
    @POST("api/mobile/admission/applications/{id}/documents")
    suspend fun uploadDocument(
        @Path("id") applicationId: String,
        @Query("schoolId") schoolId: String,
        @Part file: MultipartBody.Part
    ): DocumentUploadResponseDto
}
