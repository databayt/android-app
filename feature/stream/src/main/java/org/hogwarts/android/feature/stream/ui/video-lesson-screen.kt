package org.hogwarts.android.feature.stream.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import org.hogwarts.android.feature.stream.R
import org.hogwarts.android.feature.stream.ui.components.StreamForward10Icon
import org.hogwarts.android.feature.stream.ui.components.StreamPauseIcon
import org.hogwarts.android.feature.stream.ui.components.StreamPlayIcon
import org.hogwarts.android.feature.stream.ui.components.StreamRewind10Icon
import org.hogwarts.android.feature.stream.ui.components.StreamVideoProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLessonScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: (String, String) -> Unit,
    viewModel: VideoLessonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSpeedMenu by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Lifecycle — release the player exactly once, when the screen leaves composition.
    // The previous implementation keyed this effect on contentUrl, which released
    // the player the moment the lesson loaded: by the time setMediaItem ran, the
    // player was already gone and nothing ever started playing.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.updateResumePosition(exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }

    // Load the media item whenever the URL becomes available.
    LaunchedEffect(uiState.lesson?.contentUrl) {
        val url = uiState.lesson?.contentUrl ?: return@LaunchedEffect
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        if (uiState.resumePositionMs > 0) {
            exoPlayer.seekTo(uiState.resumePositionMs)
        }
    }

    LaunchedEffect(uiState.playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(uiState.playbackSpeed)
    }

    // Player state drives both the overlay visuals and the progress bar.
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.takeIf { it > 0 } ?: 0L
                }
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.onVideoEnded()
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Poll the position while playing so the progress bar tracks smoothly. 250ms
    // matches the web player's timeupdate cadence closely enough for a snappy feel.
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition
            delay(250)
        }
    }

    // Controls auto-hide while playing; any tap toggles them back.
    var showControls by remember { mutableStateOf(true) }
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.stream_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSpeedMenu = true }) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = stringResource(R.string.stream_playback_speed_content_desc)
                        )
                    }
                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false }
                    ) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${speed}x",
                                        color = if (uiState.playbackSpeed == speed) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                },
                                onClick = {
                                    viewModel.setPlaybackSpeed(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            VideoPlayerSurface(
                exoPlayer = exoPlayer,
                lessonTitle = uiState.lesson?.title.orEmpty(),
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                showControls = showControls,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onToggleControls = { showControls = !showControls },
                onPlayPause = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                onRewind = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 10_000).coerceAtLeast(0))
                    showControls = true
                },
                onForward = {
                    val max = exoPlayer.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                    exoPlayer.seekTo((exoPlayer.currentPosition + 10_000).coerceAtMost(max))
                    showControls = true
                },
                onSeek = { fraction ->
                    val target = (fraction * durationMs).toLong()
                    currentPositionMs = target
                    exoPlayer.seekTo(target)
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = uiState.lesson?.title.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.lesson?.duration?.takeIf { it.isNotBlank() }?.let { duration ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = duration,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.stream_speed_label, uiState.playbackSpeed.toString()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (uiState.isCompleted) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.height(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.stream_lesson_completed),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    LessonCompleteButton(
                        isLoading = uiState.isMarking,
                        onClick = { viewModel.markComplete() }
                    )
                }
            }
        }
    }
}

/**
 * Renders the ExoPlayer surface with an Apple-TV-flavored overlay that mirrors
 * the web `components/stream/shared/video-player`:
 *   - Center glass buttons for rewind / play-pause / forward.
 *   - Bottom info label + mono-timestamps + seek bar over a dark gradient.
 *   - Buffering spinner in the center.
 * The system `PlayerView` is rendered with `useController = false` so Media3
 * never paints its own UI on top of ours.
 */
@Composable
private fun VideoPlayerSurface(
    exoPlayer: ExoPlayer,
    lessonTitle: String,
    isPlaying: Boolean,
    isBuffering: Boolean,
    showControls: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onToggleControls: () -> Unit,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleControls
            )
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(GLASS_FILL)
                    .padding(16.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showControls && !isBuffering,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Web parity: `gap-10` (~40dp), 50dp skip buttons, 80dp play/pause.
            // Icon glyphs are the same SVG paths used on the web overlay so the
            // two players read as the same product.
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = StreamRewind10Icon,
                    contentDescription = "Rewind 10 seconds",
                    size = 50.dp,
                    iconSize = 22.dp,
                    onClick = onRewind
                )
                GlassIconButton(
                    icon = if (isPlaying) StreamPauseIcon else StreamPlayIcon,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    size = 80.dp,
                    iconSize = 32.dp,
                    onClick = onPlayPause
                )
                GlassIconButton(
                    icon = StreamForward10Icon,
                    contentDescription = "Forward 10 seconds",
                    size = 50.dp,
                    iconSize = 22.dp,
                    onClick = onForward
                )
            }
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (lessonTitle.isNotBlank()) {
                    Text(
                        text = lessonTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(6.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = formatTime(currentPositionMs),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    StreamVideoProgressBar(
                        progress = if (durationMs > 0) {
                            (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                        } else {
                            0f
                        },
                        onSeek = onSeek,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTime(durationMs),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    iconSize: Dp = size * 0.44f,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(GLASS_FILL)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, mins, secs)
    } else {
        "%d:%02d".format(mins, secs)
    }
}

// Matches the web player's `rgba(20,20,20,0.4)` backdrop-blur fill. Real blur on
// Compose would need a graphics layer per button; the flat tint reads close
// enough over opaque video and costs nothing.
private val GLASS_FILL = Color(0x66141414)

@Composable
private fun LessonCompleteButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.height(20.dp)
            )
        } else {
            Text(
                text = stringResource(R.string.stream_mark_as_complete),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}
