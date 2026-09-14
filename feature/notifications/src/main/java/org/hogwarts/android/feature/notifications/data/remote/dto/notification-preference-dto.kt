package org.hogwarts.android.feature.notifications.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One (type, channel) row of `GET /api/mobile/notifications/preferences`. */
@Serializable
data class NotificationPreferenceDto(
    val id: String? = null,
    val type: String,
    val channel: String,
    val enabled: Boolean,
    @SerialName("quiet_hours_start") val quietHoursStart: Int? = null,
    @SerialName("quiet_hours_end") val quietHoursEnd: Int? = null,
    @SerialName("digest_enabled") val digestEnabled: Boolean? = null,
    @SerialName("digest_frequency") val digestFrequency: String? = null,
)

/** `defaults` only arrives while the user has no stored rows; the web form ignores it. */
@Serializable
data class NotificationPreferenceListResponse(
    val data: List<NotificationPreferenceDto> = emptyList(),
)

/** The PUT accepts only `enabled` per (type, channel); quiet hours and digest are not writable. */
@Serializable
data class NotificationPreferenceUpdate(
    val type: String,
    val channel: String,
    val enabled: Boolean,
)

@Serializable
data class UpdatePreferencesRequest(val preferences: List<NotificationPreferenceUpdate>)
