package org.hogwarts.android.feature.live.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.live.ui.LiveHomeScreen
import org.hogwarts.android.feature.live.ui.room.LiveRoomScreen
import org.hogwarts.android.feature.live.ui.session.LiveRecordingsScreen
import org.hogwarts.android.feature.live.ui.session.LiveSessionScreen

@Serializable data object LiveHome
/** `/live/[id]` — one class's page. */
@Serializable data class LiveSession(val id: String)
/** `/live/[id]/recordings`. */
@Serializable data class LiveRecordings(val id: String)
/** `/live/[id]/room` — full screen, no shell header, as the web's own layout. */
@Serializable data class LiveRoom(val id: String)

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

fun NavGraphBuilder.liveSessionScreens(
    onOpenHref: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onClose: () -> Unit,
) {
    composable<LiveSession> { LiveSessionScreen(onOpenHref = onOpenHref, onOpenUrl = onOpenUrl) }
    composable<LiveRecordings> { LiveRecordingsScreen() }
    composable<LiveRoom> { LiveRoomScreen(onClose = onClose, onOpenHref = onOpenHref, onOpenUrl = onOpenUrl) }
}
