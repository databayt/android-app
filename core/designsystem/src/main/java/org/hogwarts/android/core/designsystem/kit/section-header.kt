package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * A section's title line — mirrors `shared/section-header.tsx`.
 *
 * 18sp semibold, an optional hint under it, and the section's way out ("see
 * all" with a chevron, or one [action]) on the same line at the far end.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    linkLabel: String? = null,
    onLinkClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = type.section,
                color = colors.foreground,
                modifier = Modifier.semantics { heading() },
            )
            if (description != null) {
                Text(
                    text = description,
                    style = type.body,
                    color = colors.mutedForeground,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (linkLabel != null && onLinkClick != null) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onLinkClick)
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = linkLabel, style = type.body, color = colors.mutedForeground)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.mutedForeground,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        action?.invoke()
    }
}
