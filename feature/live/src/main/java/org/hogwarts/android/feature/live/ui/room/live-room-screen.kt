package org.hogwarts.android.feature.live.ui.room

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import livekit.org.webrtc.RendererCommon
import org.hogwarts.android.feature.live.R
import org.hogwarts.android.feature.live.ui.session.LiveIcons

private val STAGE = Color(0xFF000000)
private val TILE = Color(0xFF1C1C1E)
/** The lumos player's glass, without the blur Compose cannot draw behind: a dark wash holds over any picture. */
private val GLASS = Color(0xFF1C1C1E).copy(alpha = 0.72f)
private val LIVE_RED = Color(0xFFFF3B30)

/**
 * `/live/[id]/room`, native. The stage the web draws — a screen share when
 * there is one, else the cameras, the lone camera full-bleed, "alone in the
 * room" when there is nobody to show — under glass chrome: the live marker
 * and title at the top, Close at the top end, and the microphone and camera
 * at the foot, with the class tools the school switched on: screen share
 * (the teacher; a student with the student-share tool), raised hands, chat
 * and the host's mute-all — on the web room's own wire (`ClassWire`), so app
 * and web sit in one class. Still left to the web: questions, polls, the
 * whiteboard, slides and the quality menu.
 */
@Composable
fun LiveRoomScreen(
    onClose: () -> Unit,
    onOpenHref: (String) -> Unit,
    onOpenUrl: (String) -> Unit = {},
    viewModel: LiveRoomViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // The update check stays silent while a class is on (see LiveRoomPresence).
    DisposableEffect(Unit) {
        org.hogwarts.android.core.common.brand.LiveRoomPresence.enter()
        onDispose { org.hogwarts.android.core.common.brand.LiveRoomPresence.leave() }
    }
    var pending by remember { mutableStateOf<String?>(null) }
    // Screen share: the system's consent dialog, then the SDK's capture service.
    val share = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) viewModel.startShare(data)
        // A closed dialog is a choice, not a failure: nothing to say.
    }
    fun launchShare() {
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        share.launch(manager.createScreenCaptureIntent())
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when (pending) {
            Manifest.permission.RECORD_AUDIO -> if (granted) viewModel.setMic(true)
            Manifest.permission.CAMERA -> if (granted) viewModel.setCamera(true)
            // The shared sound needs the microphone permission; without it the screen still goes out.
            SHARE_WITH_SOUND -> launchShare()
        }
        pending = null
    }
    fun turnOn(permission: String, enable: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) enable()
        else {
            pending = permission
            ask.launch(permission)
        }
    }
    fun startShare() {
        val mic = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, mic) == PackageManager.PERMISSION_GRANTED) launchShare()
        else {
            pending = SHARE_WITH_SOUND
            ask.launch(mic)
        }
    }
    var panel by remember { mutableStateOf<Panel?>(null) }
    LaunchedEffect(panel) { viewModel.setChatOpen(panel == Panel.Chat) }
    val leave = {
        viewModel.leave()
        onClose()
    }
    // Back inside a class asks first: teachers pressed it by mistake and dropped out of their own class.
    var confirmLeave by remember { mutableStateOf(false) }
    BackHandler(onBack = { if (panel != null) panel = null else confirmLeave = true })
    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text(stringResource(R.string.live_room_leave_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirmLeave = false; leave() }) { Text(stringResource(R.string.live_room_leave)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLeave = false }) { Text(stringResource(R.string.live_room_stay)) }
            },
        )
    }

    // Light status-bar icons over the black stage, restored on the way out.
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val controller = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        val before = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (before != null) {
                controller.isAppearanceLightStatusBars = before
                controller.isAppearanceLightNavigationBars = before
            }
        }
    }

    Box(Modifier.fillMaxSize().background(STAGE)) {
        when (val phase = state.phase) {
            RoomPhase.Joining -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color.White)
            is RoomPhase.External -> androidx.compose.runtime.LaunchedEffect(phase) {
                val url = phase.url
                if (url != null) onOpenUrl(url) else onOpenHref("/live/${viewModel.sessionId}")
                onClose()
            }
            is RoomPhase.Refused -> Notice(
                refusal(phase.code), primary = stringResource(R.string.live_room_back_to_class),
                onPrimary = { onOpenHref("/live/${viewModel.sessionId}") },
            )
            is RoomPhase.Over -> if (phase.why == RoomPhase.Why.Left) Unit else Notice(
                stringResource(
                    when (phase.why) {
                        RoomPhase.Why.Removed -> R.string.live_room_removed_by_host
                        RoomPhase.Why.Elsewhere -> R.string.live_room_opened_elsewhere
                        RoomPhase.Why.Ended -> R.string.live_room_class_ended
                        else -> R.string.live_room_connection_lost
                    },
                ),
                primary = if (phase.why == RoomPhase.Why.Lost) stringResource(R.string.live_room_rejoin) else null,
                onPrimary = viewModel::join,
                secondary = stringResource(R.string.live_room_back_to_class),
                onSecondary = { onOpenHref("/live/${viewModel.sessionId}") },
            )
            RoomPhase.Connected, RoomPhase.Reconnecting -> {
                Stage(viewModel.room, state)
                Chrome(
                    state = state,
                    onClose = leave,
                    onMic = { if (state.micOn) viewModel.setMic(false) else turnOn(Manifest.permission.RECORD_AUDIO) { viewModel.setMic(true) } },
                    onCamera = { if (state.cameraOn) viewModel.setCamera(false) else turnOn(Manifest.permission.CAMERA) { viewModel.setCamera(true) } },
                    onShare = { if (state.shareOn) viewModel.stopShare() else startShare() },
                    onHand = { viewModel.setHand(!state.handUp) },
                    onHands = { panel = if (panel == Panel.Hands) null else Panel.Hands },
                    onChat = { panel = if (panel == Panel.Chat) null else Panel.Chat },
                )
                NoticePill(state.notice, onShown = viewModel::noticeShown)
                when (panel) {
                    Panel.Chat -> ChatPanel(state, onSend = { viewModel.sendChat(it) }, onClose = { panel = null })
                    Panel.Hands -> HandsPanel(
                        state,
                        onLower = { viewModel.clearHand(it) },
                        onLowerAll = viewModel::clearAllHands,
                        onMuteAll = viewModel::muteAll,
                        onClose = { panel = null },
                    )
                    null -> Unit
                }
                if (phase == RoomPhase.Reconnecting) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(color = Color.White)
                            Text(stringResource(R.string.live_room_reconnecting), color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun refusal(code: String?): String = stringResource(
    when (code) {
        "NOT_AUTHENTICATED" -> R.string.live_err_not_authenticated
        "MISSING_SCHOOL", "SCHOOL_NOT_FOUND" -> R.string.live_err_missing_school
        "UNAUTHORIZED" -> R.string.live_err_unauthorized
        "LIVE_CLASS_NOT_FOUND" -> R.string.live_err_not_found
        "LIVE_CLASS_PARTICIPANT_DENIED" -> R.string.live_err_participant_denied
        "LIVE_CLASS_INVALID_STATE" -> R.string.live_err_invalid_state
        "LIVE_CLASS_PROVIDER_UNAVAILABLE" -> R.string.live_err_provider_unavailable
        "LIVE_CLASS_MAX_CONCURRENT" -> R.string.live_err_max_concurrent
        "LIVE_CLASS_ROOM_FULL" -> R.string.live_err_room_full
        else -> R.string.live_err_token_failed
    },
)

@Composable
private fun Stage(room: Room, state: RoomUiState) {
    val share = state.share
    val tiles = state.tiles
    when {
        // Our own screen, drawn back onto itself, would mirror forever: say it instead.
        share != null && state.shareIsMine -> Box(Modifier.fillMaxSize().padding(horizontal = 32.dp), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(LiveIcons.ScreenShare, null, tint = Color.White, modifier = Modifier.size(48.dp))
                Text(stringResource(R.string.live_room_sharing_now), color = Color.White, fontSize = 17.sp, textAlign = TextAlign.Center)
            }
        }
        share != null -> VideoView(room, share, fit = true, modifier = Modifier.fillMaxSize())
        tiles.size == 1 && tiles[0].isLocal && tiles[0].camera == null -> Box(Modifier.fillMaxSize().padding(horizontal = 24.dp), Alignment.Center) {
            Text(stringResource(R.string.live_room_alone_in_room), color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
        tiles.size == 1 -> TileView(room, tiles[0], lone = true, modifier = Modifier.fillMaxSize())
        else -> Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(top = 64.dp, bottom = 96.dp, start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tiles.chunked(2).forEach { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { TileView(room, it, lone = false, modifier = Modifier.weight(1f).fillMaxSize()) }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TileView(room: Room, tile: Tile, lone: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(if (lone) 0.dp else 12.dp)
    Box(
        modifier.clip(shape).background(TILE)
            .then(if (tile.speaking && !lone) Modifier.border(2.dp, Color(0xFF34C759), shape) else Modifier),
    ) {
        val camera = tile.camera
        if (camera != null) {
            VideoView(room, camera, fit = false, mirror = tile.isLocal, modifier = Modifier.fillMaxSize())
        } else {
            Box(
                Modifier.align(Alignment.Center).size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)),
                Alignment.Center,
            ) {
                Text(tile.name.take(1), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (!lone) {
            Row(
                Modifier.align(Alignment.BottomStart).padding(8.dp).clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!tile.micOn) Icon(LiveIcons.MicOff, null, tint = Color.White, modifier = Modifier.size(12.dp))
                val label = when {
                    tile.isLocal -> stringResource(R.string.live_room_you)
                    tile.isHost -> "${tile.name} · ${stringResource(R.string.live_room_host)}"
                    else -> tile.name
                }
                Text(label, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** A LiveKit video track on a texture renderer bound to the room's EGL context. */
@Composable
private fun VideoView(room: Room, track: VideoTrack, fit: Boolean, modifier: Modifier, mirror: Boolean = false) {
    val context = LocalContext.current
    val renderer = remember {
        TextureViewRenderer(context).also { room.initVideoRenderer(it) }
    }
    DisposableEffect(renderer) { onDispose { renderer.release() } }
    DisposableEffect(track) {
        track.addRenderer(renderer)
        onDispose { track.removeRenderer(renderer) }
    }
    AndroidView(
        factory = { renderer },
        update = {
            it.setMirror(mirror)
            it.setScalingType(if (fit) RendererCommon.ScalingType.SCALE_ASPECT_FIT else RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        },
        modifier = modifier,
    )
}

/** Glass chrome over the stage, as the lumos player's: live marker and title, Close, then mic and camera. */
@Composable
private fun Chrome(
    state: RoomUiState,
    onClose: () -> Unit,
    onMic: () -> Unit,
    onCamera: () -> Unit,
    onShare: () -> Unit,
    onHand: () -> Unit,
    onHands: () -> Unit,
    onChat: () -> Unit,
) {
    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.align(Alignment.TopStart).padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.weight(1f, fill = false).clip(CircleShape).background(GLASS).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(LIVE_RED))
                Text(stringResource(R.string.live_room_live), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (state.title.isNotBlank()) {
                    Text(state.title, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(Modifier.weight(0.01f))
            GlassButton(LiveIcons.X, stringResource(R.string.live_room_leave), onClick = onClose)
        }
        // ONE row of equal glass discs, as the web room's phone row
        // (`control-bar.tsx`): camera · mic · hand(s) · share · discussion.
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassButton(
                if (state.cameraOn) LiveIcons.Video else LiveIcons.VideoOff,
                stringResource(if (state.cameraOn) R.string.live_room_camera else R.string.live_room_camera_off),
                // Off tints the glyph rather than lighting a second red disc beside the mic's.
                glyph = if (state.cameraOn) Color.White else CAMERA_OFF_RED, onClick = onCamera,
            )
            GlassButton(
                if (state.micOn) LiveIcons.Mic else LiveIcons.MicOff,
                stringResource(if (state.micOn) R.string.live_room_mic else R.string.live_room_mic_muted),
                // Muted keeps the red disc: the one alarm in the row.
                fill = if (state.micOn) null else MUTED_RED, onClick = onMic,
            )
            if (state.tools.hands) {
                if (state.isHost) {
                    GlassButton(LiveIcons.Hand, stringResource(R.string.live_room_hands), badge = state.hands.size, onClick = onHands)
                } else {
                    GlassButton(
                        LiveIcons.Hand,
                        stringResource(if (state.handUp) R.string.live_room_lower_hand else R.string.live_room_raise_hand),
                        fill = if (state.handUp) AMBER else null, glyph = if (state.handUp) Color.Black else Color.White,
                        onClick = onHand,
                    )
                }
            }
            if (state.canShare) {
                GlassButton(
                    if (state.shareOn) LiveIcons.ScreenShareOff else LiveIcons.ScreenShare,
                    stringResource(if (state.shareOn) R.string.live_room_stop_share else R.string.live_room_share),
                    fill = if (state.shareOn) SHARE_BLUE else null, onClick = onShare,
                )
            }
            if (state.tools.chat) {
                GlassButton(LiveIcons.Message, stringResource(R.string.live_room_chat), badge = state.chatUnread, onClick = onChat)
            }
        }
    }
}

/** A 48dp glass disc — every control in the row, one size (the web's `disc`). */
@Composable
private fun GlassButton(
    icon: ImageVector,
    label: String,
    fill: Color? = null,
    glyph: Color = Color.White,
    badge: Int = 0,
    onClick: () -> Unit,
) {
    Box(Modifier.size(48.dp)) {
        Box(
            Modifier.matchParentSize().clip(CircleShape).background(fill ?: GLASS)
                .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
            Alignment.Center,
        ) {
            Icon(icon, label, tint = glyph, modifier = Modifier.size(22.dp))
        }
        if (badge > 0) {
            Text(
                badge.toString(), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopEnd).padding().clip(CircleShape).background(AMBER)
                    .widthIn(min = 20.dp).padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
    }
}

/** The row's colours, from the web room: muted red disc, camera-off red glyph, sharing blue, a raised hand amber. */
private val MUTED_RED = Color(0xFFDC2626).copy(alpha = 0.85f)
private val CAMERA_OFF_RED = Color(0xFFF87171)
private val AMBER = Color(0xFFFBBF24)
private val SHARE_BLUE = Color(0xFF0284C7).copy(alpha = 0.9f)

private enum class Panel { Chat, Hands }

/** `pending` marker: the microphone permission was asked on the way to a screen share. */
private const val SHARE_WITH_SOUND = "share-with-sound"

/** A notice over the stage for a few seconds — the web's room toasts. */
@Composable
private fun NoticePill(notice: RoomNotice?, onShown: () -> Unit) {
    if (notice == null) return
    LaunchedEffect(notice) {
        kotlinx.coroutines.delay(4000)
        onShown()
    }
    val text = when (notice) {
        is RoomNotice.HandRaised -> stringResource(R.string.live_room_hand_raised_by, notice.name)
        RoomNotice.HandLowered -> stringResource(R.string.live_room_hand_lowered)
        RoomNotice.MutedByHost -> stringResource(R.string.live_room_muted_by_host)
        RoomNotice.MutedAll -> stringResource(R.string.live_room_muted_all)
        RoomNotice.ShareFailed -> stringResource(R.string.live_room_share_failed)
        RoomNotice.ShareNoSound -> stringResource(R.string.live_room_share_no_sound)
    }
    Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 72.dp, start = 16.dp, end = 16.dp), Alignment.TopCenter) {
        Text(
            text, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center,
            modifier = Modifier.clip(CircleShape).background(GLASS).padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

/** A sheet over the lower stage, the call still visible above it. */
@Composable
private fun Sheet(title: String, onClose: () -> Unit, action: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).clickable(onClick = onClose)) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.62f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(TILE)
                .clickable(enabled = false) {}
                .navigationBarsPadding().imePadding(),
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                action?.invoke()
                Box(Modifier.width(8.dp))
                GlassButton(LiveIcons.X, stringResource(R.string.live_room_close), onClick = onClose)
            }
            content()
        }
    }
}

@Composable
private fun ChatPanel(state: RoomUiState, onSend: (String) -> Unit, onClose: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val list = rememberLazyListState()
    LaunchedEffect(state.chat.size) { if (state.chat.isNotEmpty()) list.animateScrollToItem(state.chat.size - 1) }
    Sheet(stringResource(R.string.live_room_chat), onClose) {
        Column(Modifier.fillMaxSize()) {
            if (state.chat.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    Text(stringResource(R.string.live_room_no_messages), color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                    state = list,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.chat, key = { it.id }) { line ->
                        Box(Modifier.fillMaxWidth(), if (line.mine) Alignment.CenterEnd else Alignment.CenterStart) {
                            Column(
                                Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(16.dp))
                                    .background(if (line.mine) SHARE_BLUE else Color.White.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                if (!line.mine) Text(line.name, color = AMBER, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(line.text, color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    if (draft.isEmpty()) Text(stringResource(R.string.live_room_message_placeholder), color = Color.White.copy(alpha = 0.5f), fontSize = 15.sp)
                    BasicTextField(
                        value = draft, onValueChange = { draft = it.take(1000) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                        cursorBrush = SolidColor(Color.White),
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 20.dp),
                    )
                }
                GlassButton(LiveIcons.Send, stringResource(R.string.live_room_send), fill = if (draft.isNotBlank()) SHARE_BLUE else null) {
                    if (draft.isNotBlank()) {
                        onSend(draft)
                        draft = ""
                    }
                }
            }
        }
    }
}

@Composable
private fun HandsPanel(
    state: RoomUiState,
    onLower: (String) -> Unit,
    onLowerAll: () -> Unit,
    onMuteAll: () -> Unit,
    onClose: () -> Unit,
) {
    Sheet(
        stringResource(R.string.live_room_hands), onClose,
        action = {
            Text(
                stringResource(R.string.live_room_mute_all), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).clickable(onClick = onMuteAll)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        },
    ) {
        if (state.hands.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(stringResource(R.string.live_room_no_hands), color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.hands, key = { it.identity }) { hand ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${state.hands.indexOf(hand) + 1}. ${hand.name}", color = Color.White, fontSize = 16.sp,
                            modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            stringResource(R.string.live_room_clear_hand), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(CircleShape).background(AMBER).clickable { onLower(hand.identity) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
                item {
                    Text(
                        stringResource(R.string.live_room_lower_all), color = Color.White, fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))
                            .clickable(onClick = onLowerAll).padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Notice(
    message: String,
    primary: String?,
    onPrimary: () -> Unit,
    secondary: String? = null,
    onSecondary: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(message, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            primary?.let {
                Text(
                    it, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(CircleShape).background(Color.White).clickable(onClick = onPrimary)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
            secondary?.let {
                Text(
                    it, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.clip(CircleShape).background(GLASS).clickable(onClick = onSecondary)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }
}

