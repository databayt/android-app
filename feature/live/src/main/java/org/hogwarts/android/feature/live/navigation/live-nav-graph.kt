package org.hogwarts.android.feature.live.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.live.ui.LiveHomeScreen
import org.hogwarts.android.feature.live.ui.room.LiveRoomScreen
import org.hogwarts.android.feature.live.ui.session.LiveRecordingsScreen
import org.hogwarts.android.feature.live.ui.session.LiveSessionScreen
import org.hogwarts.android.feature.live.ui.slot.LiveJoinSlotScreen

@Serializable data object LiveHome
/** `/live/[id]` — one class's page. */
@Serializable data class LiveSession(val id: String)
/** `/live/[id]/recordings`. */
@Serializable data class LiveRecordings(val id: String)
/** `/live/[id]/room` — full screen, no shell header, as the web's own layout. */
@Serializable data class LiveRoom(val id: String)
/**
 * `/live/join/[timetableId]` — a tap on a timetable period. Resolved by the server into the room,
 * an external meeting, a countdown or the reason it is closed; never a screen of its own to stay on.
 */
@Serializable data class LiveJoinSlot(val timetableId: String)

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

/**
 * [onOpenWeb] opens a locale-less web path in the browser (the shell's web handoff), bypassing the href
 * router: the fallback of [LiveJoinSlot] while the server has no `join-slot` endpoint. Null: not wired.
 */
fun NavGraphBuilder.liveSessionScreens(
    onOpenHref: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onClose: () -> Unit,
    onOpenWeb: ((String) -> Unit)? = null,
) {
    composable<LiveSession> { LiveSessionScreen(onOpenHref = onOpenHref, onOpenUrl = onOpenUrl) }
    composable<LiveRecordings> { LiveRecordingsScreen() }
    composable<LiveJoinSlot> {
        LiveJoinSlotScreen(onClose = onClose, onOpenHref = onOpenHref, onOpenUrl = onOpenUrl, onOpenWeb = onOpenWeb)
    }
    composable<LiveRoom> { LiveRoomScreen(onClose = onClose, onOpenHref = onOpenHref, onOpenUrl = onOpenUrl) }
}
