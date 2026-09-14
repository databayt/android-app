package org.hogwarts.android.feature.messaging.ui.format

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Clock, date and count formatting for Messages.
 *
 * The web's Messages code does not follow `i18n-format.ts` here: it prints
 * every clock, date pill and list stamp with Latin digits under Arabic too
 * (`ar-u-nu-latn` in `ios-chat-list.tsx` and `chat/adapt.ts`, "WhatsApp prints
 * bubble times in Latin digits under an Arabic UI"), and counts go out as raw
 * numbers. [easternArabicDigits] is that one switch; it is off to match the web.
 */
class MessagingFormat(
    locale: Locale,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val today: () -> LocalDate = { LocalDate.now(zone) },
    private val easternArabicDigits: Boolean = false,
) {
    private val arabic = locale.language == "ar"
    private val digitLocale: Locale =
        if (arabic && easternArabicDigits) Locale.forLanguageTag("ar-u-nu-arab") else Locale.ENGLISH
    private val textLocale: Locale = if (arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH

    private fun digits(s: String): String =
        if (!(arabic && easternArabicDigits)) s
        else s.map { c -> if (c in '0'..'9') '٠' + (c - '0') else c }.joinToString("")

    fun count(value: Int): String = NumberFormat.getIntegerInstance(digitLocale).format(value)

    /** `bubbleTime`: 24h `HH:mm`. */
    fun clock(at: Instant): String = digits(CLOCK.format(at.atZone(zone)))

    /** `formatTimestamp` in `ios-chat-list.tsx`: time today, "Yesterday", else day and month. */
    fun listStamp(at: Instant, yesterdayLabel: String): String {
        val day = at.atZone(zone).toLocalDate()
        val now = today()
        return when (day) {
            now -> clock(at)
            now.minusDays(1) -> yesterdayLabel
            // en-US `{day:2-digit, month:2-digit}` is MM/DD; Arabic reads day first.
            else -> digits(day.format(if (arabic) DAY_MONTH_AR else MONTH_DAY_EN))
        }
    }

    /** `daySeparatorLabel` in `chat/adapt.ts`. */
    fun daySeparator(at: Instant, todayLabel: String, yesterdayLabel: String): String {
        val day = at.atZone(zone).toLocalDate()
        val now = today()
        return when (day) {
            now -> todayLabel
            now.minusDays(1) -> yesterdayLabel
            else -> {
                val pattern = if (day.year == now.year) "d MMMM" else if (arabic) "d MMMM y" else "MMMM d, y"
                val sameYear = if (arabic) "d MMMM" else "MMMM d"
                digits(day.format(DateTimeFormatter.ofPattern(if (day.year == now.year) sameYear else pattern, textLocale)))
            }
        }
    }

    fun isSameDay(a: Instant, b: Instant): Boolean =
        a.atZone(zone).toLocalDate() == b.atZone(zone).toLocalDate()

    private companion object {
        val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        val MONTH_DAY_EN: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.ENGLISH)
        // `ar-u-nu-latn` prints "10\u200F/09": a right-to-left mark after the day.
        val DAY_MONTH_AR: DateTimeFormatter = DateTimeFormatter.ofPattern("dd'\u200F'/MM", Locale.ENGLISH)
    }
}

/** A photo-less person's disc: `AVATAR_COLORS` in hogwarts `messaging/avatar.tsx`. */
data class AvatarColor(val background: Color, val glyph: Color)

private val AVATAR_COLORS = listOf(
    AvatarColor(Color(0xFFCBF2EE), Color(0xFF028377)), // bg #CBF2EE, icon #028377
    AvatarColor(Color(0xFFE9E0FF), Color(0xFF5D47DE)), // bg #E9E0FF, icon #5D47DE
    AvatarColor(Color(0xFFFEF1D4), Color(0xFF9D6C2C)), // bg #FEF1D4, icon #9D6C2C
    AvatarColor(Color(0xFFFBD8DC), Color(0xFFD10335)), // bg #FBD8DC, icon #D10335
)

/**
 * `getAvatarColor`, bit for bit: JavaScript's `hash << 5` truncates to int32
 * but the subtraction that follows does not, so the running hash is a double.
 */
fun avatarColorFor(id: String): AvatarColor {
    var hash = 0.0
    for (ch in id) {
        val shifted = (hash.toLong().toInt() shl 5).toDouble()
        hash = ch.code + (shifted - hash)
    }
    return AVATAR_COLORS[(kotlin.math.abs(hash) % AVATAR_COLORS.size).toInt()]
}
