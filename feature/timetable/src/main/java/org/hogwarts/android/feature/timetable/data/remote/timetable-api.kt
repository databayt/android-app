package org.hogwarts.android.feature.timetable.data.remote

import org.hogwarts.android.feature.timetable.data.remote.dto.TimetableListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API for timetable endpoints.
 */
interface TimetableApi {

    @GET("api/mobile/timetable/{userId}")
    suspend fun getTimetable(
        @Path("userId") userId: String
    ): Response<TimetableListResponse>
}
