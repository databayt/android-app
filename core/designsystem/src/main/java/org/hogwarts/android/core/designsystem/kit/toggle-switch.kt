package org.hogwarts.android.core.designsystem.kit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * shadcn's `ui/switch.tsx`: a `h-[1.15rem] w-8` pill, primary when on and
 * `bg-input` (`dark:bg-input/80`) when off, with a 16dp thumb in
 * `bg-background` (`dark:bg-foreground`, `dark:data-checked:bg-primary-foreground`).
 */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = HogwartsTheme.colors
    val track = when {
        checked -> colors.primary
        colors.isDark -> colors.input.copy(alpha = colors.input.alpha * 0.8f)
        else -> colors.input
    }
    val thumb = when {
        !colors.isDark -> colors.background
        checked -> colors.primaryForeground
        else -> colors.foreground
    }
    val offset by animateDpAsState(if (checked) 14.dp else 0.dp, label = "switch-thumb")
    Box(
        modifier
            .size(width = 32.dp, height = 18.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(HogwartsShapes.Pill)
            .background(track)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(1.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.offset(x = offset).size(16.dp).clip(HogwartsShapes.Pill).background(thumb))
    }
}
