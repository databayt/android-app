package org.hogwarts.android.feature.live.data.remote

import org.hogwarts.android.feature.live.data.remote.dto.LiveLandingResponse
import org.hogwarts.android.feature.live.data.remote.dto.LiveRecordingListResponse
import org.hogwarts.android.feature.live.data.remote.dto.RecordingUrlResponse
import org.hogwarts.android.feature.live.data.remote.dto.JoinResponseDto
import org.hogwarts.android.feature.live.data.remote.dto.SessionPageDto
import retrofit2.Response
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

    @GET("api/mobile/live/landing")
    suspend fun getLanding(@Query("lang") lang: String? = null): LiveLandingResponse

    @GET("api/mobile/live/sessions/{id}/recordings")
    suspend fun getRecordings(@Path("id") sessionId: String): LiveRecordingListResponse

    @GET("api/mobile/live/recordings/{id}/url")
    suspend fun getRecordingUrl(@Path("id") recordingId: String): RecordingUrlResponse

    /** `/live/[id]` — the page's loader, words included. */
    @GET("api/mobile/live/sessions/{id}")
    suspend fun getSession(@Path("id") id: String, @Query("lang") lang: String): SessionPageDto

    /** The room ticket (`performLiveClassJoin`), errors in-band. */
    @GET("api/mobile/conference/{id}/join")
    suspend fun join(@Path("id") id: String): Response<JoinResponseDto>
}
