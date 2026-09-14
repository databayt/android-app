package org.hogwarts.android.core.push

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** `POST /api/mobile/notifications/register` — stores this device's FCM token for the signed-in user. */
interface DeviceTokenApi {
    @POST("api/mobile/notifications/register")
    suspend fun register(@Body body: RegisterDeviceTokenRequest): Response<Unit>
}

@Serializable
data class RegisterDeviceTokenRequest(
    @SerialName("device_token") val deviceToken: String,
    val platform: String = "android"
)
