package org.hogwarts.android.feature.live.ui.room

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.LiveKit
import io.livekit.android.audio.AudioBufferCallback
import io.livekit.android.audio.ScreenAudioCapturer
import io.livekit.android.events.DisconnectReason
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.datastream.StreamTextOptions
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.room.track.LocalAudioTrack
import io.livekit.android.room.track.ScreenSharePresets
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoPreset169
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.room.track.screencapture.ScreenCaptureParams
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import org.hogwarts.android.feature.live.data.repository.LiveRepository
import org.hogwarts.android.feature.live.service.LiveClassService
import org.hogwarts.android.feature.live.ui.room.board.BoardMessage
import org.hogwarts.android.feature.live.ui.room.board.BoardReducer
import org.hogwarts.android.feature.live.ui.room.board.BoardState
import org.hogwarts.android.feature.live.ui.room.board.BoardWire
import org.hogwarts.android.feature.live.ui.room.board.DraftStroke
import org.hogwarts.android.feature.live.ui.room.board.MAX_STROKES
import org.hogwarts.android.feature.live.ui.room.board.Sender
import org.hogwarts.android.feature.live.ui.room.board.Stroke
import org.hogwarts.android.feature.live.ui.room.board.fitStroke
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
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
    /** The participant's identity — what the board's grant names. */
    val identity: String = "",
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
    /** The screen went out without its sound (Android 9 or older, or no microphone permission). */
    data object ShareNoSound : RoomNotice
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
    /** The shared apps' sound goes out with the screen (mixed into this phone's microphone track). */
    val shareAudio: Boolean = false,
    /** HOST or CO_HOST — the teacher's tools. */
    val isHost: Boolean = false,
    val tools: RoomTools = RoomTools(),
    val handUp: Boolean = false,
    val hands: List<HandLine> = emptyList(),
    val chat: List<ChatLine> = emptyList(),
    val chatUnread: Int = 0,
    val notice: RoomNotice? = null,
    /** Entry defaults (mic/camera on) wait for the permission answer; false once applied or not wanted. */
    val entryPending: Boolean = false,
    val wantsMic: Boolean = false,
    val wantsCamera: Boolean = false,
) {
    /** The teacher always; a student only with the school's student-share tool. */
    val canShare: Boolean get() = isHost || tools.studentShare
}

/**
 * `/live/[id]/room`, native: the ticket from `performLiveClassJoin`
 * (`/api/mobile/conference/{id}/join`), then LiveKit. Like the web room,
 * teachers come in live (microphone and camera on) and students follow the room's `joinMuted`;
 * observers never publish. The screen asks for the permissions at entry, then calls [applyEntry].
 */
