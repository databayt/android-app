package org.hogwarts.android.feature.live.data.remote

import org.hogwarts.android.feature.live.data.remote.dto.LiveLandingResponse
import org.hogwarts.android.feature.live.data.remote.dto.LiveRecordingListResponse
import org.hogwarts.android.feature.live.data.remote.dto.RecordingUrlResponse
import org.hogwarts.android.feature.live.data.remote.dto.JoinResponseDto
import org.hogwarts.android.feature.live.data.remote.dto.SessionPageDto
import org.hogwarts.android.feature.live.data.remote.dto.MuteRequestDto
import org.hogwarts.android.feature.live.data.remote.dto.RecordChatRequestDto
import org.hogwarts.android.feature.live.data.remote.dto.RoomChatResponseDto
import org.hogwarts.android.feature.live.data.remote.dto.SimpleResultDto
import org.hogwarts.android.feature.live.data.remote.dto.BoardResponseDto
import org.hogwarts.android.feature.live.data.remote.dto.BoardSaveDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    /** The room chat's saved history (`live/chat-core.ts`), newest page, oldest first. */
    @GET("api/mobile/conference/{id}/chat")
    suspend fun getChat(@Path("id") id: String, @Query("before") before: Long? = null): Response<RoomChatResponseDto>

    /** Save a message this phone just sent over the room — fire-and-forget. */
    @POST("api/mobile/conference/{id}/chat")
    suspend fun recordChat(@Path("id") id: String, @Body body: RecordChatRequestDto): Response<SimpleResultDto>

    /** Host: mute every student's microphone (`live/moderation-core.ts`). */
    @POST("api/mobile/conference/{id}/mute")
    suspend fun mute(@Path("id") id: String, @Body body: MuteRequestDto): Response<SimpleResultDto>

    /** Host: the whiteboard saved for this class, to put back after a reload. */
    @GET("api/mobile/conference/{id}/board")
    suspend fun getBoard(@Path("id") id: String): Response<BoardResponseDto>

    /** Host: save the whiteboard (best-effort, a few seconds after it last changed). */
    @PUT("api/mobile/conference/{id}/board")
    suspend fun saveBoard(@Path("id") id: String, @Body body: BoardSaveDto): Response<SimpleResultDto>
}
