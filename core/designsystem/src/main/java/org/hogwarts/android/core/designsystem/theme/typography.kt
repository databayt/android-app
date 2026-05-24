package org.hogwarts.android.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.R

/**
 * Hogwarts Typography following Material 3 type scale.
 *
 * Mapping to semantic HTML (web equivalent):
 * - headlineLarge → h1
 * - headlineMedium → h2
 * - headlineSmall → h3
 * - titleLarge → h4
 * - titleMedium → h5
 * - titleSmall → h6
 * - bodyLarge → p
 * - bodyMedium → p (smaller)
 * - bodySmall → small
 * - labelLarge → button text
 * - labelMedium → caption, muted text
 * - labelSmall → overline
 *
 * Usage:
 * ```kotlin
 * Text(
 *     text = "Page Title",
 *     style = MaterialTheme.typography.headlineLarge
 * )
 * ```
 */

/**
 * SF Pro font family for Latin text (English).
 * Apple's system font — variable font with full weight axis.
 */
val SFProFontFamily = FontFamily(
    Font(R.font.sf_pro, FontWeight.Normal),
    Font(R.font.sf_pro, FontWeight.Medium),
    Font(R.font.sf_pro, FontWeight.SemiBold),
    Font(R.font.sf_pro, FontWeight.Bold)
)

/**
 * SF Arabic font family for Arabic text (RTL).
 * Apple's system Arabic font — variable font with full weight axis.
 */
val SFArabicFontFamily = FontFamily(
    Font(R.font.sf_arabic, FontWeight.Normal),
    Font(R.font.sf_arabic, FontWeight.Medium),
    Font(R.font.sf_arabic, FontWeight.SemiBold),
    Font(R.font.sf_arabic, FontWeight.Bold)
)

/**
 * Default font family (SF Pro). Use [SFArabicFontFamily] for Arabic locale.
 * The active font is selected in [HogwartsTheme] via [hogwartsTypography].
 */
val HogwartsFontFamily = SFProFontFamily
val HogwartsArabicFontFamily = SFArabicFontFamily

/**
 * Builds a [Typography] scale using the given [fontFamily].
 * Call with [SFProFontFamily] for English or [SFArabicFontFamily] for Arabic.
 */
fun hogwartsTypography(fontFamily: FontFamily = SFProFontFamily): Typography = Typography(
    // Display styles (rarely used, for hero sections)
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles (page/section titles)
    headlineLarge = TextStyle(  // h1
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(  // h2
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(  // h3
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles (card/component titles)
    titleLarge = TextStyle(  // h4
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(  // h5
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(  // h6
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles (paragraph text)
    bodyLarge = TextStyle(  // p
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(  // p (smaller)
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(  // small
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles (buttons, captions)
    labelLarge = TextStyle(  // button text
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(  // caption, muted
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(  // overline
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Default typography using Rubik. Backward-compatible with existing code.
 * For locale-aware typography, use [hogwartsTypography] with the appropriate font family.
 */
val HogwartsTypography = hogwartsTypography(SFProFontFamily)
