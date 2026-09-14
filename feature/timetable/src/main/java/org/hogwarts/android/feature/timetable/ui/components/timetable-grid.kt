package org.hogwarts.android.feature.timetable.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.zIndex
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.designsystem.theme.TimetablePalette
import org.hogwarts.android.feature.timetable.R
import org.hogwarts.android.feature.timetable.domain.model.GridMode
import org.hogwarts.android.feature.timetable.domain.model.LiveStatus
import org.hogwarts.android.feature.timetable.domain.model.Period
import org.hogwarts.android.feature.timetable.domain.model.Slot
import org.hogwarts.android.feature.timetable.domain.model.WeekTimetable
import org.hogwarts.android.feature.timetable.domain.model.activePeriodKey
import org.hogwarts.android.feature.timetable.domain.model.breaksBeforePeriod
import org.hogwarts.android.feature.timetable.domain.model.cellText
import org.hogwarts.android.feature.timetable.domain.model.liveStatus
import org.hogwarts.android.feature.timetable.domain.model.periodNumber
import org.hogwarts.android.feature.timetable.domain.model.subjectColorIndex

/** `MIN_COL_PX`: a column never narrower than this; the grid scrolls sideways instead. */
private val MIN_COLUMN = 128.dp

/** LTR isolate for a time or range inside Arabic text. */
internal fun ltr(text: String) = "\u2066$text\u2069"

/**
 * The week (or one day) as the web's `SimpleGrid`: a pinned period column, one
 * column per day, break rows spanning the days in their real time slot, each
 * class in its subject colour, and a single live lamp on today's current-or-next
 * online class. Read-only — a tap opens the class's details.
 *
 * Columns follow the layout direction, so Arabic reads Sunday from the right
 * with the period column on the right, as the web's `start-0` pins it.
 */
