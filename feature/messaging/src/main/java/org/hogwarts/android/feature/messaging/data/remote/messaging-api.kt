package org.hogwarts.android.feature.messaging.data.remote

import org.hogwarts.android.feature.messaging.data.remote.dto.ConversationListResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.MessageDto
import org.hogwarts.android.feature.messaging.data.remote.dto.MessageListResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.ProfileDto
import org.hogwarts.android.feature.messaging.data.remote.dto.SendMessageBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The routes the phone Messages screens read and write. Only routes that exist
 * in hogwarts `src/app/api/mobile/` are declared here.
 */
interface MessagingApi {

    @GET("api/mobile/conversations")
    suspend fun getConversations(): Response<ConversationListResponse>

    @GET("api/mobile/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("cursor") cursor: String? = null,
    ): Response<MessageListResponse>

    @POST("api/mobile/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body body: SendMessageBody,
    ): Response<MessageDto>

    @POST("api/mobile/conversations/{conversationId}/read")
    suspend fun markAsRead(@Path("conversationId") conversationId: String): Response<Unit>

    /** Username, photo, bio and school name for the Settings tab and own-message ticks. */
    @GET("api/mobile/profile")
    suspend fun getProfile(): Response<ProfileDto>

    companion object {
        /** The web loads threads 50 at a time. */
        const val PAGE_SIZE = 50
    }
}
