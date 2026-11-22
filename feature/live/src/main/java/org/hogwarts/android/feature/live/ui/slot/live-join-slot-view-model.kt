package org.hogwarts.android.feature.live.ui.slot

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.live.data.remote.dto.JoinSlotDto
import org.hogwarts.android.feature.live.data.remote.dto.NextClassDto
import org.hogwarts.android.feature.live.data.repository.JoinSlotResult
import org.hogwarts.android.feature.live.data.repository.LiveRepository
import java.time.Instant
import javax.inject.Inject

sealed interface SlotState {
    data object Resolving : SlotState
    /** Not open yet. [deadline] is on the [SystemClock.elapsedRealtime] clock: when to ask again. */
    data class Early(val message: String?, val deadline: Long) : SlotState
    /** Over, cancelled, not today, not online, unavailable: [kind] is the server's word for it. */
    data class Closed(val kind: String, val message: String?, val next: NextClassDto?) : SlotState
    data object Forbidden : SlotState
    /** No answer: offline or a server error. */
    data class Failed(val message: String?) : SlotState
    /** The server has no such endpoint yet: the old behaviour, the web page. */
    data object Legacy : SlotState
}

/** One-shot results of the resolve: where the tap goes. */
sealed interface SlotEffect {
    data class Room(val sessionId: String) : SlotEffect
    data class External(val url: String) : SlotEffect
}

/**
 * `/live/join/<timetableId>`, native: the server says what a tap on a timetable period should do
 * (`GET api/mobile/live/join-slot/{timetableId}`) and this view-model turns the answer into a state
 * to show or an [effect] to follow.
 */
@HiltViewModel
class LiveJoinSlotViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LiveRepository,
) : ViewModel() {
    val timetableId: String = checkNotNull(savedStateHandle["timetableId"])

    private val _state = MutableStateFlow<SlotState>(SlotState.Resolving)
    val state: StateFlow<SlotState> = _state.asStateFlow()

    private val _effects = Channel<SlotEffect>(Channel.BUFFERED)
    val effects: Flow<SlotEffect> = _effects.receiveAsFlow()

    private var job: Job? = null

    init { resolve() }

    /** Ask the server. A countdown that is showing stays on screen while the answer comes. */
    fun resolve() {
        if (job?.isActive == true) return
        if (_state.value !is SlotState.Early) _state.value = SlotState.Resolving
        job = viewModelScope.launch {
            when (val result = repository.joinSlot(timetableId)) {
                JoinSlotResult.NotDeployed -> _state.value = SlotState.Legacy
                JoinSlotResult.Forbidden -> _state.value = SlotState.Forbidden
                is JoinSlotResult.Failed -> _state.value = SlotState.Failed(result.message)
                is JoinSlotResult.Resolved -> apply(result.slot)
            }
        }
    }

    private fun apply(slot: JoinSlotDto) {
        when (slot.kind) {
            "room" -> {
                val id = slot.sessionId
                if (id.isNullOrBlank()) _state.value = SlotState.Closed("unavailable", slot.message, slot.next)
                else _effects.trySend(SlotEffect.Room(id))
            }
            "external" -> {
                val url = slot.url
                if (url.isNullOrBlank()) _state.value = SlotState.Closed("unavailable", slot.message, slot.next)
                else _effects.trySend(SlotEffect.External(url))
            }
            "early" -> {
                val ms = slot.opensInMs?.toLong()
                    ?: slot.opensAt?.let { at ->
                        runCatching { Instant.parse(at).toEpochMilli() - System.currentTimeMillis() }.getOrNull()
                    }
                    ?: 0L
                // At least a few seconds, so an answer that says "early" at zero cannot become a hot loop.
                _state.value = SlotState.Early(
                    message = slot.message,
                    deadline = SystemClock.elapsedRealtime() + ms.coerceAtLeast(MIN_REASK_MS),
                )
            }
            "over", "cancelled", "not_today", "not_online", "unavailable" ->
                _state.value = SlotState.Closed(slot.kind, slot.message, slot.next)
            else -> _state.value = SlotState.Closed("unavailable", slot.message, slot.next)
        }
    }

    private companion object {
        const val MIN_REASK_MS = 5_000L
    }
}
