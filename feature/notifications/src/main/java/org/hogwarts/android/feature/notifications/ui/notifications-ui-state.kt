package org.hogwarts.android.feature.notifications.ui

import org.hogwarts.android.feature.notifications.domain.NotificationConfig
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationType

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
    val selectedTab: NotificationConfig.FilterTab = NotificationConfig.FilterTab.ALL,
    val selectedType: NotificationType? = null,
    val error: String? = null
) {
    /**
     * Tab + type filter applied client-side so switching is instant. Sorting
     * mirrors web center.tsx: urgent first, then newest first.
     */
    val filtered: List<AppNotification>
        get() = notifications
            .asSequence()
            .filter { selectedTab != NotificationConfig.FilterTab.UNREAD || !it.isRead }
            .filter { selectedType == null || it.type == selectedType }
            .sortedWith(
                compareByDescending<AppNotification> {
                    it.priority == org.hogwarts.android.feature.notifications.domain.model.NotificationPriority.URGENT
                }.thenByDescending { it.createdAt }
            )
            .toList()

    val unreadCount: Int get() = notifications.count { !it.isRead }
}
