package org.hogwarts.android.core.designsystem.apple

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple's 8pt grid spacing system.
 * Mirrors iOS `AppleSpacing` from swift-app design system.
 */
object AppleSpacing {
    val Tiny: Dp = 4.dp
    val Compact: Dp = 8.dp
    val Small: Dp = 12.dp
    val Standard: Dp = 16.dp
    val Comfortable: Dp = 20.dp
    val Large: Dp = 24.dp
    val ExtraLarge: Dp = 32.dp
    val MinTouchTarget: Dp = 44.dp
}

object ApplePadding {
    val Standard = PaddingValues(AppleSpacing.Standard)
    val Compact = PaddingValues(AppleSpacing.Compact)
    val Comfortable = PaddingValues(AppleSpacing.Comfortable)
    val HorizontalStandard = PaddingValues(horizontal = AppleSpacing.Standard)
    val VerticalStandard = PaddingValues(vertical = AppleSpacing.Standard)
}

fun Modifier.standardPadding() = padding(AppleSpacing.Standard)
fun Modifier.compactPadding() = padding(AppleSpacing.Compact)
fun Modifier.comfortablePadding() = padding(AppleSpacing.Comfortable)
fun Modifier.horizontalPadding(amount: Dp = AppleSpacing.Standard) = padding(horizontal = amount)
fun Modifier.verticalPadding(amount: Dp = AppleSpacing.Standard) = padding(vertical = amount)
fun Modifier.sectionSpacing() = padding(vertical = AppleSpacing.Large)
