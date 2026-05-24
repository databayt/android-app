package org.hogwarts.android.feature.idcard.data.remote

import org.hogwarts.android.feature.idcard.data.remote.dto.IdCardDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for ID card endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface IdCardApi {

    @GET("api/mobile/idcard")
    suspend fun getIdCard(
        @Query("schoolId") schoolId: String,
        @Query("userId") userId: String
    ): IdCardDto
}
