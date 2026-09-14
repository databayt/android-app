package org.hogwarts.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The Messages surface's `--wa-*` tokens, value for value from hogwarts
 * `src/app/globals.css` (the `WA TOKENS START … END` block, `:root` and
 * `.dark`). The web writes them as sRGB hex / rgba already, so no OKLCH
 * conversion is involved; each value carries its CSS source as a comment.
 *
 * The glass tokens stand in for the `.wa-glass-*` classes further down the
 * same file, and the wallpaper ground and read tick are the literals
 * `chat-wallpaper.tsx` and `bubble-timestamp.tsx` use.
 */
@Immutable
data class WhatsAppColors(
    val surfacePrimary: Color,
    val surfaceProduct: Color,
    val surfaceCtaFilters: Color,
    val surfaceCtaFiltersActive: Color,
    val borderCtaFiltersActive: Color,
    val surfaceCtaCircular: Color,
    val surfacePanelBlur: Color,
    val surfacePanel: Color,
    val surfaceSearchChat: Color,
    val surfaceInvert: Color,
    val surfaceBaloonMe: Color,
    val surfaceBaloonOther: Color,
    val surfaceDate: Color,
    val surfaceInputChat: Color,
    val surfaceShadowBaloon: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textSecondaryAlpha: Color,
    val textProduct: Color,
    val textInvert: Color,
    val textCtaFilters: Color,
    val textCtaFiltersActive: Color,
    val textTabbar: Color,
    val textTabbarSelected: Color,
    val surfaceAvatarGroup: Color,
    val textAvatarGroup: Color,
    val surfaceAvatarPerson: Color,
    val textAvatarPerson: Color,
    val borderCtaFilters: Color,
    val glassBg: Color,
    val glassBgChat: Color,
    val glassBorder: Color,
    val glassInner: Color,
    /** `.wa-glass-control` glyph colour. */
    val glassGlyph: Color,
    /** `.wa-glass-menu` fill. */
    val glassMenu: Color,
    /** The top rim of the refractive edge (`inset 1px 1px 1.5px`). */
    val glassRimLight: Color,
    val borderPanel: Color,
    val borderSeparator: Color,
    val borderAvatar: Color,
    val borderInputChat: Color,
    val borderQuote: Color,
    val textQuoteTitle: Color,
    val surfaceNotice: Color,
    val textNotice: Color,
    /** `chat-wallpaper.tsx`: `bg-[#F4F0E9] dark:bg-[#0B141A]`. */
    val wallpaper: Color,
    /** `.wa-scroll-edge` gradient head. */
    val scrollEdge: Color,
    /** `.wa-scroll-edge-list` gradient head. */
    val scrollEdgeList: Color,
    /** `bubble-timestamp.tsx`: read ticks `text-[#53BDEB]`. */
    val readTick: Color,
    /** `message-bubble.tsx`: the failed mark `bg-[#ff3b30]`. */
    val failedMark: Color,
    /** `reply-bubble.tsx`: quote bar and sender `#DA4F7A`. */
    val replyAccent: Color,
) {
    companion object {
        val light = WhatsAppColors(
            surfacePrimary = Color(0xFFFFFFFF), // #ffffff
            surfaceProduct = Color(0xFF1DAB61), // #1dab61
            surfaceCtaFilters = Color(0xFFF4F4F4), // #f4f4f4
            surfaceCtaFiltersActive = Color(0xFFCFFDCF), // #cffdcf
            borderCtaFiltersActive = Color(0xFFA7CBA5), // #a7cba5
            surfaceCtaCircular = Color(0x080A0A0A), // rgba(10, 10, 10, 0.03)
            surfacePanelBlur = Color(0xCCF4F4F4), // rgba(244, 244, 244, 0.8)
            surfacePanel = Color(0xCCF5F2EB), // rgba(245, 242, 235, 0.8)
            surfaceSearchChat = Color(0xFFF5F5F4), // #f5f5f4
            surfaceInvert = Color(0xFF0A0A0A), // #0a0a0a
            surfaceBaloonMe = Color(0xFFCFFDCF), // #cffdcf
            surfaceBaloonOther = Color(0xFFFFFFFF), // #ffffff
            surfaceDate = Color(0xFFFDFDFD), // #fdfdfd
            surfaceInputChat = Color(0xFFFFFFFF), // #ffffff
            surfaceShadowBaloon = Color(0x0F000000), // rgba(0, 0, 0, 0.06)
            textPrimary = Color(0xFF0A0A0A), // #0a0a0a
            textSecondary = Color(0xFF767779), // #767779
            textSecondaryAlpha = Color(0x80000000), // rgba(0, 0, 0, 0.5)
            textProduct = Color(0xFF1DAB61), // #1dab61
            textInvert = Color(0xFFFFFFFF), // #ffffff
            textCtaFilters = Color(0xFF6A6C6C), // #6a6c6c
            textCtaFiltersActive = Color(0xFF00613B), // #00613b
            textTabbar = Color(0xFF767779), // #767779
            textTabbarSelected = Color(0xFF0A0A0A), // #0a0a0a
            surfaceAvatarGroup = Color(0xFFDFFBD6), // #dffbd6
            textAvatarGroup = Color(0xFF408559), // #408559
            surfaceAvatarPerson = Color(0xFFD6E7FB), // #d6e7fb
            textAvatarPerson = Color(0xFF2E67C5), // #2e67c5
            borderCtaFilters = Color(0xFFCCCCCC), // #cccccc
            glassBg = Color(0xD9F1F1F1), // rgba(241, 241, 241, 0.85)
            glassBgChat = Color(0xD9FFFDF7), // rgba(255, 253, 247, 0.85)
            glassBorder = Color(0xA6FFFFFF), // rgba(255, 255, 255, 0.65)
            glassInner = Color(0x12000000), // rgba(0, 0, 0, 0.07)
            glassGlyph = Color(0xFF1A1A1A), // .wa-glass-control color: #1a1a1a
            glassMenu = Color(0xE6FBFBFB), // .wa-glass-menu rgba(251, 251, 251, 0.9)
            glassRimLight = Color(0xF2FFFFFF), // inset rgba(255, 255, 255, 0.95)
            borderPanel = Color(0x4D000000), // rgba(0, 0, 0, 0.3)
            borderSeparator = Color(0x33000000), // rgba(0, 0, 0, 0.2)
            borderAvatar = Color(0x1A0A0A0A), // rgba(10, 10, 10, 0.1)
            borderInputChat = Color(0xFFB2B2B2), // #b2b2b2
            borderQuote = Color(0xFFD42A66), // #d42a66
            textQuoteTitle = Color(0xFFD42A66), // #d42a66
            surfaceNotice = Color(0xFFFFEED1), // #ffeed1
            textNotice = Color(0xFF0A0A0A), // #0a0a0a
            wallpaper = Color(0xFFF4F0E9), // #F4F0E9
            scrollEdge = Color(0xEBF5F2EB), // rgba(245, 242, 235, 0.92)
            scrollEdgeList = Color(0xF0FFFFFF), // rgba(255, 255, 255, 0.94)
            readTick = Color(0xFF53BDEB), // #53BDEB
            failedMark = Color(0xFFFF3B30), // #ff3b30
            replyAccent = Color(0xFFDA4F7A), // #DA4F7A
        )
        val dark = WhatsAppColors(
            surfacePrimary = Color(0xFF0B141A), // #0b141a
            surfaceProduct = Color(0xFF1DAB61), // #1dab61
            surfaceCtaFilters = Color(0xFF1F2C33), // #1f2c33
            surfaceCtaFiltersActive = Color(0xFF0B3624), // #0b3624
            borderCtaFiltersActive = Color(0xFF1D5C3C), // #1d5c3c
            surfaceCtaCircular = Color(0x14FFFFFF), // rgba(255, 255, 255, 0.08)
            surfacePanelBlur = Color(0xCC111B21), // rgba(17, 27, 33, 0.8)
            surfacePanel = Color(0xD9111B21), // rgba(17, 27, 33, 0.85)
            surfaceSearchChat = Color(0xFF1F2C33), // #1f2c33
            surfaceInvert = Color(0xFFFFFFFF), // #ffffff
            surfaceBaloonMe = Color(0xFF005C4B), // #005c4b
            surfaceBaloonOther = Color(0xFF202C33), // #202c33
            surfaceDate = Color(0xFF1F2C33), // #1f2c33
            surfaceInputChat = Color(0xFF1F2C33), // #1f2c33
            surfaceShadowBaloon = Color(0x33000000), // rgba(0, 0, 0, 0.2)
            textPrimary = Color(0xFFE9EDEF), // #e9edef
            textSecondary = Color(0xFF8696A0), // #8696a0
            textSecondaryAlpha = Color(0x80E9EDEF), // rgba(233, 237, 239, 0.5)
            textProduct = Color(0xFF25D366), // #25d366
            textInvert = Color(0xFF0B141A), // #0b141a
            textCtaFilters = Color(0xFF8696A0), // #8696a0
            textCtaFiltersActive = Color(0xFFAFF2BF), // #aff2bf
            textTabbar = Color(0xFF8696A0), // #8696a0
            textTabbarSelected = Color(0xFFE9EDEF), // #e9edef
            surfaceAvatarGroup = Color(0xFF1D3B26), // #1d3b26
            textAvatarGroup = Color(0xFF7FD39C), // #7fd39c
            surfaceAvatarPerson = Color(0xFF1E3348), // #1e3348
            textAvatarPerson = Color(0xFF7FB6E8), // #7fb6e8
            borderCtaFilters = Color(0xFF2A3942), // #2a3942
            glassBg = Color(0xD9202C33), // rgba(32, 44, 51, 0.85)
            // .dark .wa-glass-control rgba(60, 70, 78, 0.55) wins over the chat tint.
            glassBgChat = Color(0x8C3C464E),
            glassBorder = Color(0x26FFFFFF), // rgba(255, 255, 255, 0.15)
            glassInner = Color(0x1AFFFFFF), // rgba(255, 255, 255, 0.1)
            glassGlyph = Color(0xFFF2F2F2), // .dark .wa-glass-control color: #f2f2f2
            glassMenu = Color(0xE0283036), // .dark .wa-glass-menu rgba(40, 48, 54, 0.88)
            glassRimLight = Color(0x38FFFFFF), // inset rgba(255, 255, 255, 0.22)
            borderPanel = Color(0x1FFFFFFF), // rgba(255, 255, 255, 0.12)
            borderSeparator = Color(0x338696A0), // rgba(134, 150, 160, 0.2)
            borderAvatar = Color(0x14FFFFFF), // rgba(255, 255, 255, 0.08)
            borderInputChat = Color(0xFF323D44), // #323d44
            borderQuote = Color(0xFFF26196), // #f26196
            textQuoteTitle = Color(0xFFF26196), // #f26196
            surfaceNotice = Color(0xFF182229), // #182229
            textNotice = Color(0xFFFFD279), // #ffd279
            wallpaper = Color(0xFF0B141A), // #0B141A
            scrollEdge = Color(0xEB0B141A), // rgba(11, 20, 26, 0.92)
            scrollEdgeList = Color(0xEB0B141A), // rgba(11, 20, 26, 0.92)
            readTick = Color(0xFF53BDEB), // #53BDEB
            failedMark = Color(0xFFFF3B30), // #ff3b30
            replyAccent = Color(0xFFDA4F7A), // #DA4F7A
        )
    }
}

val LocalWhatsAppColors = compositionLocalOf { WhatsAppColors.light }
