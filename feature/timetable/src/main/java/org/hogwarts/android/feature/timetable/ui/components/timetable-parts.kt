package org.hogwarts.android.feature.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.TileFace
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.timetable.R
import org.hogwarts.android.feature.timetable.domain.model.Closure
import org.hogwarts.android.feature.timetable.domain.model.DayRow
import org.hogwarts.android.feature.timetable.domain.model.JoinTarget
import org.hogwarts.android.feature.timetable.domain.model.NowKind
import org.hogwarts.android.feature.timetable.domain.model.NowRow
import org.hogwarts.android.feature.timetable.domain.model.isRowJoinable
import org.hogwarts.android.feature.timetable.domain.model.periodNumber

/** The dashboard's `today-classes.kt` tints, in its order, so a subject wears the same tile on both screens. */
private val SUBJECT_TINTS = listOf(TileTint.Blue, TileTint.Green, TileTint.Orange, TileTint.Purple, TileTint.Teal, TileTint.Pink, TileTint.Indigo)

private fun tintFor(subject: String?): TileTint =
    SUBJECT_TINTS[Math.floorMod(subject.orEmpty().hashCode(), SUBJECT_TINTS.size)]

/** "07:15–08:05", held left to right inside Arabic. */
internal fun DayRow.timeRange(): String = ltr("${start.hhmm}–${end.hhmm}")

@Composable
internal fun periodLabel(name: String): String = "${stringResource(R.string.timetable_period)} ${periodNumber(name)}"

/** Opens a Join target: an external room in the browser, a web path through the shell. */
@Composable
internal fun rememberJoin(onOpenHref: (String) -> Unit): (JoinTarget) -> Unit {
    val uri = LocalUriHandler.current
    return { target ->
        when (target) {
            is JoinTarget.Url -> runCatching { uri.openUri(target.url) }
            is JoinTarget.Href -> onOpenHref(target.href)
        }
    }
}

/**
 * `ClosureNotice`: "School is closed today — Eid" above the day. It informs and
 * never blanks: the pattern still renders underneath.
 */
