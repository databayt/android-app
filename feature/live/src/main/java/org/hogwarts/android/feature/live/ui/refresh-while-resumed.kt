package org.hogwarts.android.feature.live.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

/** How often a list that stays on screen is read again. */
const val LIVE_REFRESH_EVERY_MS = 30_000L

/**
 * Runs [refresh] every time the screen comes back to the front (ON_RESUME) and every
 * [LIVE_REFRESH_EVERY_MS] while it stays there — so a teacher who finishes one class and returns to the
 * list sees the next one at once. The reads are background reads: callers keep the old content on screen.
 */
@Composable
fun RefreshWhileResumed(refresh: () -> Unit) {
    val latest = rememberUpdatedState(refresh)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            latest.value()
            while (true) {
                delay(LIVE_REFRESH_EVERY_MS)
                latest.value()
            }
        }
    }
}
