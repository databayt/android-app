package org.hogwarts.android.feature.live.ui.session

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.live.R
import org.hogwarts.android.feature.live.domain.model.LiveRecording
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * `/live/[id]/recordings` (`live/recordings.tsx`): each recording's date and
 * status, its length, and — when ready — a Play button that mints a signed
 * URL and swaps itself for the player. A failed load drops back to Play with
 * the error, as the web's player does.
 */
@Composable
fun LiveRecordingsScreen(viewModel: LiveRecordingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val urls by viewModel.urls.collectAsStateWithLifecycle()
    val failed by viewModel.failed.collectAsStateWithLifecycle()
    val c = HogwartsTheme.colors
    Box(Modifier.fillMaxSize().background(c.background)) {
        when (val s = state) {
            LoadState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is LoadState.Failed -> Text(s.message.orEmpty(), color = c.mutedForeground, modifier = Modifier.align(Alignment.Center))
            is LoadState.Ready -> LazyColumn(
                Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(stringResource(R.string.live_rec_title), color = c.foreground, fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp))
                }
                if (s.value.isEmpty()) {
                    item { Text(stringResource(R.string.live_rec_empty), color = c.mutedForeground, fontSize = 14.sp) }
                }
                items(s.value, key = { it.id }) { rec ->
                    RecordingRow(
                        rec,
                        url = urls[rec.id],
                        loading = urls.containsKey(rec.id) && urls[rec.id] == null,
                        failed = rec.id in failed,
                        onPlay = { viewModel.play(rec.id) },
                        onError = { viewModel.playbackFailed(rec.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingRow(
    rec: LiveRecording,
    url: String?,
    loading: Boolean,
    failed: Boolean,
    onPlay: () -> Unit,
    onError: () -> Unit,
) {
    val c = HogwartsTheme.colors
    val status = stringResource(
        when (rec.status) {
            "ready" -> R.string.live_rec_ready
            "processing" -> R.string.live_rec_processing
            "failed" -> R.string.live_rec_failed
            "expired" -> R.string.live_rec_expired
            else -> R.string.live_rec_pending
        },
    )
    Column(
        Modifier.fillMaxWidth().border(1.dp, c.border, RoundedCornerShape(6.dp)).padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                rec.completedAt?.atZone(ZoneId.systemDefault())
                    ?.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault())) ?: "—",
                color = c.foreground, fontSize = 14.sp, modifier = Modifier.weight(1f),
            )
            val ready = rec.status == "ready"
            Text(
                status, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = if (ready) c.primaryForeground else c.foreground,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (ready) c.primary else c.muted)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            )
        }
        Text(duration(rec.durationSeconds), color = c.mutedForeground, fontSize = 14.sp)
        if (rec.isPlayable) {
            Box(Modifier.padding(top = 12.dp)) {
                if (url != null) {
                    Player(url, onError)
                } else {
                    val label = (if (loading) stringResource(R.string.live_rec_loading) else stringResource(R.string.live_rec_play)) +
                        if (failed) " · " + stringResource(R.string.live_err_recording_failed) else ""
                    Text(
                        label, color = c.primaryForeground, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.height(32.dp).clip(RoundedCornerShape(8.dp)).background(c.primary)
                            .clickable(enabled = !loading, role = Role.Button, onClick = onPlay)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

private fun duration(s: Int?): String = if (s == null || s == 0) "—" else "${s / 60}:${(s % 60).toString().padStart(2, '0')}"

@OptIn(UnstableApi::class)
@Composable
private fun Player(url: String, onError: () -> Unit) {
    val context = LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) = onError()
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    AndroidView(
        factory = { PlayerView(it).apply { this.player = player } },
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp)).background(Color.Black),
    )
}
