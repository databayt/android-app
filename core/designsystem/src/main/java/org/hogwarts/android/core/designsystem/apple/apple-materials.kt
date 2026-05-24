package org.hogwarts.android.core.designsystem.apple

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple-inspired glassmorphism material system.
 * Mirrors iOS materials from swift-app design system.
 *
 * On Android 12+ (API 31), blur effects are available via RenderEffect.
 * Below API 31, falls back to semi-transparent solid colors.
 */
enum class AppleMaterial(
    val lightAlpha: Float,
    val darkAlpha: Float
) {
    UltraThin(0.08f, 0.08f),
    Thin(0.25f, 0.25f),
    Regular(0.50f, 0.50f),
    Thick(0.70f, 0.70f)
}

@Composable
fun Modifier.liquidGlassCard(
    cornerRadius: Dp = 20.dp,
    material: AppleMaterial = AppleMaterial.Thin
): Modifier {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        Color.White.copy(alpha = material.darkAlpha)
    } else {
        Color.White.copy(alpha = material.lightAlpha + 0.6f)
    }
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }
    val shape = RoundedCornerShape(cornerRadius)

    return this
        .shadow(
            elevation = 12.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.08f),
            spotColor = Color.Black.copy(alpha = 0.08f)
        )
        .clip(shape)
        .background(bgColor, shape)
        .border(0.5.dp, borderColor, shape)
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.graphicsLayer {
                    // On API 31+ we could add RenderEffect blur here
                    // For now, the semi-transparent background provides the glass effect
                }
            } else {
                Modifier
            }
        )
}

@Composable
fun Modifier.glassOverlay(
    cornerRadius: Dp = 16.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        Color.White.copy(alpha = AppleMaterial.UltraThin.darkAlpha)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(bgColor, shape)
}

@Composable
fun Modifier.glassContainer(
    cornerRadius: Dp = 16.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        Color.White.copy(alpha = AppleMaterial.Regular.darkAlpha)
    } else {
        Color.White.copy(alpha = 0.85f)
    }
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(bgColor, shape)
        .border(0.5.dp, borderColor, shape)
}

@Composable
fun Modifier.glassPanel(
    cornerRadius: Dp = 16.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        Color.White.copy(alpha = AppleMaterial.Thick.darkAlpha)
    } else {
        Color.White.copy(alpha = 0.92f)
    }
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.04f)
    }
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(bgColor, shape)
        .border(0.5.dp, borderColor, shape)
}

private fun RoundedCornerShape(radius: Dp) =
    androidx.compose.foundation.shape.RoundedCornerShape(radius)
