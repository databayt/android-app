package org.hogwarts.android.feature.notifications.data.remote

import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationListResponse
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationPreferenceListResponse
import org.hogwarts.android.feature.notifications.data.remote.dto.RegisterDeviceTokenRequest
import org.hogwarts.android.feature.notifications.data.remote.dto.UpdatePreferencesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    @GET("api/mobile/notifications")
    suspend fun getNotifications(
        @Query("unread") unreadOnly: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<NotificationListResponse>

    @POST("api/mobile/notifications/{notificationId}/read")
    suspend fun markRead(
        @Path("notificationId") notificationId: String
    ): Response<Unit>

    @POST("api/mobile/notifications/read-all")
    suspend fun markAllRead(): Response<Unit>

    @GET("api/mobile/notifications/preferences")
    suspend fun getPreferences(): Response<NotificationPreferenceListResponse>

    @PUT("api/mobile/notifications/preferences")
    suspend fun updatePreferences(
        @Body request: UpdatePreferencesRequest
    ): Response<Unit>

    @POST("api/mobile/notifications/register")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): Response<Unit>
}
