package org.hogwarts.android.feature.live.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.live.ui.LiveHomeScreen

@Serializable data object LiveHome

/**
 * `/live`. Joining a room is not a destination here: the session card hands
 * the href to the shell, which resolves it the way it resolves every other
 * server-given link — a native screen where one exists, the site otherwise.
 */
fun NavGraphBuilder.liveHomeScreen(onOpenHref: (String) -> Unit) {
    composable<LiveHome> {
        LiveHomeScreen(onOpenHref = onOpenHref)
    }
}
