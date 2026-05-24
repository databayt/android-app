package org.hogwarts.android.feature.events.data.remote

import org.hogwarts.android.feature.events.data.remote.dto.EventDto
import org.hogwarts.android.feature.events.data.remote.dto.EventRegistrationDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for events endpoints.
 *
 * schoolId is extracted from the JWT token by the web API.
 * X-School-Id header is added by TenantInterceptor (ignored by web but harmless).
 */
interface EventsApi {

    @GET("api/mobile/events")
    suspend fun getEvents(
        @Query("upcoming") upcoming: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): List<EventDto>

    @GET("api/mobile/events/{id}")
    suspend fun getEvent(
        @Path("id") eventId: String
    ): EventDto

    @POST("api/mobile/events/{id}/register")
    suspend fun registerForEvent(
        @Path("id") eventId: String
    ): EventRegistrationDto

    @DELETE("api/mobile/events/{id}/register")
    suspend fun unregisterFromEvent(
        @Path("id") eventId: String
    )
}
