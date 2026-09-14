package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * One choice in a sideways shelf — the quick-attendance section chip:
 * `rounded-full border px-3.5 py-2 text-sm`, selected = primary ground with
 * primary-foreground ink. [leading] and [trailing] read [LocalContentColor]
 * so a dot, a time or a check follows the chip's ink.
 */
@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    /** Long names truncate here (`max-w-36`); unbounded when null. */
    labelMaxWidth: Dp? = null,
) {
    val colors = HogwartsTheme.colors
    val bg = if (selected) colors.primary else colors.background
    val fg = if (selected) colors.primaryForeground else colors.foreground
    val border = if (selected) colors.primary else colors.border
    Row(
        modifier = modifier
            .clip(HogwartsShapes.Pill)
            .background(bg)
            .border(1.dp, border, HogwartsShapes.Pill)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides fg) {
            leading?.invoke()
            Text(
                label,
                style = HogwartsTheme.type.body,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // No weight: the chip sits in a sideways-scrolling row with unbounded width.
                modifier = if (labelMaxWidth != null) Modifier.widthIn(max = labelMaxWidth) else Modifier,
            )
            trailing?.invoke(this)
        }
    }
}

/** Chips in one row that scrolls sideways, 8dp apart, no scrollbar. */
@Composable
fun ChoiceChipRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
