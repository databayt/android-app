package org.hogwarts.android.feature.notifications.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.notifications.ui.NotificationsScreen
import org.hogwarts.android.feature.notifications.ui.preferences.NotificationPreferencesScreen

@Serializable data object Notifications

@Serializable data object NotificationPreferences

fun NavGraphBuilder.notificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPreferences: () -> Unit
) {
    composable<Notifications> {
        NotificationsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToPreferences = onNavigateToPreferences
        )
    }
}

fun NavGraphBuilder.notificationPreferencesScreen(
    onNavigateBack: () -> Unit
) {
    composable<NotificationPreferences> {
        NotificationPreferencesScreen(onNavigateBack = onNavigateBack)
    }
}

/**
 * Convenience helper that wires both routes — call this from the app's nav host.
 */
fun NavGraphBuilder.notificationsGraph(navController: NavController) {
    notificationsScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToPreferences = { navController.navigate(NotificationPreferences) }
    )
    notificationPreferencesScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}
