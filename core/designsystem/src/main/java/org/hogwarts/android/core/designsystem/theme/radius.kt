package org.hogwarts.android.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Corner radii mirrored from the web: `--radius: 0.625rem` (10px) and the
 * Tailwind scale built on it, plus the phone kit's fixed corners.
 */
object Radius {
    val Sm = 6.dp
    val Md = 8.dp
    val Lg = 10.dp

    /** `rounded-xl` — every grey card and panel in the phone kit. */
    val Xl = 14.dp

    /** The green brand banner. */
    val Banner = 36.dp
}

object HogwartsShapes {
    val Sm: Shape = RoundedCornerShape(Radius.Sm)
    val Md: Shape = RoundedCornerShape(Radius.Md)
    val Lg: Shape = RoundedCornerShape(Radius.Lg)
    val Card: Shape = RoundedCornerShape(Radius.Xl)
    val Banner: Shape = RoundedCornerShape(Radius.Banner)
    val Pill: Shape = CircleShape

    /** App tiles cut their corners at 29.2% of the width, like the tile PNGs' alpha. */
    val Tile: Shape = SquircleShape(TILE_CORNER_FRACTION)
}

const val TILE_CORNER_FRACTION = 0.292f
