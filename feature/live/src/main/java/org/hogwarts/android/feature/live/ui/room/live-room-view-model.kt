package org.hogwarts.android.feature.live.ui.room

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.LiveKit
import io.livekit.android.events.DisconnectReason
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.datastream.StreamTextOptions
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.room.track.ScreenSharePresets
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.room.track.screencapture.ScreenCaptureParams
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

/** One chat line — live (LiveKit `lk.chat`) or from the saved history. */
data class ChatLine(val id: String, val identity: String, val name: String, val text: String, val at: Long, val mine: Boolean)

/** A raised hand, in the order it went up. */
data class HandLine(val identity: String, val name: String)

/** A passing notice over the stage (the web's room toasts). */
sealed interface RoomNotice {
    data class HandRaised(val name: String) : RoomNotice
    data object HandLowered : RoomNotice
    data object MutedByHost : RoomNotice
    data object MutedAll : RoomNotice
    data object ShareFailed : RoomNotice
}

data class RoomUiState(
    val phase: RoomPhase = RoomPhase.Joining,
    val title: String = "",
    val tiles: List<Tile> = emptyList(),
    /** A screen share fills the stage when there is one, as the web's `Stage` does. */
    val share: VideoTrack? = null,
    /** This phone is the one sharing (the stage then says so instead of mirroring itself). */
    val shareIsMine: Boolean = false,
    val micOn: Boolean = false,
    val cameraOn: Boolean = false,
    val shareOn: Boolean = false,
    /** HOST or CO_HOST — the teacher's tools. */
    val isHost: Boolean = false,
    val tools: RoomTools = RoomTools(),
    val handUp: Boolean = false,
    val hands: List<HandLine> = emptyList(),
    val chat: List<ChatLine> = emptyList(),
    val chatUnread: Int = 0,
    val notice: RoomNotice? = null,
) {
    /** The teacher always; a student only with the school's student-share tool. */
    val canShare: Boolean get() = isHost || tools.studentShare
}

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
    private var roomHosts: Set<String> = emptySet()
    /** When this phone first saw each hand go up — the queue's tiebreak. */
    private val handSeen = mutableMapOf<String, Long>()
    private var chatOpen = false

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
                    is RoomEvent.RoomMetadataChanged -> roomHosts = ClassWire.roomHosts(event.newMetadata)
                    is RoomEvent.DataReceived -> if (event.topic == ClassWire.CLASS_TOPIC) onClassMessage(event)
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
            val tools = RoomTools.from(ticket.roomConfig)
            _state.update { it.copy(isHost = ticket.role == "HOST" || ticket.role == "CO_HOST", tools = tools) }
            listenForChat()
            // A teacher's screen at 720p/15 — the web's `ScreenSharePresets.h720fps15`.
            runCatching {
                room.screenShareTrackCaptureDefaults =
                    room.screenShareTrackCaptureDefaults.copy(captureParams = ScreenSharePresets.H720_FPS15.capture)
            }
            runCatching { room.connect(ticket.wsUrl, ticket.token) }
                .onSuccess {
                    roomHosts = ClassWire.roomHosts(room.metadata)
                    _state.update { it.copy(phase = RoomPhase.Connected) }
                    refresh()
                    if (tools.chat) loadChatHistory()
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

    /** The system's screen-capture consent came back with [data]: publish the screen. */
    fun startShare(data: Intent) = viewModelScope.launch {
        val params = ScreenCaptureParams(
            mediaProjectionPermissionResultData = data,
            // Stopped from the system (the notification, or the cast tile).
            onStop = { _state.update { it.copy(shareOn = false) }; refresh() },
        )
        val ok = runCatching { room.localParticipant.setScreenShareEnabled(true, params) }.getOrDefault(false)
        _state.update { it.copy(shareOn = ok, notice = if (ok) it.notice else RoomNotice.ShareFailed) }
        refresh()
    }

    fun stopShare() = viewModelScope.launch {
        runCatching { room.localParticipant.setScreenShareEnabled(false) }
        _state.update { it.copy(shareOn = false) }
        refresh()
    }

    /** Student: raise or lower the hand — the same attributes the web reads. */
    fun setHand(up: Boolean) {
        runCatching { room.localParticipant.updateAttributes(ClassWire.handAttributes(up, System.currentTimeMillis())) }
        _state.update { it.copy(handUp = up) }
    }

    /** Host: lower one student's hand (the web's `hand.clear`, addressed). */
    fun clearHand(identity: String) = viewModelScope.launch {
        sendClass("hand.clear", listOf(Participant.Identity(identity)))
    }

    fun clearAllHands() = viewModelScope.launch { sendClass("hand.clearAll") }

    /** One class-channel message, reliable; a failure is logged, never thrown into the UI. */
    private suspend fun sendClass(type: String, to: List<Participant.Identity>? = null) {
        val result = runCatching {
            room.localParticipant.publishData(ClassWire.encode(type), DataPublishReliability.RELIABLE, ClassWire.CLASS_TOPIC, to)
        }
        val failure = result.exceptionOrNull() ?: result.getOrNull()?.exceptionOrNull()
        if (failure != null) android.util.Log.w("LiveRoom", "class message $type not sent", failure)
        else android.util.Log.d("LiveRoom", "class message $type sent to ${to ?: "all"}")
    }

    /** Host: mute every student, then tell the class why (the web host's `muted`). */
    fun muteAll() = viewModelScope.launch {
        if (repository.muteAll(sessionId)) {
            sendClass("muted")
            _state.update { it.copy(notice = RoomNotice.MutedAll) }
        }
    }

    fun sendChat(raw: String) = viewModelScope.launch {
        val text = raw.trim().take(1000)
        if (text.isEmpty()) return@launch
        val info = room.localParticipant.sendText(text, StreamTextOptions(topic = ClassWire.CHAT_TOPIC)).getOrNull()
            ?: return@launch
        val me = room.localParticipant
        addChat(ChatLine(info.id, me.identity?.value.orEmpty(), me.name.orEmpty(), text, info.timestampMs, mine = true))
        // The memory, after the fact — the class already has the message.
        repository.recordChat(sessionId, info.id, text)
    }

    fun setChatOpen(open: Boolean) {
        chatOpen = open
        if (open) _state.update { it.copy(chatUnread = 0) }
    }

    fun noticeShown() = _state.update { it.copy(notice = null) }

    private fun listenForChat() {
        runCatching { room.unregisterTextStreamHandler(ClassWire.CHAT_TOPIC) }
        room.registerTextStreamHandler(ClassWire.CHAT_TOPIC) { reader, from ->
            viewModelScope.launch {
                val text = runCatching { reader.readAll().joinToString("") }.getOrNull() ?: return@launch
                val who = room.getParticipantByIdentity(from)
                addChat(
                    ChatLine(
                        reader.info.id, from.value, who?.name?.takeIf { it.isNotBlank() } ?: from.value,
                        text, reader.info.timestampMs, mine = false,
                    ),
                )
            }
        }
    }

    private suspend fun loadChatHistory() {
        val mine = room.localParticipant.identity?.value
        val saved = repository.getChat(sessionId).map {
            ChatLine(it.id, it.identity, it.name, it.text, it.at, mine = it.identity == mine)
        }
        _state.update { s -> s.copy(chat = (saved + s.chat).distinctBy { it.id }.sortedBy { it.at }) }
    }

    private fun addChat(line: ChatLine) = _state.update { s ->
        if (s.chat.any { it.id == line.id }) s
        else s.copy(
            chat = (s.chat + line).sortedBy { it.at },
            chatUnread = if (line.mine || chatOpen) s.chatUnread else s.chatUnread + 1,
        )
    }

    private fun onClassMessage(event: RoomEvent.DataReceived) {
        val from = event.participant?.identity?.value ?: return
        val fromHost = from == hostIdentity || from in roomHosts
        if (!fromHost) return
        when (ClassWire.typeOf(event.data)) {
            "hand.clear", "hand.clearAll" -> {
                val wasUp = room.localParticipant.attributes[ClassWire.HAND] == "1"
                runCatching { room.localParticipant.updateAttributes(ClassWire.handAttributes(false, 0)) }
                _state.update { it.copy(handUp = false, notice = if (wasUp) RoomNotice.HandLowered else it.notice) }
            }
            "muted" -> _state.update { it.copy(notice = RoomNotice.MutedByHost) }
        }
    }

    fun leave() {
        _state.update { it.copy(phase = RoomPhase.Over(RoomPhase.Why.Left)) }
        room.disconnect()
    }

    private fun refresh() {
        val people: List<Participant> = listOf(room.localParticipant) + room.remoteParticipants.values
        var share: VideoTrack? = null
        var shareIsMine = false
        val tiles = people.map { p ->
            val cam = p.getTrackPublication(Track.Source.CAMERA)
            val screen = p.getTrackPublication(Track.Source.SCREEN_SHARE)
            if (share == null && screen != null && !screen.muted) {
                share = screen.track as? VideoTrack
                shareIsMine = p == room.localParticipant
            }
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
        // Hands: the attributes every client reads, ordered by when they went up.
        val now = System.currentTimeMillis()
        val inputs = people.map { p ->
            val id = p.identity?.value.orEmpty()
            val up = p.attributes[ClassWire.HAND] == "1"
            if (up) handSeen.getOrPut(id) { now } else handSeen.remove(id)
            ClassWire.HandInput(id, p.name?.takeIf { it.isNotBlank() } ?: id, p.attributes[ClassWire.HAND], p.attributes[ClassWire.HAND_AT], handSeen[id] ?: now)
        }
        val queue = ClassWire.handQueue(inputs).map { HandLine(it.identity, it.name) }
        val me = room.localParticipant.identity?.value
        _state.update { s ->
            val newHand = queue.firstOrNull { q -> q.identity != me && s.hands.none { it.identity == q.identity } }
            s.copy(
                tiles = tiles, share = share, shareIsMine = shareIsMine, hands = queue,
                // A host's mute-all mutes the track server-side: follow it.
                micOn = tiles.firstOrNull { it.isLocal }?.micOn ?: s.micOn,
                handUp = queue.any { it.identity == me },
                notice = if (s.isHost && s.tools.hands && newHand != null) RoomNotice.HandRaised(newHand.name) else s.notice,
            )
        }
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
