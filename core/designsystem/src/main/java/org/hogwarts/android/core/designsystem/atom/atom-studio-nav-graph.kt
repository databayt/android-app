package org.hogwarts.android.core.designsystem.atom

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object AtomStudioRoute

fun NavGraphBuilder.atomStudioScreen(
    onNavigateBack: () -> Unit
) {
    composable<AtomStudioRoute> {
        AtomStudio(onNavigateBack = onNavigateBack)
    }
}
