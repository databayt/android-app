package org.hogwarts.android.core.designsystem.apple

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Apple-inspired shape system with continuous corners.
 *
 * Android's RoundedCornerShape uses circular arcs.
 * True iOS "continuous" (squircle) corners would require a custom Shape.
 * For practical purposes, RoundedCornerShape provides a close match,
 * especially at smaller radii.
 */
object AppleShape {
    val Card: Shape = RoundedCornerShape(26.dp)
    val Button: Shape = RoundedCornerShape(12.dp)
    val TextField: Shape = RoundedCornerShape(12.dp)
    val Chip: Shape = RoundedCornerShape(8.dp)
    val Sheet: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val Avatar: Shape = RoundedCornerShape(50)
    val SmallCard: Shape = RoundedCornerShape(16.dp)
    val ListRow: Shape = RoundedCornerShape(12.dp)
}
