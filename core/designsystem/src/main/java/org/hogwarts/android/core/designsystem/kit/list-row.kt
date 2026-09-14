package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * One record as the /live article row — mirrors `shared/list-row.tsx`:
 * art square, then title (with its badge), description and meta, then the
 * figure or a chevron at the far end. The copy column is the one that flexes.
 */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    art: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    description: String? = null,
    meta: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    chevron: Boolean = onClick != null && trailing == null,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (art != null) Box(Modifier.width(56.dp)) { art() }
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    title,
                    style = type.rowTitle,
                    color = colors.foreground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                badge?.invoke()
            }
            if (description != null) {
                Text(
                    description,
                    style = type.body,
                    color = colors.mutedForeground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (meta != null) {
                Text(
                    meta,
                    style = type.caption,
                    color = colors.mutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        trailing?.invoke()
        if (chevron) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.mutedForeground.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Rows under one another. Rows with art need nothing between them; rows
 * without art take a hairline so a column of text doesn't run together.
 */
@Composable
fun ListRows(
    modifier: Modifier = Modifier,
    divided: Boolean = false,
    rows: List<@Composable () -> Unit>,
) {
    val border = HogwartsTheme.colors.border
    Column(modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            if (divided && index > 0) Hairline(border)
            Box(if (divided) Modifier.padding(vertical = 2.dp) else Modifier) { row() }
        }
    }
}
