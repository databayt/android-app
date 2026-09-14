package org.hogwarts.android.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.settings.ui.SettingsScreen

/** Web `/settings`. */
@Serializable data object Settings

fun NavGraphBuilder.settingsGraph(
    onOpenHref: (String) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onLogout: () -> Unit,
) {
    composable<Settings> {
        SettingsScreen(
            onOpenHref = onOpenHref,
            onOpenNotificationPreferences = onOpenNotificationPreferences,
            onLogout = onLogout,
        )
    }
}
