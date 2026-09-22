package org.hogwarts.android.feature.subjects.textbook.ui

import androidx.compose.ui.graphics.Color
import org.hogwarts.android.feature.subjects.textbook.data.ReaderThemeName
import java.text.NumberFormat
import java.util.Locale

/** A reading palette — the `--book-*` variables of one `data-theme` × `data-mode` in `reader.css`. */
data class ReaderPalette(
    val bg: Color,
    val fg: Color,
    val muted: Color,
    val rule: Color,
    val chromeBg: Color,
    val chromeFg: Color,
    val pill: Color,
    val pillFg: Color,
    val pillDark: Color,
    val pillDarkFg: Color,
    val scrubEmpty: Color,
    val scrubEmptyFg: Color,
    val scrubFill: Color,
    val scrubFillFg: Color,
    /** Dark mode and Quiet veil the page deeper under the line guide. */
    val deepVeil: Boolean,
)

private fun hex(v: Long) = Color(v or 0xFF000000)
private fun rgba(r: Int, g: Int, b: Int, a: Float) = Color(r, g, b, (a * 255).toInt())

private val ORIGINAL = ReaderPalette(
    bg = hex(0xffffff), fg = hex(0x1c1c1e), muted = hex(0x8e8e93), rule = rgba(0, 0, 0, 0.12f),
    chromeBg = rgba(255, 255, 255, 0.84f), chromeFg = hex(0x1c1c1e),
    pill = hex(0xe6e6e9), pillFg = hex(0x1c1c1e), pillDark = hex(0x36363a), pillDarkFg = hex(0xffffff),
    scrubEmpty = hex(0x343338), scrubEmptyFg = hex(0xffffff), scrubFill = hex(0xe6e5ea), scrubFillFg = hex(0x1c1c1e),
    deepVeil = false,
)

fun readerPalette(theme: ReaderThemeName, dark: Boolean): ReaderPalette {
    val light = when (theme) {
        ReaderThemeName.Original -> ORIGINAL
        ReaderThemeName.Quiet -> ORIGINAL.copy(
            bg = hex(0x4a494e), fg = hex(0xacaab5), muted = hex(0x8b8a93), rule = rgba(255, 255, 255, 0.14f),
            chromeBg = rgba(74, 73, 78, 0.86f), chromeFg = hex(0xd6d5dc),
            pill = rgba(255, 255, 255, 0.12f), pillFg = hex(0xeceaf2), pillDark = hex(0xeceaf2), pillDarkFg = hex(0x2b2a2e),
            scrubEmpty = hex(0x2f2e33), scrubEmptyFg = hex(0xf2f2f7), deepVeil = true,
        )
        ReaderThemeName.Paper -> ORIGINAL.copy(
            bg = hex(0xededed), fg = hex(0x1d1c1a), muted = hex(0x7c7c82),
            chromeBg = rgba(237, 237, 237, 0.86f), pill = rgba(255, 255, 255, 0.8f),
        )
        ReaderThemeName.Bold -> ORIGINAL.copy(fg = hex(0x1b1b1b))
        ReaderThemeName.Calm -> ORIGINAL.copy(
            bg = hex(0xeee2ca), fg = hex(0x332a23), muted = hex(0x9a8b7a), rule = rgba(51, 42, 35, 0.14f),
            chromeBg = rgba(238, 226, 202, 0.86f), chromeFg = hex(0x332a23),
            pill = rgba(244, 236, 219, 0.95f), pillFg = hex(0x332a23), pillDark = hex(0x332a23),
            scrubEmpty = hex(0x332a23), scrubFill = hex(0xf4ecdb), scrubFillFg = hex(0x332a23),
        )
        ReaderThemeName.Focus -> ORIGINAL.copy(
            bg = hex(0xfffcf5), fg = hex(0x1c1a12), muted = hex(0x8b8778), rule = rgba(28, 26, 18, 0.12f),
            chromeBg = rgba(255, 252, 245, 0.86f), chromeFg = hex(0x1c1a12), pill = hex(0xefeadd), pillFg = hex(0x1c1a12),
        )
    }
    if (!dark) return light
    return light.copy(
        bg = hex(0x000000),
        fg = when (theme) {
            ReaderThemeName.Calm -> hex(0xded0b4)
            ReaderThemeName.Focus -> hex(0xe3ddcb)
            else -> hex(0xd9d9de)
        },
        muted = hex(0x7a7a80), rule = rgba(255, 255, 255, 0.14f),
        chromeBg = rgba(28, 28, 30, 0.86f), chromeFg = hex(0xe5e5ea),
        pill = rgba(44, 44, 46, 0.95f), pillFg = hex(0xf2f2f7), pillDark = hex(0xf2f2f7), pillDarkFg = hex(0x1c1c1e),
        scrubEmpty = hex(0x2c2c2e), scrubEmptyFg = hex(0xf2f2f7), scrubFill = hex(0xe6e5ea), scrubFillFg = hex(0x1c1c1e),
        deepVeil = true,
    )
}

/** The theme cards' swatches — fixed, whatever the reader's current palette. */
fun themeSwatch(theme: ReaderThemeName): Pair<Color, Color> = when (theme) {
    ReaderThemeName.Original -> hex(0xffffff) to hex(0x1c1c1e)
    ReaderThemeName.Quiet -> hex(0x4a494e) to hex(0xacaab5)
    ReaderThemeName.Paper -> hex(0xededed) to hex(0x1d1c1a)
    ReaderThemeName.Bold -> hex(0xffffff) to hex(0x1b1b1b)
    ReaderThemeName.Calm -> hex(0xeee2ca) to hex(0x332a23)
    ReaderThemeName.Focus -> hex(0xfffcf5) to hex(0x1c1a12)
}

/** `formatNumber` in the web's format.ts: Arabic-Indic digits for Arabic, no grouping. */
fun formatNumber(n: Int, lang: String): String {
    val format = NumberFormat.getInstance(if (lang == "ar") Locale.forLanguageTag("ar-EG") else Locale.ENGLISH)
    format.isGroupingUsed = false
    return format.format(n)
}
