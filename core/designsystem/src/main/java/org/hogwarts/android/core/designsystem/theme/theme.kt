package org.hogwarts.android.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import android.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

/**
 * Light color scheme for Hogwarts Android.
 */
private val LightColorScheme = lightColorScheme(
    primary = HogwartsPrimary,
    onPrimary = HogwartsOnPrimary,
    primaryContainer = HogwartsPrimaryLight,
    onPrimaryContainer = HogwartsPrimaryDark,

    secondary = HogwartsSecondary,
    onSecondary = HogwartsOnSecondary,
    secondaryContainer = HogwartsSecondaryLight,
    onSecondaryContainer = HogwartsSecondaryDark,

    tertiary = HogwartsTertiary,
    onTertiary = HogwartsOnTertiary,
    tertiaryContainer = HogwartsTertiaryLight,
    onTertiaryContainer = HogwartsTertiaryDark,

    error = HogwartsError,
    onError = HogwartsOnError,
    errorContainer = HogwartsErrorLight,
    onErrorContainer = HogwartsOnErrorContainer,

    background = HogwartsBackground,
    onBackground = HogwartsOnBackground,

    surface = HogwartsSurface,
    onSurface = HogwartsOnSurface,
    surfaceVariant = HogwartsSurfaceVariant,
    onSurfaceVariant = HogwartsOnSurfaceVariant,
    surfaceContainerLowest = HogwartsSurface,
    surfaceContainerLow = HogwartsSurface,
    surfaceContainer = HogwartsSurface,
    surfaceContainerHigh = HogwartsSurface,
    surfaceContainerHighest = HogwartsSurfaceVariant,

    outline = HogwartsOutline,
    outlineVariant = HogwartsOutline
)

/**
 * Dark color scheme for Hogwarts Android.
 */
private val DarkColorScheme = darkColorScheme(
    primary = HogwartsPrimaryLight,
    onPrimary = HogwartsPrimaryDark,
    primaryContainer = HogwartsPrimary,
    onPrimaryContainer = HogwartsOnPrimary,

    secondary = HogwartsSecondaryLight,
    onSecondary = HogwartsSecondaryDark,
    secondaryContainer = HogwartsSecondary,
    onSecondaryContainer = HogwartsOnSecondary,

    tertiary = HogwartsTertiaryLight,
    onTertiary = HogwartsTertiaryDark,
    tertiaryContainer = HogwartsTertiary,
    onTertiaryContainer = HogwartsOnTertiary,

    error = HogwartsErrorLight,
    onError = HogwartsOnErrorContainer,
    errorContainer = HogwartsError,
    onErrorContainer = HogwartsOnError,

    background = HogwartsBackgroundDark,
    onBackground = HogwartsOnBackgroundDark,

    surface = HogwartsSurfaceDark,
    onSurface = HogwartsOnSurfaceDark,
    surfaceVariant = HogwartsSurfaceVariantDark,
    onSurfaceVariant = HogwartsOnSurfaceVariantDark,
    surfaceContainerLowest = HogwartsBackgroundDark,
    surfaceContainerLow = HogwartsSurfaceDark,
    surfaceContainer = HogwartsSurfaceDark,
    surfaceContainerHigh = HogwartsSurfaceVariantDark,
    surfaceContainerHighest = HogwartsSurfaceVariantDark,

    outline = HogwartsOutlineDark,
    outlineVariant = HogwartsOutlineDark
)

/**
 * Hogwarts Android theme wrapper.
 *
 * RTL is read from [LocalLayoutDirection], which the framework derives from the
 * activity's `Configuration` after [androidx.appcompat.app.AppCompatDelegate.setApplicationLocales]
 * (or the platform `LocaleManager` on Android 13+) applies the per-app locale.
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic color (Android 12+)
 * @param content Composable content
 */
@Composable
fun HogwartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: transparent status bar, let content draw behind
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val fontFamily = if (isRtl) SFArabicFontFamily else SFProFontFamily
    val typography = hogwartsTypography(fontFamily)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
