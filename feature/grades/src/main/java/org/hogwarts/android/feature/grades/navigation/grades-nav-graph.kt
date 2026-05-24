package org.hogwarts.android.feature.grades.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.grades.ui.GradesScreen

/**
 * Type-safe route for the grades destination.
 */
@Serializable data object Grades

/**
 * Grades navigation graph entry.
 */
fun NavGraphBuilder.gradesScreen(
    onNavigateBack: () -> Unit
) {
    composable<Grades> {
        GradesScreen(onNavigateBack = onNavigateBack)
    }
}
