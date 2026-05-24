package org.hogwarts.android.core.designsystem.apple

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple-inspired elevation system with 4 levels.
 * Mirrors iOS `ElevationLevel` from swift-app design system.
 */
enum class AppleElevation(
    val shadowColor: Color,
    val radius: Dp,
    val offsetY: Dp
) {
    Flat(Color.Transparent, 0.dp, 0.dp),
    Low(Color(0x0D000000), 4.dp, 2.dp),       // 5% black
    Medium(Color(0x14000000), 12.dp, 4.dp),    // 8% black
    High(Color(0x1F000000), 20.dp, 8.dp)       // 12% black
}

fun Modifier.elevation(
    level: AppleElevation,
    shape: Shape = AppleShape.Card
): Modifier = if (level == AppleElevation.Flat) {
    this
} else {
    shadow(
        elevation = level.radius,
        shape = shape,
        ambientColor = level.shadowColor,
        spotColor = level.shadowColor
    )
}
