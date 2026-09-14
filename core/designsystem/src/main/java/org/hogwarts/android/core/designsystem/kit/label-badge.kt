package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

enum class BadgeVariant { Default, Secondary, Destructive, Outline }

/**
 * A short status word — mirrors `ui/badge.tsx`: `rounded-md border px-2 py-0.5
 * text-xs font-medium`. [tint] recolours an outline badge's border and ink
 * (the web's `border-amber-500 text-amber-600` for "late").
 */
@Composable
fun LabelBadge(
    label: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
    tint: Color? = null,
) {
    val colors = HogwartsTheme.colors
    val (bg, fg) = when (variant) {
        BadgeVariant.Default -> colors.primary to colors.primaryForeground
        BadgeVariant.Secondary -> colors.muted to colors.foreground
        BadgeVariant.Destructive -> colors.destructive to Color.White
        BadgeVariant.Outline -> Color.Transparent to (tint ?: colors.foreground)
    }
    val border = if (variant == BadgeVariant.Outline) tint ?: colors.border else Color.Transparent
    Text(
        text = label,
        style = HogwartsTheme.type.caption.copy(fontWeight = FontWeight.Medium),
        color = fg,
        maxLines = 1,
        modifier = modifier
            .clip(HogwartsShapes.Md)
            .background(bg)
            .border(1.dp, border, HogwartsShapes.Md)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
