package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/** Tone is carried by the figure, never by the cell. */
enum class StatTone { Default, Positive, Negative, Warning, Info }

@Immutable
data class StatItem(
    val key: String,
    val label: String,
    val value: String,
    /** One short line under the figure — a unit, a share, "of 417 days". */
    val hint: String? = null,
    val tone: StatTone = StatTone.Default,
    val onClick: (() -> Unit)? = null,
    /** Take the whole row — the headline figure the others break down. */
    val wide: Boolean = false,
    /** A block under the hint, e.g. a progress bar. */
    val extra: (@Composable () -> Unit)? = null,
)

internal data class PlacedStat(val item: StatItem, val row: Int, val col: Int, val span: Int)

/**
 * Lay items out up front so each cell knows whether it has a neighbour above
 * (top hairline) or before it (start hairline). A wide item starts a fresh
 * row; the last item stretches over what is left of its row.
 * Ported from `place()` in `shared/stat-panel.tsx`.
 */
internal fun placeStats(items: List<StatItem>, columns: Int): List<PlacedStat> {
    val placed = mutableListOf<PlacedStat>()
    var cursor = 0
    items.forEachIndexed { index, item ->
        if (item.wide && cursor % columns != 0) {
            val gap = columns - (cursor % columns)
            placed.lastOrNull()?.let { placed[placed.lastIndex] = it.copy(span = it.span + gap) }
            cursor += gap
        }
        val col = cursor % columns
        var span = if (item.wide) columns else 1
        if (index == items.lastIndex && col + span < columns) span = columns - col
        placed += PlacedStat(item, cursor / columns, col, span)
        cursor += span
    }
    return placed
}

/**
 * Several figures as ONE grey panel split by hairlines — mirrors
 * `shared/stat-panel.tsx`. Label small and muted above a bold figure.
 */
@Composable
fun StatPanel(
    items: List<StatItem>,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    columns: Int = 2,
    content: (@Composable () -> Unit)? = null,
) {
    if (items.isEmpty() && content == null) return
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val rows = remember(items, columns) { placeStats(items, columns).groupBy { it.row }.values.toList() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(colors.muted),
    ) {
        if (title != null) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(title, style = type.rowTitle, color = colors.foreground)
                if (description != null) {
                    Text(description, style = type.body, color = colors.mutedForeground)
                }
            }
            Hairline(colors.border)
        }
        rows.forEachIndexed { rowIndex, cells ->
            if (rowIndex > 0) Hairline(colors.border)
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                cells.forEach { cell ->
                    if (cell.col > 0) {
                        Box(Modifier.width(1.dp).fillMaxHeight().background(colors.border))
                    }
                    StatCell(cell, colors, Modifier.weight(cell.span.toFloat()))
                }
            }
        }
        content?.invoke()
    }
}

@Composable
private fun StatCell(cell: PlacedStat, colors: HogwartsColors, modifier: Modifier) {
    val type = HogwartsTheme.type
    val item = cell.item
    val tone = when (item.tone) {
        StatTone.Default -> colors.foreground
        StatTone.Positive -> colors.positive
        StatTone.Negative -> colors.destructive
        StatTone.Warning -> colors.warning
        StatTone.Info -> colors.info
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .then(if (item.onClick != null) Modifier.clickable(onClick = item.onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(item.label, style = type.caption, color = colors.mutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.value, style = if (item.wide) type.figureWide else type.figure, color = tone)
        if (item.hint != null) {
            Text(item.hint, style = type.caption, color = colors.mutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        item.extra?.let { Box(Modifier.padding(top = 4.dp)) { it() } }
    }
}

@Composable
internal fun Hairline(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}
