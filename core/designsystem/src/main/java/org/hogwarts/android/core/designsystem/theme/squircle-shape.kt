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
        // The smoothed corner starts ~1.28x further along the edge than a
        // circular arc of the same radius would.
        val r = min(w, h) * cornerFraction
        val extent = min(r * 1.28f, min(w, h) / 2f)
        val k = r * 0.44f

        val path = Path().apply {
            moveTo(extent, 0f)
            lineTo(w - extent, 0f)
            cubicTo(w - extent + k, 0f, w, extent - k, w, extent)
            lineTo(w, h - extent)
            cubicTo(w, h - extent + k, w - extent + k, h, w - extent, h)
            lineTo(extent, h)
            cubicTo(extent - k, h, 0f, h - extent + k, 0f, h - extent)
            lineTo(0f, extent)
            cubicTo(0f, extent - k, extent - k, 0f, extent, 0f)
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean =
        other is SquircleShape && other.cornerFraction == cornerFraction

    override fun hashCode(): Int = cornerFraction.hashCode()
}
