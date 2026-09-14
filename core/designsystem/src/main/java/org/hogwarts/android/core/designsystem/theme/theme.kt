package org.hogwarts.android.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

/**
 * Material 3 roles derived from the web tokens, so stock components (dialogs,
 * text fields, switches) land on the same neutral palette as the kit.
 * shadcn's neutral theme has no accent hue: primary is near-black in light and
 * near-white in dark, exactly as on the phone web.
 */
private fun HogwartsColors.toColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = primaryForeground,
        primaryContainer = muted,
        onPrimaryContainer = foreground,
        secondary = muted,
        onSecondary = foreground,
        secondaryContainer = muted,
        onSecondaryContainer = foreground,
        tertiary = positive,
        onTertiary = background,
        error = destructive,
        onError = Color.White,
        background = background,
        onBackground = foreground,
        surface = background,
        onSurface = foreground,
        surfaceVariant = muted,
        onSurfaceVariant = mutedForeground,
        surfaceContainerLowest = background,
        surfaceContainerLow = surface,
        surfaceContainer = muted,
        surfaceContainerHigh = muted,
        surfaceContainerHighest = muted,
        inverseSurface = foreground,
        inverseOnSurface = background,
        outline = border,
        outlineVariant = border,
        scrim = BrandColors.Ink,
    )
}

/**
 * Hogwarts theme: web tokens ([HogwartsColors]), the kit type scale and the
 * brand fonts for the current layout direction.
 *
 * RTL is read from [LocalLayoutDirection], which the framework derives from the
 * activity's `Configuration` after the per-app locale is applied.
 * Edge-to-edge is enabled by the activity (`enableEdgeToEdge()`); the theme only
 * keeps the system bar icons legible against the current background.
 */
@Composable
fun HogwartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) HogwartsColors.Dark else HogwartsColors.Light

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val fontFamily = BrandFonts.forDirection(isRtl)

    CompositionLocalProvider(
        LocalHogwartsColors provides colors,
        LocalWhatsAppColors provides if (darkTheme) WhatsAppColors.dark else WhatsAppColors.light,
        LocalKitTypography provides kitTypography(fontFamily),
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = hogwartsTypography(fontFamily),
            content = content
        )
    }
}

/** Accessors for the Hogwarts tokens inside [HogwartsTheme]. */
object HogwartsTheme {
    val colors: HogwartsColors
        @Composable @ReadOnlyComposable get() = LocalHogwartsColors.current

    val type: KitTypography
        @Composable @ReadOnlyComposable get() = LocalKitTypography.current
}