@Composable
fun TimetableGrid(
    week: WeekTimetable,
    days: List<Int>,
    mode: GridMode,
    highlightToday: Boolean,
    nowMinutes: Int,
    modifier: Modifier = Modifier,
    onInspect: ((Slot) -> Unit)? = null,
) {
    val palette = TimetablePalette.of(HogwartsTheme.colors.isDark)
    val dayNames = stringArrayResource(R.array.timetable_day_names)
    val teaching = remember(week.periods) { week.periods.filter { !it.isBreak } }
    val breaks = remember(week.periods) { breaksBeforePeriod(week.periods) }
    val activeKey = activePeriodKey(teaching, nowMinutes)
    val highlightDay = if (highlightToday) week.today else -1

    Box(
        modifier
            .fillMaxWidth()
            .shadow(8.dp, HogwartsShapes.Card, clip = false)
            .clip(HogwartsShapes.Card)
            .border(1.dp, palette.line, HogwartsShapes.Card)
            .background(palette.surface),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = days.size + 1
            val column = max(MIN_COLUMN, maxWidth / columns)
            val scroll = rememberScrollState()
            Column(
                Modifier
                    .horizontalScroll(scroll)
                    .width(column * columns),
            ) {
                // Header: the clock over the period column, then the day names.
                Row(Modifier.height(IntrinsicSize.Min).background(palette.header)) {
                    PinnedCell(scroll, column, palette.header, palette) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = palette.mutedInk, modifier = Modifier.size(16.dp))
                    }
                    days.forEachIndexed { index, day ->
                        val isToday = day == highlightDay
                        Box(
                            Modifier
                                .width(column)
                                .fillMaxHeight()
                                .then(if (isToday) Modifier.background(HogwartsTheme.colors.primary.copy(alpha = 0.05f)) else Modifier)
                                .endLine(index < days.lastIndex, palette.line)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                dayNames.getOrElse(day) { "" },
                                style = HogwartsTheme.type.body.copy(
                                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium,
                                ),
                                color = if (isToday) HogwartsTheme.colors.foreground else palette.labelInk,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Line(palette.line)

                teaching.forEachIndexed { index, period ->
                    if (index > 0) Line(palette.line)
                    breaks[period.key]?.forEach { br -> BreakRow(br, days.size, column, scroll, palette) }
                    PeriodRow(
                        period = period,
                        days = days,
                        column = column,
                        scroll = scroll,
                        palette = palette,
                        week = week,
                        mode = mode,
                        highlightDay = highlightDay,
                        isActive = period.key == activeKey,
                        nowMinutes = nowMinutes,
                        onInspect = onInspect,
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodRow(
    period: Period,
    days: List<Int>,
    column: Dp,
    scroll: ScrollState,
    palette: TimetablePalette,
    week: WeekTimetable,
    mode: GridMode,
    highlightDay: Int,
    isActive: Boolean,
    nowMinutes: Int,
    onInspect: ((Slot) -> Unit)?,
) {
    val type = HogwartsTheme.type
    val periodWord = stringResource(R.string.timetable_period)
    Row(Modifier.height(IntrinsicSize.Min)) {
        PinnedCell(scroll, column, palette.periodCell, palette) {
            Text(
                "$periodWord ${periodNumber(period.name)}",
                style = type.body.copy(fontWeight = FontWeight.Medium),
                color = palette.labelInk,
                textAlign = TextAlign.Center,
            )
            Text(ltr("(${period.start.hhmm})"), style = type.caption, color = palette.mutedInk, modifier = Modifier.padding(top = 4.dp))
        }
        days.forEachIndexed { index, day ->
            val slot = week.slotAt(day, period.key)
            val text = slot?.let { cellText(it, mode) }
            val ground = when {
                slot != null && text!!.primary.isNotEmpty() -> palette.subjects[subjectColorIndex(text.primary, palette.subjects.size)]
                day == highlightDay -> HogwartsTheme.colors.primary.copy(alpha = 0.05f)
                else -> palette.emptyCell
            }
            // One lamp for the whole grid: today's class in progress, or the next to start, when it is online.
            val status = if (slot != null && day == week.today && isActive && slot.liveClass?.isOnlineToday == true) {
                liveStatus(period.start, period.end, nowMinutes)
            } else {
                null
            }
            val liveLabel = when (status) {
                LiveStatus.Live -> stringResource(R.string.timetable_live_now)
                LiveStatus.Upcoming, LiveStatus.Missed -> stringResource(R.string.timetable_scheduled_today)
                null -> null
            }
            Box(
                Modifier
                    .width(column)
                    .fillMaxHeight()
                    .heightIn(min = 72.dp)
                    .background(ground)
                    .endLine(index < days.lastIndex, palette.line)
                    .then(if (slot != null && onInspect != null) Modifier.clickable { onInspect(slot) } else Modifier)
                    .then(if (liveLabel != null) Modifier.semantics { contentDescription = liveLabel } else Modifier),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (slot != null && text != null) {
                        Text(
                            text.primary,
                            style = type.caption.copy(fontWeight = FontWeight.Medium),
                            color = palette.cellInk,
                            textAlign = TextAlign.Center,
                        )
                        if (text.secondary.isNotEmpty()) {
                            Text(
                                text.secondary,
                                style = type.caption,
                                color = palette.secondaryInk,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    } else {
                        Text("-", style = type.body, color = palette.dashInk)
                    }
                }
                if (status != null) {
                    LiveMark(
                        color = when (status) {
                            LiveStatus.Live -> BrandColors.Live
                            LiveStatus.Upcoming -> BrandColors.Upcoming
                            LiveStatus.Missed -> BrandColors.Missed
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakRow(
    period: Period,
    dayCount: Int,
    column: Dp,
    scroll: ScrollState,
    palette: TimetablePalette,
) {
    val type = HogwartsTheme.type
    Row(Modifier.height(IntrinsicSize.Min)) {
        PinnedCell(scroll, column, palette.periodCell, palette) {
            Text(stringResource(R.string.timetable_break), style = type.bodyMedium, color = palette.labelInk, textAlign = TextAlign.Center)
            Text(ltr("(${period.start.hhmm})"), style = type.caption, color = palette.mutedInk, modifier = Modifier.padding(top = 4.dp))
        }
        Box(
            Modifier
                .width(column * dayCount)
                .fillMaxHeight()
                .background(palette.breakSpan)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                ltr("${period.start.hhmm} - ${period.end.hhmm}"),
                style = type.bodyMedium,
                color = palette.mutedInk,
            )
        }
    }
}

/**
 * The period column's cell, pinned while the days scroll under it (`sticky
 * start-0`). It is moved back by the scroll offset — direction-aware, so it
 * holds the right edge in Arabic — and painted opaque above the day cells.
 */
@Composable
private fun PinnedCell(
    scroll: ScrollState,
    width: Dp,
    ground: Color,
    palette: TimetablePalette,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .zIndex(1f)
            .offset { IntOffset(scroll.value, 0) }
            .width(width)
            .fillMaxHeight()
            .background(palette.surface)
            .background(ground)
            .endLine(true, palette.line)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}

@Composable
private fun Line(color: Color) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(color))
}

/** `border-e`: a 1dp rule on the end edge, whichever side that is. */
@Composable
private fun Modifier.endLine(show: Boolean, color: Color): Modifier {
    if (!show) return this
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return drawWithContent {
        drawContent()
        val x = if (rtl) 0.5f else size.width - 0.5f
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
    }
}

/** `live-mark.tsx`: a ring around a filled centre, in the lamp's colour. */
@Composable
private fun LiveMark(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension / 24f
        drawCircle(color, radius = 10.25f * unit, style = Stroke(width = 1.5f * unit))
        drawCircle(color, radius = 2f * unit)
    }
}
