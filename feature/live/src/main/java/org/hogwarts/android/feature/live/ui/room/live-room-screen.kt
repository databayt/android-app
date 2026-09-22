package org.hogwarts.android.feature.live.ui.room

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
 * at the foot. What the phone leaves to the web: raised hands, chat and
 * questions, polls, the whiteboard, slides and the quality menu.
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
    var pending by remember { mutableStateOf<String?>(null) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) when (pending) {
            Manifest.permission.RECORD_AUDIO -> viewModel.setMic(true)
            Manifest.permission.CAMERA -> viewModel.setCamera(true)
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
    val leave = {
        viewModel.leave()
        onClose()
    }
    BackHandler(onBack = leave)

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
                )
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
private fun Chrome(state: RoomUiState, onClose: () -> Unit, onMic: () -> Unit, onCamera: () -> Unit) {
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
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).clip(CircleShape).background(GLASS).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassButton(
                if (state.micOn) LiveIcons.Mic else LiveIcons.MicOff,
                stringResource(if (state.micOn) R.string.live_room_mic else R.string.live_room_mic_muted),
                off = !state.micOn, onClick = onMic,
            )
            GlassButton(
                if (state.cameraOn) LiveIcons.Video else LiveIcons.VideoOff,
                stringResource(if (state.cameraOn) R.string.live_room_camera else R.string.live_room_camera_off),
                off = !state.cameraOn, onClick = onCamera,
            )
            Row(
                Modifier.height(48.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(LiveIcons.Users, stringResource(R.string.live_room_participants), tint = Color.White, modifier = Modifier.size(20.dp))
                Text(state.tiles.size.toString(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GlassButton(icon: ImageVector, label: String, off: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(if (off) Color.White.copy(alpha = 0.9f) else GLASS)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        Alignment.Center,
    ) {
        Icon(icon, label, tint = if (off) Color.Black else Color.White, modifier = Modifier.size(22.dp))
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

