package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * One record in a listing's grid view — mirrors `shared/item-card.tsx`:
 * grey card, 14dp corners, no border, at least 128dp tall, floor (badges,
 * meta) pushed to the bottom so a row of cards lines up at the chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemCard(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    value: String? = null,
    badges: (@Composable () -> Unit)? = null,
    meta: String? = null,
    art: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Box(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .heightIn(min = 128.dp)
                .clip(HogwartsShapes.Card)
                .background(colors.muted)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (art != null) Box(Modifier.width(44.dp).padding(bottom = 4.dp)) { art() }
            val endPad = if (actions != null) Modifier.padding(end = 28.dp) else Modifier
            if (eyebrow != null) {
                Text(eyebrow, style = type.caption, color = colors.mutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = endPad)
            }
            Text(
                title,
                style = type.cardTitle,
                color = colors.foreground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = if (eyebrow == null) endPad else Modifier,
            )
            if (value != null) Text(value, style = type.value, color = colors.foreground)
            if (badges != null || meta != null) {
                Spacer(Modifier.weight(1f))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    if (badges != null) FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { badges() }
                    if (meta != null) Text(meta, style = type.caption, color = colors.mutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (actions != null) {
            Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { actions() }
        }
    }
}

/** Two cards across, rows sharing a height so floors align — `ItemGrid` on a phone. */
@Composable
fun ItemGrid(
    count: Int,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    item: @Composable (index: Int, modifier: Modifier) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        (0 until count).chunked(columns).forEach { indices ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                indices.forEach { item(it, Modifier.weight(1f).fillMaxHeight()) }
                repeat(columns - indices.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** The grid's load-more pill. */
@Composable
fun ItemGridMore(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, loading: Boolean = false, loadingLabel: String? = null) {
    Box(modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
        PillButton(
            label = if (loading) loadingLabel ?: label else label,
            onClick = onClick,
            enabled = !loading,
            variant = PillVariant.Muted,
        )
    }
}
