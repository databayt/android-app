package org.hogwarts.android.core.common.brand

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Process-wide "a live class room is on screen" flag. The room composable enters/leaves with a
 * DisposableEffect; the update check reads it so a teacher coming back from another app
 * mid screen-share is never interrupted. A counter, so overlapping enter/leave pairs stay correct.
 */
object LiveRoomPresence {
    private val count = MutableStateFlow(0)

    /** True while at least one room is composed. */
    val inRoom: StateFlow<Boolean> get() = _inRoom
    private val _inRoom = MutableStateFlow(false)

    fun enter() {
        count.update { it + 1 }
        _inRoom.value = true
    }

    fun leave() {
        count.update { (it - 1).coerceAtLeast(0) }
        _inRoom.value = count.value > 0
    }
}
