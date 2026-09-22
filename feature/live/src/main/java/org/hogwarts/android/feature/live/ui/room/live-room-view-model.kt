package org.hogwarts.android.feature.live.ui.room

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.LiveKit
import io.livekit.android.events.DisconnectReason
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.live.data.repository.LiveRepository
import javax.inject.Inject

/** One person in the room, as a tile draws them. */
data class Tile(
    val key: String,
    val name: String,
    val isLocal: Boolean,
    val isHost: Boolean,
    val camera: VideoTrack?,
    val micOn: Boolean,
    val speaking: Boolean,
)

sealed interface RoomPhase {
    data object Joining : RoomPhase
    /** The join was refused; [code] is `performLiveClassJoin`'s error code. */
    data class Refused(val code: String?) : RoomPhase
    data object Connected : RoomPhase
    data object Reconnecting : RoomPhase
    data class Over(val why: Why) : RoomPhase
    /** An external-link class has no SFU room: the web hands the viewer to the vendor meeting. */
    data class External(val url: String?) : RoomPhase

    enum class Why { Ended, Removed, Elsewhere, Lost, Left }
}

data class RoomUiState(
    val phase: RoomPhase = RoomPhase.Joining,
    val title: String = "",
    val tiles: List<Tile> = emptyList(),
    /** A screen share fills the stage when there is one, as the web's `Stage` does. */
    val share: VideoTrack? = null,
    val micOn: Boolean = false,
    val cameraOn: Boolean = false,
)

/**
 * `/live/[id]/room`, native: the ticket from `performLiveClassJoin`
 * (`/api/mobile/conference/{id}/join`), then LiveKit. The phone joins with its
 * microphone and camera off and asks for each the first time it is turned on.
 */
@HiltViewModel
class LiveRoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    private val repository: LiveRepository,
) : ViewModel() {
    val sessionId: String = checkNotNull(savedStateHandle["id"])
    val room: Room = LiveKit.create(context)
    private var hostIdentity: String? = null

    private val _state = MutableStateFlow(RoomUiState())
    val state: StateFlow<RoomUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            room.events.events.collect { event ->
                when (event) {
                    is RoomEvent.Reconnecting -> _state.update { it.copy(phase = RoomPhase.Reconnecting) }
                    is RoomEvent.Reconnected, is RoomEvent.Connected -> _state.update { it.copy(phase = RoomPhase.Connected) }
                    is RoomEvent.Disconnected -> _state.update {
                        if (it.phase is RoomPhase.Over) it else it.copy(phase = RoomPhase.Over(why(event.reason)))
                    }
                    else -> Unit
                }
                refresh()
            }
        }
        join()
    }

    fun join() {
        _state.update { it.copy(phase = RoomPhase.Joining) }
        viewModelScope.launch {
            val page = runCatching { repository.getSession(sessionId) }.getOrNull()
            page?.let { p -> _state.update { it.copy(title = p.title) } }
            if (page?.isExternal == true) {
                _state.update { it.copy(phase = RoomPhase.External(page.meetingUrl)) }
                return@launch
            }
            val result = runCatching { repository.join(sessionId) }.getOrNull()
            val ticket = result?.data
            if (result?.success != true || ticket == null) {
                _state.update { it.copy(phase = RoomPhase.Refused(result?.error)) }
                return@launch
            }
            hostIdentity = ticket.hostIdentity
            runCatching { room.connect(ticket.wsUrl, ticket.token) }
                .onSuccess {
                    _state.update { it.copy(phase = RoomPhase.Connected) }
                    refresh()
                }
                .onFailure { _state.update { it.copy(phase = RoomPhase.Refused("LIVE_CLASS_PROVIDER_UNAVAILABLE")) } }
        }
    }

    fun setMic(on: Boolean) = viewModelScope.launch {
        val ok = runCatching { room.localParticipant.setMicrophoneEnabled(on) }.getOrDefault(false)
        if (ok) _state.update { it.copy(micOn = on) }
        refresh()
    }

    fun setCamera(on: Boolean) = viewModelScope.launch {
        val ok = runCatching { room.localParticipant.setCameraEnabled(on) }.getOrDefault(false)
        if (ok) _state.update { it.copy(cameraOn = on) }
        refresh()
    }

    fun leave() {
        _state.update { it.copy(phase = RoomPhase.Over(RoomPhase.Why.Left)) }
        room.disconnect()
    }

    private fun refresh() {
        val people: List<Participant> = listOf(room.localParticipant) + room.remoteParticipants.values
        var share: VideoTrack? = null
        val tiles = people.map { p ->
            val cam = p.getTrackPublication(Track.Source.CAMERA)
            val screen = p.getTrackPublication(Track.Source.SCREEN_SHARE)
            if (share == null && screen != null && !screen.muted) share = screen.track as? VideoTrack
            val mic = p.getTrackPublication(Track.Source.MICROPHONE)
            val identity = p.identity?.value.orEmpty()
            Tile(
                key = p.sid.value,
                name = p.name?.takeIf { it.isNotBlank() } ?: identity,
                isLocal = p == room.localParticipant,
                isHost = identity.isNotEmpty() && identity == hostIdentity,
                camera = if (cam != null && !cam.muted) cam.track as? VideoTrack else null,
                micOn = mic != null && !mic.muted,
                speaking = p.isSpeaking,
            )
        }.sortedWith(compareByDescending<Tile> { it.isHost }.thenBy { it.isLocal })
        _state.update { it.copy(tiles = tiles, share = share) }
    }

    private fun why(reason: DisconnectReason?): RoomPhase.Why = when (reason) {
        DisconnectReason.PARTICIPANT_REMOVED -> RoomPhase.Why.Removed
        DisconnectReason.DUPLICATE_IDENTITY -> RoomPhase.Why.Elsewhere
        DisconnectReason.ROOM_DELETED, DisconnectReason.ROOM_CLOSED -> RoomPhase.Why.Ended
        DisconnectReason.CLIENT_INITIATED -> RoomPhase.Why.Left
        else -> RoomPhase.Why.Lost
    }

    override fun onCleared() {
        room.disconnect()
        room.release()
    }
}
