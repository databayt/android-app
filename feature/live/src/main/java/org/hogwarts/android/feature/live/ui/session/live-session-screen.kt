package org.hogwarts.android.feature.live.ui.session

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.hogwarts.android.core.designsystem.icon.LucideIcons
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.live.data.remote.dto.LinkDto
import org.hogwarts.android.feature.live.data.remote.dto.SessionPageDto
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * `/live/[id]` (`live/detail.tsx`): the title and its times with the status
 * badge, the student's one-line state with its one action, the description,
 * the facts, the attendance note, the page's buttons and the lesson's
 * references. Every word comes from the server's dictionary.
 */
@Composable
fun LiveSessionScreen(
    onOpenHref: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    viewModel: LiveSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = HogwartsTheme.colors
    Box(Modifier.fillMaxSize().background(c.background)) {
        when (val s = state) {
            LoadState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is LoadState.Failed -> Text(s.message.orEmpty(), color = c.mutedForeground, modifier = Modifier.align(Alignment.Center).padding(24.dp))
            is LoadState.Ready -> SessionPage(s.value, onOpenHref, onOpenUrl)
        }
    }
}

@Composable
private fun SessionPage(p: SessionPageDto, onOpenHref: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    val c = HogwartsTheme.colors
    val l = p.labels
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(p.title, color = c.foreground, fontSize = 30.sp, lineHeight = 1.2.em, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.025).em)
                Text(p.`when`, color = c.mutedForeground, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Text(
                p.statusLabel, color = c.primaryForeground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(c.primary).padding(horizontal = 10.dp, vertical = 2.dp),
            )
        }

        SessionState(p, onOpenHref, onOpenUrl)

        p.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = c.foreground, fontSize = 16.sp, lineHeight = 1.75.em)
        }

        // The facts, two to a row.
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            p.rows.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    pair.forEach { row ->
                        Column(Modifier.weight(1f)) {
                            Text(row.label, color = c.mutedForeground, fontSize = 14.sp)
                            Text(row.value, color = c.foreground, fontSize = 14.sp)
                        }
                    }
                    if (pair.size == 1) Box(Modifier.weight(1f))
                }
            }
        }

        p.attendanceNote?.let { note ->
            Column(
                Modifier.fillMaxWidth().border(1.dp, c.border, RoundedCornerShape(8.dp)).padding(16.dp),
            ) {
                Text(note, color = c.mutedForeground, fontSize = 14.sp)
                p.attendanceLink?.let { link ->
                    Text(
                        link, color = c.mutedForeground, fontSize = 14.sp, textDecoration = TextDecoration.Underline,
                        modifier = Modifier.padding(top = 8.dp).clickable { onOpenHref("/attendance/manual") },
                    )
                }
            }
        }

        // The page's own buttons: Join for the class's host, View recordings once it has ended.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (p.canJoin && p.canEnd) {
                if (p.isExternal) {
                    p.meetingUrl?.let { url -> SolidButton(l["open_meeting"].orEmpty(), null) { onOpenUrl(url) } }
                } else {
                    SolidButton(l["join"].orEmpty(), null) { onOpenHref("/live/${p.id}/room") }
                }
            }
            if (p.status == "ended") {
                OutlineButton(l["view_recordings"].orEmpty(), null) { onOpenHref("/live/${p.id}/recordings") }
            }
        }

        p.references?.let { r ->
            Column(
                Modifier.fillMaxWidth().border(1.dp, c.border, RoundedCornerShape(8.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(r.title, color = c.foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                r.lesson?.let { lesson ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(r.lessonLabel, color = c.mutedForeground, fontSize = 14.sp)
                        Text(
                            lesson, color = c.foreground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false).border(1.dp, c.border, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 2.dp),
                        )
                    }
                    r.practice?.let { Text(it, color = c.mutedForeground, fontSize = 12.sp) }
                }
                RefList(r.videosTitle, r.videos, onOpenHref, onOpenUrl)
                RefList(r.materialsTitle, r.materials, onOpenHref, onOpenUrl)
                RefList(r.examsTitle, r.exams, onOpenHref, onOpenUrl)
                RefList(r.assignmentsTitle, r.assignments, onOpenHref, onOpenUrl)
                RefList(r.linksTitle, r.links, onOpenHref, onOpenUrl)
            }
        }
    }
}

@Composable
private fun RefList(title: String, items: List<LinkDto>, onOpenHref: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    if (items.isEmpty()) return
    val c = HogwartsTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title.uppercase(), color = c.mutedForeground, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val target: (() -> Unit)? = item.path?.let { { onOpenHref(it) } } ?: item.url?.let { { onOpenUrl(it) } }
                Text(
                    item.title, color = c.foreground, fontSize = 14.sp,
                    textDecoration = if (target != null) TextDecoration.Underline else null,
                    modifier = if (target != null) Modifier.clickable(onClick = target) else Modifier,
                )
                item.badge?.let {
                    Text(it, color = c.foreground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(c.muted).padding(horizontal = 8.dp, vertical = 2.dp))
                }
                item.date?.let { Text(it, color = c.mutedForeground, fontSize = 12.sp) }
            }
        }
    }
}

