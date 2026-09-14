package org.hogwarts.android.core.designsystem.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

/**
 * A continuous-corner rounded rectangle (iOS app icon shape).
 *
 * Each corner eases into the straight edge with two cubic segments instead of a
 * circular arc, so the curvature has no visible "kink" where a
 * `RoundedCornerShape` meets the side. [cornerFraction] is the corner extent as
 * a share of the shorter side, matching the web's `rounded-[29.2%]` clip.
 */
class SquircleShape(private val cornerFraction: Float) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        // A smoothed corner starts ~1.28x further along the edge than a
        // circular arc of the same radius; its control points sit 80% of the
        // way toward the corner, which keeps curvature continuous at the edge.
        val r = min(w, h) * cornerFraction
        val e = min(r * 1.28f, min(w, h) / 2f)
        val c = e * 0.2f

        val path = Path().apply {
            moveTo(e, 0f)
            lineTo(w - e, 0f)
            cubicTo(w - c, 0f, w, c, w, e)
            lineTo(w, h - e)
            cubicTo(w, h - c, w - c, h, w - e, h)
            lineTo(e, h)
            cubicTo(c, h, 0f, h - c, 0f, h - e)
            lineTo(0f, e)
            cubicTo(0f, c, c, 0f, e, 0f)
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean =
        other is SquircleShape && other.cornerFraction == cornerFraction

    override fun hashCode(): Int = cornerFraction.hashCode()
}
