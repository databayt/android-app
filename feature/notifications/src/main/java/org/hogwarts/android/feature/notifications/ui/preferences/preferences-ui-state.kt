package org.hogwarts.android.feature.notifications.ui.preferences

import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationPreference
import org.hogwarts.android.feature.notifications.domain.model.NotificationType

data class PreferencesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val preferences: Map<Pair<NotificationType, NotificationChannel>, NotificationPreference> = emptyMap(),
    val error: String? = null,
    val savedAt: Long? = null
) {
    fun isEnabled(type: NotificationType, channel: NotificationChannel): Boolean =
        preferences[type to channel]?.enabled ?: defaultEnabled(channel)

    private fun defaultEnabled(channel: NotificationChannel): Boolean = when (channel) {
        NotificationChannel.IN_APP -> true
        NotificationChannel.EMAIL -> true
        NotificationChannel.PUSH -> true
        else -> false
    }
}
