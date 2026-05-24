package org.hogwarts.android.feature.notifications.domain.model

/**
 * User preference for a (type, channel) pair.
 *
 * Mirrors the web `NotificationPreferenceDTO` -- one row per type/channel
 * combination. The mobile preferences screen renders this as a matrix.
 */
data class NotificationPreference(
    val type: NotificationType,
    val channel: NotificationChannel,
    val enabled: Boolean,
    val quietHoursStart: Int? = null,
    val quietHoursEnd: Int? = null,
    val digestEnabled: Boolean? = null,
    val digestFrequency: String? = null
)
