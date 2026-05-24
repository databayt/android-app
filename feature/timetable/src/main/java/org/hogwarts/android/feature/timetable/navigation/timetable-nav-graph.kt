package org.hogwarts.android.feature.timetable.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.timetable.ui.TimetableScreen

@Serializable data object Timetable

fun NavGraphBuilder.timetableScreen(
    onNavigateBack: () -> Unit
) {
    composable<Timetable> {
        TimetableScreen(onNavigateBack = onNavigateBack)
    }
}
