package org.hogwarts.android.feature.live.ui.slot

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.live.R
import org.hogwarts.android.feature.live.data.remote.dto.NextClassDto
import org.hogwarts.android.feature.live.ui.session.OutlineButton
import org.hogwarts.android.feature.live.ui.session.SolidButton
import java.util.Locale

/**
 * `/live/join/<timetableId>` — a tap on a period of the timetable or the dashboard. The server decides:
 * the room (replacing this screen, so Back does not return here), an external meeting, a countdown
 * while the class is not open yet, or the reason it is closed with the reader's next class.
 *
 * [onOpenWeb] hands a path to the web page when the server has no such endpoint yet (the old behaviour);
 * without it that case shows the unavailable message.
 */
@Composable
fun LiveJoinSlotScreen(
    onClose: () -> Unit,
    onOpenHref: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenWeb: ((String) -> Unit)? = null,
    viewModel: LiveJoinSlotViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = HogwartsTheme.colors

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                // Pop this destination first, then go: Back from the room returns to where the tap was.
                is SlotEffect.Room -> { onClose(); onOpenHref("/live/${effect.sessionId}/room") }
                is SlotEffect.External -> { onOpenUrl(effect.url); onClose() }
            }
        }
    }
    val legacy = state is SlotState.Legacy
    LaunchedEffect(legacy) {
        if (legacy && onOpenWeb != null) {
            onClose()
            onOpenWeb("/live/join/${viewModel.timetableId}")
        }
    }

    val goNext: (NextClassDto) -> Unit = { next ->
        // Replace this resolver with the next class's, so Back skips the one already over.
        onClose()
        onOpenHref("/live/join/${next.timetableId}")
    }

    Box(Modifier.fillMaxSize().background(c.background).navigationBarsPadding()) {
        when (val s = state) {
            SlotState.Resolving -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            SlotState.Legacy -> if (onOpenWeb != null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                SlotMessage(
                    message = stringResource(R.string.live_slot_unavailable),
                    next = null, onNext = goNext, onBack = onClose,
                )
            }
            SlotState.Forbidden -> SlotMessage(
                message = stringResource(R.string.live_slot_forbidden),
                next = null, onNext = goNext, onBack = onClose,
            )
            is SlotState.Failed -> SlotMessage(
                message = stringResource(R.string.live_slot_failed),
                next = null, onNext = goNext, onBack = onClose,
                onRetry = viewModel::resolve,
            )
            is SlotState.Early -> EarlyCountdown(s, onAsk = viewModel::resolve, onBack = onClose)
            is SlotState.Closed -> SlotMessage(
                message = s.message?.takeIf { it.isNotBlank() } ?: stringResource(closedFallback(s.kind)),
                next = s.next?.takeIf { it.timetableId.isNotBlank() },
                onNext = goNext, onBack = onClose,
            )
        }
    }
}

private fun closedFallback(kind: String): Int = when (kind) {
    "over" -> R.string.live_slot_over
    "cancelled" -> R.string.live_slot_cancelled
    "not_today" -> R.string.live_slot_not_today
    "not_online" -> R.string.live_slot_not_online
    else -> R.string.live_slot_unavailable
}

/** The server's message and a countdown to when the room opens; asks again by itself at zero. */
@Composable
private fun EarlyCountdown(state: SlotState.Early, onAsk: () -> Unit, onBack: () -> Unit) {
    val c = HogwartsTheme.colors
    var now by remember(state) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(state, lifecycle) {
        // Only while the screen is in front: back from the background at zero asks at once.
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                now = SystemClock.elapsedRealtime()
                if (now >= state.deadline) {
                    onAsk()
                    // The answer replaces this state (room, a new countdown, a refusal); until then wait.
                    awaitCancellation()
                }
                delay(1_000)
            }
        }
    }
    val remaining = (state.deadline - now).coerceAtLeast(0)
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            state.message?.takeIf { it.isNotBlank() } ?: stringResource(R.string.live_slot_early_title),
            color = c.foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.live_slot_opens_in, formatCountdown(remaining)),
            color = c.mutedForeground, fontSize = 16.sp, textAlign = TextAlign.Center,
        )
        OutlineButton(stringResource(R.string.live_slot_back), null, onBack)
    }
}

@Composable
private fun SlotMessage(
    message: String,
    next: NextClassDto?,
    onNext: (NextClassDto) -> Unit,
    onBack: () -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    val c = HogwartsTheme.colors
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = c.foreground, fontSize = 18.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        if (onRetry != null) SolidButton(stringResource(R.string.live_slot_retry), null, height = 40.dp, onClick = onRetry)
        if (next != null) {
            SolidButton(stringResource(R.string.live_slot_next, next.title), null, height = 40.dp) { onNext(next) }
        }
        OutlineButton(stringResource(R.string.live_slot_back), null, onBack)
    }
}

private fun formatCountdown(ms: Long): String {
    val total = ms / 1_000
    val h = total / 3_600
    val m = (total % 3_600) / 60
    val s = total % 60
    return if (h > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    else String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