@HiltViewModel
class LiveRoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val repository: LiveRepository,
) : ViewModel() {
    val sessionId: String = checkNotNull(savedStateHandle["id"])
    val room: Room = LiveKit.create(context)

    /**
     * One connected [Room] per class on this phone. A second back-stack entry for a class this phone is
     * already in (the room, "back to the class page", Join again) builds a second view-model; it must not
     * join again — the server would answer DUPLICATE_IDENTITY and drop the first. It stays idle and the
     * screen shows this one instead.
     */
    val twin: LiveRoomViewModel? = Live.twinOf(sessionId, this)
    /** The class service was started by this view-model (only the owner stops it). */
    private var serviceOn = false
    private var joinJob: kotlinx.coroutines.Job? = null
    private var hostIdentity: String? = null
    private var roomHosts: Set<String> = emptySet()
    /** When this phone first saw each hand go up — the queue's tiebreak. */
    private val handSeen = mutableMapOf<String, Long>()
    private var chatOpen = false
    /** The shared apps' sound while this phone shares its screen (Android 10+), else null. */
    private var screenAudio: ScreenAudioCapturer? = null
    /**
     * While sharing with sound the mic track must stay live (the sound rides on it), so a teacher who
     * turns the mic off gets silence in place of the voice and the video keeps playing to the class.
     */
    @Volatile private var voiceSilenced = false

    private val _state = MutableStateFlow(RoomUiState())
    val state: StateFlow<RoomUiState> = _state.asStateFlow()

    /** The whiteboard, held here so it survives a reconnect and a screen rotation. */
    private val _board = MutableStateFlow(BoardState())
    val board: StateFlow<BoardState> = _board.asStateFlow()

    /**
     * Reliable board messages leave in the order they were made — a stroke and the undo that
     * removes it must not swap on the way out — so they go through one queue, one at a time.
     */
    private val boardOut = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private var boardRestored = false
    private var boardSavedKey: List<String>? = null
    private var draftPruner: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            room.events.events.collect { event ->
                when (event) {
                    is RoomEvent.Reconnecting -> _state.update { it.copy(phase = RoomPhase.Reconnecting) }
                    is RoomEvent.Reconnected -> {
                        _state.update { it.copy(phase = RoomPhase.Connected) }
                        askForBoard()
                    }
                    is RoomEvent.Connected -> _state.update { it.copy(phase = RoomPhase.Connected) }
                    // The host arriving after us: ask again, they may have no board state for us yet.
                    is RoomEvent.ParticipantConnected -> if (event.participant.identity?.value == hostIdentity) askForBoard()
                    is RoomEvent.Disconnected -> {
                        stopClassService()
                        _state.update {
                            if (it.phase is RoomPhase.Over) it else it.copy(phase = RoomPhase.Over(why(event.reason)))
                        }
                    }
                    is RoomEvent.RoomMetadataChanged -> roomHosts = ClassWire.roomHosts(event.newMetadata)
                    is RoomEvent.DataReceived -> if (event.topic == ClassWire.CLASS_TOPIC) onClassMessage(event)
                    else -> Unit
                }
                refresh()
            }
        }
        viewModelScope.launch { for (job in boardOut) runCatching { job() } }
        if (twin == null) {
            Live.register(sessionId, this)
            join()
        }
    }

    fun join() {
        // One connected room per class: already in (or on the way in), the call is a no-op.
        val phase = _state.value.phase
        if (joinJob?.isActive == true || phase is RoomPhase.Connected || phase is RoomPhase.Reconnecting) return
        _state.update { it.copy(phase = RoomPhase.Joining) }
        joinJob = viewModelScope.launch {
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
            val live = RoomDefaults.startsLive(ticket.role, tools.joinMuted)
            _state.update {
                it.copy(
                    isHost = RoomDefaults.isHost(ticket.role), tools = tools,
                    wantsMic = RoomDefaults.micStartsOn(ticket.role, tools.joinMuted),
                    wantsCamera = RoomDefaults.cameraStartsOn(ticket.role, tools.joinMuted),
                    entryPending = false,
                )
            }
            listenForChat()
            // A teacher's screen at 720p/15 — the web's `ScreenSharePresets.h720fps15`, capture AND
            // encoding. Only the capture was set before, so the SDK sized the top layer for the screen
            // at 7 Mbps; on a teacher's mobile uplink in Sudan the students heard her and never saw the
            // screen (أفراح restarted the share 18 times in one class, 11-22). Same ceiling as the web.
            runCatching {
                room.screenShareTrackCaptureDefaults =
                    room.screenShareTrackCaptureDefaults.copy(captureParams = ScreenSharePresets.H720_FPS15.capture)
                room.screenShareTrackPublishDefaults =
                    room.screenShareTrackPublishDefaults.copy(videoEncoding = ScreenSharePresets.H720_FPS15.encoding)
            }
            // A phone camera at 360p — enough for a face, light enough for an old phone on Sudanese data.
            runCatching {
                room.videoTrackCaptureDefaults =
                    room.videoTrackCaptureDefaults.copy(captureParams = VideoPreset169.H360.capture)
            }
            runCatching { room.connect(ticket.wsUrl, ticket.token) }
                .onSuccess {
                    roomHosts = ClassWire.roomHosts(room.metadata)
                    _state.update { it.copy(phase = RoomPhase.Connected, entryPending = live) }
                    startClassService()
                    refresh()
                    if (tools.chat) loadChatHistory()
                    // A student asks the host for the board; the host puts back what it saved.
                    askForBoard()
                    if (_state.value.isHost && tools.whiteboard) rememberBoard()
                }
                .onFailure { _state.update { it.copy(phase = RoomPhase.Refused("LIVE_CLASS_PROVIDER_UNAVAILABLE")) } }
        }
    }

    /** The entry permission prompt is answered: publish what the role's default asks for and the phone may. */
    fun applyEntry(micGranted: Boolean, cameraGranted: Boolean) = viewModelScope.launch {
        val s = _state.value
        if (!s.entryPending) return@launch
        // Settle first so the banner can only show after the attempt.
        if (s.wantsMic && micGranted) {
            val ok = runCatching { room.localParticipant.setMicrophoneEnabled(true) }.getOrDefault(false)
            if (ok) _state.update { it.copy(micOn = true) }
        }
        if (s.wantsCamera && cameraGranted) {
            val ok = runCatching { room.localParticipant.setCameraEnabled(true) }.getOrDefault(false)
            if (ok) _state.update { it.copy(cameraOn = true) }
        }
        _state.update { it.copy(entryPending = false) }
        // The microphone permission may have just arrived: the service takes the microphone type.
        if (micGranted) startClassService()
        refresh()
    }

    fun setMic(on: Boolean) = viewModelScope.launch {
        if (screenAudio != null) {
            voiceSilenced = !on
            _state.update { it.copy(micOn = on) }
            refresh()
            return@launch
        }
        val ok = runCatching { room.localParticipant.setMicrophoneEnabled(on) }.getOrDefault(false)
        if (ok) _state.update { it.copy(micOn = on) }
        if (ok && on) startClassService()
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
            onStop = {
                viewModelScope.launch {
                    stopShareAudio()
                    _state.update { it.copy(shareOn = false) }
                    refresh()
                }
            },
        )
        val ok = runCatching { room.localParticipant.setScreenShareEnabled(true, params) }.getOrDefault(false)
        _state.update { it.copy(shareOn = ok, notice = if (ok) it.notice else RoomNotice.ShareFailed) }
        if (ok && !startShareAudio()) _state.update { it.copy(notice = RoomNotice.ShareNoSound) }
        refresh()
    }

    /**
     * Teachers share educational videos and websites, so the sound goes with the picture:
     * Android 10's playback capture of the shared apps, mixed into this phone's microphone track
     * (the web and the other phones hear it as the teacher's audio). Needs the microphone
     * permission. False when the screen goes out without sound.
     */
    private suspend fun startShareAudio(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!granted) return false
        val voiceWasOn = _state.value.micOn
        voiceSilenced = !voiceWasOn
        if (!voiceWasOn && !runCatching { room.localParticipant.setMicrophoneEnabled(true) }.getOrDefault(false)) {
            voiceSilenced = false
            return false
        }
        val screen = room.localParticipant.getTrackPublication(Track.Source.SCREEN_SHARE)?.track
        val mic = room.localParticipant.getTrackPublication(Track.Source.MICROPHONE)?.track as? LocalAudioTrack
        val capturer = screen?.let { runCatching { ScreenAudioCapturer.createFromScreenShareTrack(it) }.getOrNull() }
        if (mic == null || capturer == null) {
            restoreVoice(voiceWasOn)
            return false
        }
        capturer.gain = SHARE_AUDIO_GAIN
        mic.setAudioBufferCallback(object : AudioBufferCallback {
            override fun onBuffer(
                buffer: ByteBuffer, audioFormat: Int, channelCount: Int, sampleRate: Int, bytesRead: Int, captureTimeNs: Long,
            ): Long {
                if (voiceSilenced) silence(buffer, bytesRead)
                return capturer.onBuffer(buffer, audioFormat, channelCount, sampleRate, bytesRead, captureTimeNs)
            }
        })
        screenAudio = capturer
        _state.update { it.copy(shareAudio = true) }
        return true
    }

    /** Stops the shared sound and puts the microphone back the way the teacher left it. */
    private suspend fun stopShareAudio() {
        val capturer = screenAudio ?: return
        screenAudio = null
        (room.localParticipant.getTrackPublication(Track.Source.MICROPHONE)?.track as? LocalAudioTrack)
            ?.setAudioBufferCallback(null)
        runCatching { capturer.releaseAudioResources() }
        restoreVoice(on = !voiceSilenced)
        _state.update { it.copy(shareAudio = false) }
    }

    private suspend fun restoreVoice(on: Boolean) {
        voiceSilenced = false
        if (!on) runCatching { room.localParticipant.setMicrophoneEnabled(false) }
        _state.update { it.copy(micOn = on) }
    }

    fun stopShare() = viewModelScope.launch {
        stopShareAudio()
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
        // The board is the one class tool students speak on: their strokes come from anyone and the
        // reducer decides what each sender may do.
        BoardWire.decode(event.data)?.let { onBoardMessage(it, Sender(from, fromHost)); return }
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

    // ---- the whiteboard (hogwarts `live/room/board/`, wire in `board/board-wire.kt`) ----

    val localIdentity: String get() = room.localParticipant.identity?.value.orEmpty()

    private fun canDraw(): Boolean = BoardReducer.canDrawOn(_board.value.grant, localIdentity, _state.value.isHost)

    private fun onBoardMessage(msg: BoardMessage, from: Sender) {
        when (msg) {
            BoardMessage.SyncRequest -> if (_state.value.isHost && answersSync()) {
                boardOut.trySend { answerSync(from.identity) }
            }
            is BoardMessage.DraftMsg -> {
                _board.update { BoardReducer.applyDraft(it, msg.draft, from, System.currentTimeMillis()) }
                pruneDraftsSoon()
            }
            else -> _board.update { BoardReducer.apply(it, msg, from) }
        }
    }

    /**
     * Host: the late joiner's snapshot, then every stroke, addressed to the asker alone — the
     * web's `answerSync`. A phone holds no questions, poll, slides, timer or quiz, so those go
     * out empty; a co-host therefore answers only when the class's own host is not in the room
     * (otherwise its empty snapshot would land after the real one and blank it).
     */
    private fun answersSync(): Boolean {
        val primary = hostIdentity
        return primary == null || primary == localIdentity || room.remoteParticipants.keys.none { it.value == primary }
    }

    private suspend fun answerSync(identity: String) {
        val to = listOf(Participant.Identity(identity))
        val b = _board.value
        publish(BoardWire.encode(BoardMessage.SyncState(b.show, b.strokes.size, b.grant)), DataPublishReliability.RELIABLE, to)
        for (s in b.strokes) publish(BoardWire.encode(BoardMessage.StrokeMsg(s)), DataPublishReliability.RELIABLE, to)
    }

    /** Student: ask the host for the board — on connect, on reconnect and when the host arrives. */
    private fun askForBoard() {
        if (_state.value.isHost || !_state.value.tools.whiteboard) return
        val host = hostIdentity ?: return
        boardOut.trySend { publish(BoardWire.encode(BoardMessage.SyncRequest), DataPublishReliability.RELIABLE, listOf(Participant.Identity(host))) }
    }

    private suspend fun publish(bytes: ByteArray, reliability: DataPublishReliability, to: List<Participant.Identity>?) {
        val result = runCatching { room.localParticipant.publishData(bytes, reliability, ClassWire.CLASS_TOPIC, to) }
        val failure = result.exceptionOrNull() ?: result.getOrNull()?.exceptionOrNull()
        if (failure != null) android.util.Log.w("LiveRoom", "board message not sent", failure)
    }

    /** One reliable board message: applied here at once (our own messages do not echo), then sent in order. */
    private fun sendBoard(msg: BoardMessage) {
        val me = Sender(localIdentity, _state.value.isHost)
        _board.update { BoardReducer.apply(it, msg, me) }
        boardOut.trySend { publish(BoardWire.encode(msg), DataPublishReliability.RELIABLE, null) }
    }

    /** A finished stroke, fitted under the message cap. Only someone who may draw sends one. */
    fun boardStroke(stroke: Stroke) {
        if (canDraw()) sendBoard(BoardMessage.StrokeMsg(fitStroke(stroke)))
    }

    /** The eraser and undo: a host removes anything, a student only their own (the reducer decides). */
    fun boardRemove(ids: List<String>) {
        if (ids.isNotEmpty() && canDraw()) sendBoard(BoardMessage.Remove(ids))
    }

    /** A stroke in progress — lossy, so a dropped frame costs nothing. */
    fun boardDraft(draft: DraftStroke) {
        if (!canDraw()) return
        val bytes = BoardWire.encode(BoardMessage.DraftMsg(draft))
        viewModelScope.launch { publish(bytes, DataPublishReliability.LOSSY, null) }
    }

    fun boardClear() {
        if (_state.value.isHost) sendBoard(BoardMessage.Clear)
    }

    /** Host: who besides the hosts may draw — everyone (`*`), one identity, or no one (null). */
    fun boardGrant(to: String?) {
        if (_state.value.isHost) sendBoard(BoardMessage.Grant(to))
    }

    /** Host: put the board on everyone's stage, or take it down. */
    fun boardShow(on: Boolean) {
        if (_state.value.isHost && _state.value.tools.whiteboard) sendBoard(BoardMessage.Show(on))
    }

    /** Remote drafts that stopped growing are dropped; a ticker runs only while there are drafts. */
    private fun pruneDraftsSoon() {
        if (draftPruner?.isActive == true) return
        draftPruner = viewModelScope.launch {
            while (_board.value.drafts.isNotEmpty()) {
                delay(BoardReducer.DRAFT_TTL_MS / 2)
                _board.update { BoardReducer.pruneDrafts(it, System.currentTimeMillis()) }
            }
        }
    }

    /**
     * Host, best-effort: put the saved board back when this room's is empty (each stroke
     * broadcast, as the web's board memory does), then save the page four seconds after it
     * last changed. Any failure is silent.
     */
    private fun rememberBoard() {
        viewModelScope.launch {
            val saved = runCatching { repository.getBoard(sessionId) }.getOrNull() ?: JsonArray(emptyList())
            if (_board.value.strokes.isEmpty()) {
                saved.mapNotNull { BoardWire.strokeFrom(it) }.forEach { sendBoard(BoardMessage.StrokeMsg(it)) }
            }
            boardSavedKey = _board.value.strokes.map { it.id }
            boardRestored = true
        }
        viewModelScope.launch {
            _board.map { it.strokes }.distinctUntilChanged { a, b -> a === b }.collectLatest { strokes ->
                val key = strokes.map { it.id }
                if (!boardRestored || key == boardSavedKey) return@collectLatest
                delay(BOARD_SAVE_AFTER_MS)
                boardSavedKey = key
                runCatching {
                    repository.saveBoard(sessionId, JsonArray(strokes.takeLast(MAX_STROKES).map { BoardWire.strokeElement(it) }))
                }
            }
        }
    }

    fun leave() {
        _state.update { it.copy(phase = RoomPhase.Over(RoomPhase.Why.Left)) }
        stopClassService()
        room.disconnect()
    }

    /**
     * The foreground service that keeps the class alive in the background (see [LiveClassService]).
     * Started once connected, with the activity in front; called again it re-enters foreground, which
     * is how a microphone permission granted mid-class upgrades it. Never throws.
     */
    private fun startClassService() {
        if (twin != null) return
        val phase = _state.value.phase
        if (phase !is RoomPhase.Connected && phase !is RoomPhase.Reconnecting) return
        if (LiveClassService.start(context, sessionId, _state.value.title)) serviceOn = true
    }

    private fun stopClassService() {
        if (!serviceOn) return
        serviceOn = false
        LiveClassService.stop(context, sessionId)
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
                // Sharing with sound keeps the mic track live; the teacher's own switch is what counts.
                micOn = if (p == room.localParticipant && screenAudio != null) !voiceSilenced else mic != null && !mic.muted,
                speaking = p.isSpeaking,
                identity = identity,
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
        stopClassService()
        Live.unregister(sessionId, this)
        screenAudio?.let { runCatching { it.releaseAudioResources() } }
        screenAudio = null
        room.disconnect()
        room.release()
    }

    /** The view-models that own a room on this phone, by class id. */
    private object Live {
        private val owners = java.util.concurrent.ConcurrentHashMap<String, WeakReference<LiveRoomViewModel>>()

        /** The view-model already in (or joining) [id]'s room, other than [self]; null when there is none. */
        fun twinOf(id: String, self: LiveRoomViewModel): LiveRoomViewModel? {
            val other = owners[id]?.get()?.takeIf { it !== self } ?: return null
            return when (other.state.value.phase) {
                RoomPhase.Joining, RoomPhase.Connected, RoomPhase.Reconnecting -> other
                else -> null
            }
        }

        fun register(id: String, vm: LiveRoomViewModel) { owners[id] = WeakReference(vm) }

        fun unregister(id: String, vm: LiveRoomViewModel) {
            val current = owners[id] ?: return
            if (current.get() === vm) owners.remove(id, current)
        }
    }
}

/** The host's board is saved this long after it last changed (the web's `SAVE_AFTER_MS`). */
private const val BOARD_SAVE_AFTER_MS = 4_000L

/** The shared sound against the teacher's voice in the one mic track (1.0 = as played). */
private const val SHARE_AUDIO_GAIN = 0.8f

/** Zeroes the voice samples of one mic buffer, leaving its position alone. */
private fun silence(buffer: ByteBuffer, bytes: Int) {
    val start = buffer.position()
    val end = minOf(start + bytes, buffer.limit())
    for (i in start until end) buffer.put(i, 0)
}
