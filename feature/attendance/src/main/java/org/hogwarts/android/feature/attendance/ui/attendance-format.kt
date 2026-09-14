package org.hogwarts.android.feature.attendance.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.feature.attendance.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

/**
 * Numbers and dates the way the web's `Intl` prints them for `ar` and `en`:
 * Eastern Arabic digits under Arabic, Latin under English, Gregorian always.
 */
internal class AttendanceFormat(locale: Locale) {
    private val arabic = locale.language == "ar"
    private val numbering: Locale = if (arabic) Locale.forLanguageTag("ar-u-nu-arab") else Locale.ENGLISH
    private val digits = DecimalStyle.of(numbering)

    fun count(value: Int): String = NumberFormat.getIntegerInstance(numbering).format(value)

    fun percent(value: Int): String = NumberFormat.getPercentInstance(numbering).format(value / 100.0)

    /** `{ weekday: "long", day: "numeric", month: "long" }` — "Monday, September 14". */
    fun longDay(date: LocalDate): String = date.format(pattern(if (arabic) "EEEE، d MMMM" else "EEEE, MMMM d"))

    /** `{ weekday: "short", day: "numeric", month: "short" }` — "Mon, Sep 14". */
    fun shortDay(date: LocalDate): String = date.format(pattern(if (arabic) "EEE، d MMM" else "EEE, MMM d"))

    /** A wall-clock "HH:mm" from the server, in the reader's digits. */
    fun clock(hhmm: String): String = if (arabic) hhmm.map { c -> if (c.isDigit()) digits.zeroDigit + (c - '0') else c }.joinToString("") else hhmm

    private fun pattern(p: String) = DateTimeFormatter.ofPattern(p, if (arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH).withDecimalStyle(digits)
}

@Composable
@ReadOnlyComposable
internal fun attendanceFormat(): AttendanceFormat = AttendanceFormat(currentLocale())

/** The status's own name in the reader's language, never the enum. */
@Composable
@ReadOnlyComposable
internal fun statusName(status: String): String = when (status.uppercase()) {
    "PRESENT" -> stringResource(R.string.attendance_status_present)
    "ABSENT" -> stringResource(R.string.attendance_status_absent)
    "LATE" -> stringResource(R.string.attendance_status_late)
    "EXCUSED" -> stringResource(R.string.attendance_status_excused)
    "SICK" -> stringResource(R.string.attendance_status_sick)
    "HOLIDAY" -> stringResource(R.string.attendance_status_holiday)
    else -> status
}