@Composable
fun ClosureNotice(closure: Closure?, modifier: Modifier = Modifier) {
    if (closure == null) return
    val colors = HogwartsTheme.colors
    val dash = colors.mutedForeground.copy(alpha = 0.3f)
    Text(
        text = listOfNotNull(stringResource(R.string.timetable_closed_today), closure.title).joinToString(" — "),
        style = HogwartsTheme.type.body,
        color = colors.mutedForeground,
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Lg)
            .background(colors.muted.copy(alpha = 0.4f))
            .drawBehind {
                val r = 10.dp.toPx()
                drawRoundRect(
                    color = dash,
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .padding(12.dp),
    )
}

/**
 * The day as kit rows — the same shape the dashboard's today section draws:
 * a class is a tile row (subject; section or teacher; period · time · room),
 * a break or free period is one quiet line. A class in its join window carries
 * the Join pill.
 */
@Composable
fun DayRows(
    rows: List<DayRow>,
    teacherView: Boolean,
    nowMinutes: Int,
    onJoin: (JoinTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val breakWord = stringResource(R.string.timetable_break)
    val online = stringResource(R.string.timetable_online)
    val join = stringResource(R.string.timetable_join)
    val labels = rows.map { periodLabel(it.periodName) }
    ListRows(
        modifier = modifier,
        rows = rows.mapIndexed { index, row ->
            {
                if (!row.isClass) {
                    Text(
                        listOf(if (row.isBreak) breakWord else labels[index], row.timeRange()).joinToString(" · "),
                        style = type.caption,
                        color = colors.mutedForeground,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                } else {
                    val target = row.liveClass?.joinTarget?.takeIf { isRowJoinable(row.start, row.end, nowMinutes) }
                    ListRow(
                        title = row.subject ?: labels[index],
                        art = { TileFace(tint = tintFor(row.subject)) },
                        badge = if (row.liveClass?.isOnlineToday == true) {
                            { OnlineMark(online) }
                        } else {
                            null
                        },
                        description = if (teacherView) row.className else row.teacher,
                        // First-strong isolate: a room like "ب10" keeps its own order inside the line.
                        meta = listOfNotNull(labels[index], row.timeRange(), row.room?.let { "\u2068$it\u2069" }).joinToString(" · "),
                        trailing = target?.let { { PillButton(join, onClick = { onJoin(it) }, icon = Icons.Outlined.Videocam) } },
                    )
                }
            }
        },
    )
}

/** `OnlineBadge`: a camera and the word, beside the room — never instead of it. */
@Composable
private fun OnlineMark(label: String) {
    val muted = HogwartsTheme.colors.mutedForeground
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Icons.Outlined.Videocam, contentDescription = null, tint = muted, modifier = Modifier.size(12.dp))
        Text(label, style = HogwartsTheme.type.caption, color = muted, maxLines = 1)
    }
}

/**
 * The Current / Next card over the teacher's and guardian's day: green while
 * the class runs, blue before it starts; the subject, who or where, and its
 * times at the far end.
 */
@Suppress("DEPRECATION") // Icons.Outlined.ShowChart: the web's rising line does not turn in Arabic.
@Composable
fun NowCard(
    now: NowRow,
    label: String,
    teacherView: Boolean,
    nowMinutes: Int,
    onJoin: (JoinTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val accent = if (now.kind == NowKind.Current) BrandColors.Live else colors.info
    val row = now.row
    // `isLiveJoinable`: the current class always, the next within ten minutes of its bell.
    val joinable = now.kind == NowKind.Current || (row.start.minutes - nowMinutes) in 0..10
    val target = row.liveClass?.joinTarget?.takeIf { joinable }
    val where = if (teacherView) {
        listOfNotNull(row.className, row.room?.let { "\u2068$it\u2069" }).joinToString(" · ")
    } else {
        listOfNotNull(row.teacher, row.room?.let { "\u2068$it\u2069" }).joinToString(" • ")
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(accent.copy(alpha = if (colors.isDark) 0.12f else 0.08f))
            .border(2.dp, accent, HogwartsShapes.Card)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(48.dp).clip(HogwartsShapes.Pill).background(accent), contentAlignment = Alignment.Center) {
            // The web's teacher card shows a rising line (never mirrored); the guardian's a chevron that turns in Arabic.
            val icon = if (teacherView) Icons.Outlined.ShowChart else Icons.AutoMirrored.Filled.KeyboardArrowRight
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = type.body, color = colors.mutedForeground)
            Text(row.subject ?: periodLabel(row.periodName), style = type.section, color = colors.foreground)
            if (where.isNotEmpty() || row.liveClass?.isOnlineToday == true) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (where.isNotEmpty()) Text(where, style = type.body, color = colors.mutedForeground, modifier = Modifier.weight(1f, fill = false))
                    if (row.liveClass?.isOnlineToday == true) OnlineMark(stringResource(R.string.timetable_online))
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(ltr(row.start.hhmm), style = type.figure.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = colors.foreground)
            Text(ltr("- ${row.end.hhmm}"), style = type.body, color = colors.mutedForeground)
        }
        if (target != null) {
            PillButton(stringResource(R.string.timetable_join), onClick = { onJoin(target) }, icon = Icons.Outlined.Videocam)
        }
    }
}

/** The day's empty card: a calendar and one sentence. */
@Composable
fun EmptyDay(message: String, modifier: Modifier = Modifier) {
    val colors = HogwartsTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .padding(vertical = 48.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = colors.mutedForeground.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
        Text(message, style = HogwartsTheme.type.body, color = colors.mutedForeground, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
    }
}

/** Grey placeholder blocks while the first load runs. */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(modifier.clip(HogwartsShapes.Md).background(HogwartsTheme.colors.muted))
}

@Composable
fun GridSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SkeletonBlock(Modifier.fillMaxWidth().height(44.dp))
        repeat(5) { SkeletonBlock(Modifier.fillMaxWidth().height(72.dp)) }
    }
}
