package org.hogwarts.android.feature.notifications.domain

import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationType

/**
 * Mirrors web `src/components/school-dashboard/notifications/config.ts`.
 * Centralized configuration that the UI/preferences layers consume.
 */
object NotificationConfig {

    enum class FilterTab { ALL, UNREAD }

    val filterTabs: List<FilterTab> = listOf(FilterTab.ALL, FilterTab.UNREAD)

    /** Channels the mobile client currently supports surface-level toggling for. */
    val supportedChannels: List<NotificationChannel> = listOf(
        NotificationChannel.IN_APP,
        NotificationChannel.EMAIL,
        NotificationChannel.PUSH
    )

    /** Notification types exposed in the preferences screen. */
    val configurableTypes: List<NotificationType> = NotificationType.entries.toList()

    const val NOTIFICATIONS_PER_PAGE = 30
    const val NOTIFICATION_BELL_MAX_DISPLAY = 9 // shown as "9+" beyond this
}
