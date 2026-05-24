package org.hogwarts.android.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.settings.ui.SettingsScreen

@Serializable data object Settings

fun NavGraphBuilder.settingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    composable<Settings> {
        SettingsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToProfile = onNavigateToProfile,
            onLogout = onLogout
        )
    }
}
