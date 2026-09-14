package org.hogwarts.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The phone kit's type scale, measured from hogwarts
 * `src/components/school-dashboard/shared/` (Tailwind sizes → sp).
 */
@Immutable
data class KitTypography(
    /** Brand banner headline: `text-3xl leading-[1.35] font-light`. */
    val bannerHeadline: TextStyle,
    /** Menu popover links: `text-2xl`. */
    val menuLink: TextStyle,
    /** Wide stat figure: `text-2xl leading-8 font-bold`. */
    val figureWide: TextStyle,
    /** Section heading: `text-lg leading-7 font-semibold`. */
    val section: TextStyle,
    /** Stat figure: `text-lg leading-6 font-bold`. */
    val figure: TextStyle,
    /** Row title: `text-base leading-6 font-semibold`. */
    val rowTitle: TextStyle,
    /** Card value: `text-base leading-6 font-bold`. */
    val value: TextStyle,
    /** Body and descriptions: `text-sm`. */
    val body: TextStyle,
    /** Card title: `text-sm leading-5 font-semibold`. */
    val cardTitle: TextStyle,
    /** Values in info rows and pills: `text-sm font-medium`. */
    val bodyMedium: TextStyle,
    /** Tile label: `text-[13px] leading-4 font-semibold`. */
    val tileLabel: TextStyle,
    /** Captions, stat labels, meta: `text-xs`. */
    val caption: TextStyle,
    /** Badge count: `text-[11px] font-semibold`. */
    val badge: TextStyle,
)

fun kitTypography(fontFamily: FontFamily): KitTypography {
    fun style(size: Int, line: Double, weight: FontWeight) = TextStyle(
        fontFamily = fontFamily,
        fontSize = size.sp,
        lineHeight = line.sp,
        fontWeight = weight,
    )
    return KitTypography(
        bannerHeadline = style(30, 40.5, FontWeight.Light),
        menuLink = style(24, 32.0, FontWeight.Normal),
        figureWide = style(24, 32.0, FontWeight.Bold),
        section = style(18, 28.0, FontWeight.SemiBold),
        figure = style(18, 24.0, FontWeight.Bold),
        rowTitle = style(16, 24.0, FontWeight.SemiBold),
        value = style(16, 24.0, FontWeight.Bold),
        body = style(14, 20.0, FontWeight.Normal),
        cardTitle = style(14, 20.0, FontWeight.SemiBold),
        bodyMedium = style(14, 20.0, FontWeight.Medium),
        tileLabel = style(13, 16.0, FontWeight.SemiBold),
        caption = style(12, 16.0, FontWeight.Normal),
        badge = style(11, 11.0, FontWeight.SemiBold),
    )
}

val LocalKitTypography = staticCompositionLocalOf { kitTypography(FontFamily.Default) }
