package org.hogwarts.android.core.designsystem.apple

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Apple-inspired spring animation specs.
 * Matches iOS spring animations for natural, responsive feel.
 */
object AppleAnimation {
    val Responsive = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )

    val Gentle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val Smooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
}
