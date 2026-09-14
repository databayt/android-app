package org.hogwarts.android.feature.timetable.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.timetable.ui.TimetableScreen

@Serializable data object Timetable

/**
 * `/timetable`: the role's view under the section's tabs. [onOpenHref]
 * receives the web paths the app does not rebuild — the admin sub-pages
 * (`/timetable/analytics`, `generate`, `conflicts`, `settings`), the admin grid
 * itself, and LiveKit rooms (`/live/{id}/room`).
 */
fun NavGraphBuilder.timetableGraph(onOpenHref: (href: String) -> Unit) {
    composable<Timetable> {
        TimetableScreen(onOpenHref = onOpenHref)
    }
}
