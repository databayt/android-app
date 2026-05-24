package org.hogwarts.android.feature.messaging.data.remote

import org.hogwarts.android.feature.messaging.data.remote.dto.ActionSuccessResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.ArchiveBody
import org.hogwarts.android.feature.messaging.data.remote.dto.ContactGroupsResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.ConversationListResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.CreateConversationDto
import org.hogwarts.android.feature.messaging.data.remote.dto.CreateConversationResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.EditMessageBody
import org.hogwarts.android.feature.messaging.data.remote.dto.ForwardBody
import org.hogwarts.android.feature.messaging.data.remote.dto.ForwardResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.MessageDto
import org.hogwarts.android.feature.messaging.data.remote.dto.MessageListResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.MessageSearchResponse
import org.hogwarts.android.feature.messaging.data.remote.dto.MuteBody
import org.hogwarts.android.feature.messaging.data.remote.dto.PinBody
import org.hogwarts.android.feature.messaging.data.remote.dto.ReactionBody
import org.hogwarts.android.feature.messaging.data.remote.dto.SendMessageDto
import org.hogwarts.android.feature.messaging.data.remote.dto.StarBody
import org.hogwarts.android.feature.messaging.data.remote.dto.StarredMessagesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MessagingApi {

    @GET("api/mobile/contacts")
    suspend fun getContacts(
        @Query("locale") locale: String = "en",
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
    ): Response<ContactGroupsResponse>

    @GET("api/mobile/conversations")
    suspend fun getConversations(
        @Query("type") type: String? = null,
    ): Response<ConversationListResponse>

    @GET("api/mobile/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null,
    ): Response<MessageListResponse>

    @POST("api/mobile/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body message: SendMessageDto,
    ): Response<MessageDto>

    @POST("api/mobile/conversations")
    suspend fun createConversation(
        @Body conversation: CreateConversationDto,
    ): Response<CreateConversationResponse>

    @POST("api/mobile/conversations/{conversationId}/read")
    suspend fun markAsRead(
        @Path("conversationId") conversationId: String,
    ): Response<Unit>

    // Message-level actions
    @PATCH("api/mobile/conversations/{conversationId}/messages/{messageId}")
    suspend fun editMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String,
        @Body body: EditMessageBody,
    ): Response<ActionSuccessResponse>

    @DELETE("api/mobile/conversations/{conversationId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String,
    ): Response<ActionSuccessResponse>

    @POST("api/mobile/conversations/{conversationId}/messages/{messageId}/reactions")
    suspend fun addReaction(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String,
        @Body body: ReactionBody,
    ): Response<ActionSuccessResponse>

    @HTTP(method = "DELETE", path = "api/mobile/conversations/{conversationId}/messages/{messageId}/reactions", hasBody = false)
    suspend fun removeReaction(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String,
        @Query("emoji") emoji: String,
    ): Response<ActionSuccessResponse>

    @POST("api/mobile/conversations/{conversationId}/messages/{messageId}/star")
    suspend fun toggleStar(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String,
        @Body body: StarBody,
    ): Response<ActionSuccessResponse>

    @POST("api/mobile/conversations/{conversationId}/messages/{messageId}/forward")
    suspend fun forwardMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String,
        @Body body: ForwardBody,
    ): Response<ForwardResponse>

    // Conversation-level actions
    @POST("api/mobile/conversations/{conversationId}/pin")
    suspend fun togglePin(
        @Path("conversationId") conversationId: String,
        @Body body: PinBody,
    ): Response<ActionSuccessResponse>

    @POST("api/mobile/conversations/{conversationId}/mute")
    suspend fun toggleMute(
        @Path("conversationId") conversationId: String,
        @Body body: MuteBody,
    ): Response<ActionSuccessResponse>

    @POST("api/mobile/conversations/{conversationId}/archive")
    suspend fun archiveConversation(
        @Path("conversationId") conversationId: String,
        @Body body: ArchiveBody,
    ): Response<ActionSuccessResponse>

    @POST("api/mobile/conversations/{conversationId}/leave")
    suspend fun leaveConversation(
        @Path("conversationId") conversationId: String,
    ): Response<ActionSuccessResponse>

    // Search + starred
    @GET("api/mobile/messages/search")
    suspend fun searchMessages(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30,
    ): Response<MessageSearchResponse>

    @GET("api/mobile/conversations/{conversationId}/messages/search")
    suspend fun searchConversationMessages(
        @Path("conversationId") conversationId: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 50,
    ): Response<MessageSearchResponse>

    @GET("api/mobile/starred-messages")
    suspend fun getStarredMessages(
        @Query("limit") limit: Int = 50,
    ): Response<StarredMessagesResponse>

    // WhatsApp bridge endpoints
    @GET("api/mobile/whatsapp/status")
    suspend fun getWhatsAppStatus(): Response<WhatsAppStatusResponse>

    @POST("api/mobile/whatsapp/connect")
    suspend fun connectWhatsApp(): Response<WhatsAppQRResponse>

    @POST("api/mobile/whatsapp/disconnect")
    suspend fun disconnectWhatsApp(): Response<Unit>

    @PUT("api/mobile/conversations/{conversationId}/whatsapp")
    suspend fun toggleConversationWhatsApp(
        @Path("conversationId") conversationId: String,
        @Body body: WhatsAppToggleBody,
    ): Response<Unit>

    @GET("api/mobile/whatsapp/qr")
    suspend fun getWhatsAppQR(): Response<WhatsAppQRResponse>
}

@kotlinx.serialization.Serializable
data class WhatsAppStatusResponse(
    val status: String,
    val phone: String? = null,
    @kotlinx.serialization.SerialName("instance_name") val instanceName: String? = null,
)

@kotlinx.serialization.Serializable
data class WhatsAppQRResponse(
    @kotlinx.serialization.SerialName("qr_code") val qrCode: String? = null,
    val status: String,
)

@kotlinx.serialization.Serializable
data class WhatsAppToggleBody(
    val enabled: Boolean,
)
