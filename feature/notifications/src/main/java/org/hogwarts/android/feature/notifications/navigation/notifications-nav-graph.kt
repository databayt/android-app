package org.hogwarts.android.feature.notifications.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.notifications.ui.NotificationsScreen
import org.hogwarts.android.feature.notifications.ui.NotificationsTab
import org.hogwarts.android.feature.notifications.ui.preferences.NotificationPreferencesScreen

/** Web `/notifications`. */
@Serializable data object Notifications

/** Web `/notifications/unread`. */
@Serializable data object NotificationsUnread

/** Web `/notifications/preferences`. */
@Serializable data object NotificationPreferences

/**
 * @param onOpenHref a card's link, as a locale-less web path the shell resolves.
 * @param onNavigate one of this section's own routes.
 */
fun NavGraphBuilder.notificationsGraph(
    onOpenHref: (String) -> Unit,
    onNavigate: (Any) -> Unit,
) {
    composable<Notifications> {
        NotificationsScreen(
            initialTab = NotificationsTab.All,
            onOpenHref = onOpenHref,
            onOpenPreferences = { onNavigate(NotificationPreferences) },
        )
    }
    composable<NotificationsUnread> {
        NotificationsScreen(
            initialTab = NotificationsTab.Unread,
            onOpenHref = onOpenHref,
            onOpenPreferences = { onNavigate(NotificationPreferences) },
        )
    }
    composable<NotificationPreferences> {
        NotificationPreferencesScreen(
            onSelectTab = { tab -> onNavigate(if (tab == NotificationsTab.Unread) NotificationsUnread else Notifications) },
        )
    }
}
