package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

enum class PillVariant { Primary, Muted, Outline, Ghost, BrandWhite, BrandGhost }

/**
 * The kit's button: `h-10 rounded-full px-5 text-sm font-medium`, side by
 * side — never stacked full-width bars.
 */
@Composable
fun PillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillVariant = PillVariant.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = HogwartsTheme.colors
    val (bg, fg) = when (variant) {
        PillVariant.Primary -> colors.primary to colors.primaryForeground
        PillVariant.Muted -> colors.muted to colors.foreground
        PillVariant.Outline -> colors.background to colors.foreground
        PillVariant.Ghost -> Color.Transparent to colors.foreground
        PillVariant.BrandWhite -> Color.White to BrandColors.Ink
        PillVariant.BrandGhost -> Color.Transparent to BrandColors.Ink.copy(alpha = 0.75f)
    }
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(HogwartsShapes.Pill)
            .background(bg)
            .then(if (variant == PillVariant.Outline) Modifier.border(1.dp, colors.border, HogwartsShapes.Pill) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Text(label, style = HogwartsTheme.type.bodyMedium, color = fg, maxLines = 1)
    }
}
