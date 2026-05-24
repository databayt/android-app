package org.hogwarts.android.core.sync.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.core.sync.ui.ConflictResolutionScreen
import org.hogwarts.android.core.sync.ui.SyncProgressScreen
import org.hogwarts.android.core.sync.ui.SyncSettingsScreen

@Serializable data object SyncSettings
@Serializable data object SyncConflicts
@Serializable data object SyncProgress

fun NavGraphBuilder.syncSettingsScreen(
    onNavigateBack: () -> Unit
) {
    composable<SyncSettings> {
        SyncSettingsScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.conflictResolutionScreen(
    onNavigateBack: () -> Unit
) {
    composable<SyncConflicts> {
        ConflictResolutionScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.syncProgressScreen(
    onNavigateBack: () -> Unit
) {
    composable<SyncProgress> {
        SyncProgressScreen(onNavigateBack = onNavigateBack)
    }
}