/**
 * `session-state.tsx`: the one line a student needs — upcoming with its
 * countdown (and Enter within ten minutes), live with the big Enter, ended
 * with where the recording stands.
 */
@Composable
private fun SessionState(p: SessionPageDto, onOpenHref: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    val c = HogwartsTheme.colors
    val l = p.labels
    val join: (() -> Unit)? = when {
        !p.canJoin -> null
        p.isExternal -> p.meetingUrl?.let { url -> { onOpenUrl(url) } }
        else -> ({ onOpenHref("/live/${p.id}/room") })
    }
    when (p.status) {
        "cancelled", "failed" -> Banner(Tone.Muted, LucideIcons.Clock) {
            Text(l["cancelled"].orEmpty(), color = c.foreground, fontWeight = FontWeight.Medium)
        }
        "scheduled" -> {
            var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(Unit) { while (true) { delay(30_000); now = System.currentTimeMillis() } }
            val start = remember(p.scheduledStart) { Instant.parse(p.scheduledStart).toEpochMilli() }
            val diffMin = ((start - now) / 60_000.0).roundToInt().coerceAtLeast(0)
            val countdown = if (diffMin >= 120) l["hours"].orEmpty().replace("{n}", (diffMin / 60.0).roundToInt().toString())
            else l["minutes"].orEmpty().replace("{n}", diffMin.toString())
            val soon = diffMin <= 10
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(start))
            Banner(if (soon) Tone.Primary else Tone.Muted, LucideIcons.Clock) {
                Text(l["upcoming"].orEmpty(), color = c.foreground, fontWeight = FontWeight.Medium)
                Text(
                    l["starts_at"].orEmpty().replace("{time}", time) +
                        if (start > now) " · " + l["starts_in"].orEmpty().replace("{value}", countdown) else "",
                    color = c.mutedForeground, fontSize = 14.sp,
                )
                if (soon && join != null) SolidButton(l["enter"].orEmpty(), LucideIcons.Video, onClick = join)
            }
        }
        "live" -> Banner(Tone.Live, LucideIcons.Radio) {
            Text(l["live"].orEmpty(), color = c.foreground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (join != null) SolidButton(l["enter"].orEmpty(), LucideIcons.Video, height = 40.dp, onClick = join)
        }
        else -> Banner(Tone.Muted, LiveIcons.Film) {
            Text(l["ended"].orEmpty(), color = c.foreground, fontWeight = FontWeight.Medium)
            val line = when (p.recording) {
                "processing" -> l["processing"]
                "ready" -> l["ready"]
                "failed" -> l["failed"]
                else -> l["no_recording"]
            }.orEmpty()
            Text(line, color = if (p.recording == "ready") c.foreground else c.mutedForeground, fontSize = 14.sp)
            if (p.recording == "ready") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.lessonPath?.let { path -> SolidButton(l["open_lesson"].orEmpty(), LucideIcons.Video) { onOpenHref(path) } }
                    OutlineButton(l["watch_recording"].orEmpty(), LucideIcons.Video) { onOpenHref("/live/${p.id}/recordings") }
                }
            }
        }
    }
}

private enum class Tone { Muted, Primary, Live }

@Composable
private fun Banner(tone: Tone, icon: ImageVector, content: @Composable () -> Unit) {
    val c = HogwartsTheme.colors
    val (border, bg) = when (tone) {
        Tone.Live -> c.destructive.copy(alpha = 0.4f) to c.destructive.copy(alpha = 0.05f)
        Tone.Primary -> c.primary.copy(alpha = 0.4f) to c.primary.copy(alpha = 0.05f)
        Tone.Muted -> c.border to c.muted.copy(alpha = 0.4f)
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(bg).border(1.dp, border, RoundedCornerShape(8.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = c.foreground, modifier = Modifier.padding(top = 2.dp).size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun SolidButton(label: String, icon: ImageVector?, height: Dp = 32.dp, onClick: () -> Unit) {
    val c = HogwartsTheme.colors
    Row(
        Modifier.height(height).clip(RoundedCornerShape(8.dp)).background(c.primary)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = if (height > 32.dp) 24.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = c.primaryForeground, modifier = Modifier.padding(end = 8.dp).size(16.dp))
        Text(label, color = c.primaryForeground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OutlineButton(label: String, icon: ImageVector?, onClick: () -> Unit) {
    val c = HogwartsTheme.colors
    Row(
        Modifier.height(32.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, c.border, RoundedCornerShape(8.dp)).background(c.background)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = c.foreground, modifier = Modifier.padding(end = 8.dp).size(16.dp))
        Text(label, color = c.foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

