package org.hogwarts.android.feature.live.data.remote

import org.hogwarts.android.feature.live.data.remote.dto.LiveRecordingListResponse
import org.hogwarts.android.feature.live.data.remote.dto.LiveSessionListResponse
import org.hogwarts.android.feature.live.data.remote.dto.RecordingUrlResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The live block's endpoints.
 *
 * Joining is NOT here: `/api/mobile/conference/:id/join` already exists and
 * mints a short-lived ticket, and it is the shell that opens a room. This
 * interface is discovery — what is on, what was, and how to play it back.
 */
interface LiveApi {

    @GET("api/mobile/live/sessions")
    suspend fun getSessions(
        @Query("window") window: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): LiveSessionListResponse

    @GET("api/mobile/live/sessions/{id}/recordings")
    suspend fun getRecordings(@Path("id") sessionId: String): LiveRecordingListResponse

    @GET("api/mobile/live/recordings/{id}/url")
    suspend fun getRecordingUrl(@Path("id") recordingId: String): RecordingUrlResponse
}
