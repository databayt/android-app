package org.hogwarts.android.feature.notifications.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferenceDto(
    val id: String? = null,
    val type: String,
    val channel: String,
    val enabled: Boolean,
    @SerialName("quiet_hours_start") val quietHoursStart: Int? = null,
    @SerialName("quiet_hours_end") val quietHoursEnd: Int? = null,
    @SerialName("digest_enabled") val digestEnabled: Boolean? = null,
    @SerialName("digest_frequency") val digestFrequency: String? = null
)

@Serializable
data class NotificationPreferenceDefaults(
    @SerialName("in_app") val inApp: Boolean = true,
    val email: Boolean = true,
    val push: Boolean = true,
    val sms: Boolean = false,
    @SerialName("quiet_hours_start") val quietHoursStart: Int = 22,
    @SerialName("quiet_hours_end") val quietHoursEnd: Int = 7
)

@Serializable
data class NotificationPreferenceListResponse(
    val data: List<NotificationPreferenceDto> = emptyList(),
    val defaults: NotificationPreferenceDefaults? = null
)

@Serializable
data class NotificationPreferenceUpdate(
    val type: String,
    val channel: String,
    val enabled: Boolean
)

@Serializable
data class UpdatePreferencesRequest(
    val preferences: List<NotificationPreferenceUpdate>
)

@Serializable
data class RegisterDeviceTokenRequest(
    @SerialName("device_token") val deviceToken: String,
    val platform: String = "android"
)
