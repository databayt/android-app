package org.hogwarts.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors mirrored from hogwarts `src/app/globals.css`.
 *
 * The web defines them in OKLCH; these are the exact sRGB conversions
 * (OKLab → linear sRGB → gamma), so a surface here matches the phone web
 * pixel for pixel. Read them through [HogwartsTheme.colors]; the Material 3
 * `ColorScheme` is derived from the same values for stock components.
 *
 * | token            | light                       | dark                         |
 * | ---------------- | --------------------------- | ---------------------------- |
 * | background       | oklch(1 0 0)      #FFFFFF   | oklch(0.145 0 0)   #0A0A0A   |
 * | foreground       | oklch(0.145 0 0)  #0A0A0A   | oklch(0.985 0 0)   #FAFAFA   |
 * | card             | oklch(1 0 0)      #FFFFFF   | oklch(0.205 0 0)   #171717   |
 * | primary          | oklch(0.205 0 0)  #171717   | oklch(0.922 0 0)   #E5E5E5   |
 * | muted            | oklch(0.97 0 0)   #F5F5F5   | oklch(0.269 0 0)   #262626   |
 * | muted-foreground | oklch(0.556 0 0)  #737373   | oklch(0.708 0 0)   #A1A1A1   |
 * | destructive      | oklch(0.577 0.245 27.3) #E7000B | oklch(0.704 0.191 22.2) #FF6467 |
 * | border           | oklch(0.922 0 0)  #E5E5E5   | oklch(1 0 0 / 10%)           |
 * | input            | oklch(0.922 0 0)  #E5E5E5   | oklch(1 0 0 / 15%)           |
 * | ring             | oklch(0.708 0 0)  #A1A1A1   | oklch(0.556 0 0)   #737373   |
 * | surface          | oklch(0.98 0 0)   #F8F8F8   | oklch(0.2 0 0)     #161616   |
 */
@Immutable
data class HogwartsColors(
    val background: Color,
    val foreground: Color,
    val card: Color,
    val cardForeground: Color,
    val primary: Color,
    val primaryForeground: Color,
    val muted: Color,
    val mutedForeground: Color,
    val destructive: Color,
    val border: Color,
    val input: Color,
    val ring: Color,
    val surface: Color,
    /** Figure tones used by the stat panel (Tailwind emerald/amber/sky 600 · 400 dark). */
    val positive: Color,
    val warning: Color,
    val info: Color,
    val isDark: Boolean,
) {
    companion object {
        val Light = HogwartsColors(
            background = Color(0xFFFFFFFF),
            foreground = Color(0xFF0A0A0A),
            card = Color(0xFFFFFFFF),
            cardForeground = Color(0xFF0A0A0A),
            primary = Color(0xFF171717),
            primaryForeground = Color(0xFFFAFAFA),
            muted = Color(0xFFF5F5F5),
            mutedForeground = Color(0xFF737373),
            destructive = Color(0xFFE7000B),
            border = Color(0xFFE5E5E5),
            input = Color(0xFFE5E5E5),
            ring = Color(0xFFA1A1A1),
            surface = Color(0xFFF8F8F8),
            positive = Color(0xFF009966),
            warning = Color(0xFFE17100),
            info = Color(0xFF0084D1),
            isDark = false,
        )

        val Dark = HogwartsColors(
            background = Color(0xFF0A0A0A),
            foreground = Color(0xFFFAFAFA),
            card = Color(0xFF171717),
            cardForeground = Color(0xFFFAFAFA),
            primary = Color(0xFFE5E5E5),
            primaryForeground = Color(0xFF171717),
            muted = Color(0xFF262626),
            mutedForeground = Color(0xFFA1A1A1),
            destructive = Color(0xFFFF6467),
            border = Color(0x1AFFFFFF),
            input = Color(0x26FFFFFF),
            ring = Color(0xFF737373),
            surface = Color(0xFF161616),
            positive = Color(0xFF00D492),
            warning = Color(0xFFFFB900),
            info = Color(0xFF00BCFF),
            isDark = true,
        )
    }
}

/**
 * Brand colors that do NOT invert with the theme — artwork and brand grounds,
 * pinned on the web as literal hexes for the same reason.
 */
object BrandColors {
    /** The green banner ground (`#00bc6d`): library hero, live, next action. */
    val Green = Color(0xFF00BC6D)

    /** Ink on the green banner; white on this green measures ~2.5:1. */
    val Ink = Color(0xFF050505)

    /** The dashboard's calendar card. */
    val CalendarMint = Color(0xFF9FE5B1)

    /** iOS system red — the calendar weekday. */
    val WeekdayRed = Color(0xFFFF3B30)

    /** Live / upcoming / missed session states. */
    val Live = Color(0xFF00BC6D)
    val Upcoming = Color(0xFFF5A524)
    val Missed = Color(0xFFE5484D)

    /** Messages unread badge. */
    val UnreadBadge = Color(0xFF1DAB61)

    /** App icon ground (orange feather). */
    val AppIcon = Color(0xFFE8704E)

    /** Attendance status marks (Tailwind red-500 / amber-500 / emerald-500). */
    val Absent = Color(0xFFFB2C36)
    val Late = Color(0xFFFE9A00)
    val Present = Color(0xFF00BC7D)
}

val LocalHogwartsColors = staticCompositionLocalOf { HogwartsColors.Light }
