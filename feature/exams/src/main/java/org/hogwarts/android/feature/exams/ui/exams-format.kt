package org.hogwarts.android.feature.exams.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.feature.exams.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Dates the way the web's `formatDate(date, lang, …)` prints them (`Intl`,
 * Gregorian, Eastern Arabic digits under `ar`). Counts, percentages and
 * scores stay Latin: the exams components interpolate raw numbers
 * (`{result.percentage.toFixed(0)}%`), which never localises digits.
 */
internal class ExamsFormat(locale: Locale, private val zone: ZoneId = ZoneId.systemDefault()) {
    private val arabic = locale.language == "ar"
    private val dateLocale: Locale = if (arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
    private val digits = DecimalStyle.of(if (arabic) Locale.forLanguageTag("ar-u-nu-arab") else Locale.ENGLISH)

    fun date(instant: Instant): LocalDate = instant.atZone(zone).toLocalDate()

    /** `{ weekday: "short" }` — "Mon" / "الاثنين". */
    fun weekday(date: LocalDate): String = date.format(pattern("EEE"))

    /** `{ day: "numeric" }` — "14" / "١٤". */
    fun day(date: LocalDate): String = date.format(pattern("d"))

    /** `{ day: "numeric", month: "long" }` — "September 14" / "١٤ سبتمبر". */
    fun dayMonth(date: LocalDate): String = date.format(pattern(if (arabic) "d MMMM" else "MMMM d"))

    /** `{ day: "numeric", month: "short", year: "numeric" }` — "Sep 14, 2026" / "١٤ سبتمبر ٢٠٢٦". */
    fun shortDate(date: LocalDate): String = date.format(pattern(if (arabic) "d MMM y" else "MMM d, y"))

    /** `{ weekday: "short", month: "short", day: "numeric", year: "numeric" }`. */
    fun weekdayDate(date: LocalDate): String = date.format(pattern(if (arabic) "EEE، d MMM y" else "EEE, MMM d, y"))

    /** `{ month: "short", day: "numeric" }`. */
    fun monthDay(date: LocalDate): String = date.format(pattern(if (arabic) "d MMM" else "MMM d"))

    private fun pattern(p: String) = DateTimeFormatter.ofPattern(p, dateLocale).withDecimalStyle(digits)
}

@Composable
@ReadOnlyComposable
internal fun examsFormat(): ExamsFormat = ExamsFormat(currentLocale())

/** Left-to-right isolate for times, scores and codes inside Arabic text. */
internal fun ltr(text: String): String = "\u2066$text\u2069"

/** `${percentage.toFixed(0)}%`. */
internal fun percentLabel(value: Double): String = "${value.roundToInt()}%"

/** A mark as JS prints a number: no trailing ".0". */
internal fun mark(value: Double?): String = when {
    value == null -> "—"
    value % 1.0 == 0.0 -> value.toLong().toString()
    else -> value.toString()
}

/** `results.examsHome`: today / tomorrow / "{count} days". */
@Composable
@ReadOnlyComposable
internal fun whenLabel(daysUntil: Int, tomorrowWord: Boolean = true): String = when {
    daysUntil == 0 -> stringResource(R.string.exams_home_today)
    daysUntil == 1 && tomorrowWord -> stringResource(R.string.exams_home_tomorrow)
    else -> stringResource(R.string.exams_home_days_count, daysUntil.toString())
}
