package org.hogwarts.android.core.designsystem.animation

import androidx.compose.animation.core.*

object AppleSpring {
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val Smooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )

    val Interactive = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessVeryLow
    )

    fun <T> bouncySpec() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> smoothSpec() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> snappySpec() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )

    fun <T> interactiveSpec() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessVeryLow
    )

    val NavigationEnter = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 300f
    )

    val NavigationExit = spring<Float>(
        dampingRatio = 1f,
        stiffness = 400f
    )

    val ModalPresent = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 250f
    )

    val ModalDismiss = spring<Float>(
        dampingRatio = 1f,
        stiffness = 350f
    )
}
