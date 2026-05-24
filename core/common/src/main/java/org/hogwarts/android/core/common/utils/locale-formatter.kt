package org.hogwarts.android.core.common.utils

import android.content.Context
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.BidiFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import org.hogwarts.android.core.common.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Locale-aware formatting for dates, numbers, and currency.
 *
 * Patterns are derived from ICU "skeletons" via
 * [android.text.format.DateFormat.getBestDateTimePattern], so the same skeleton
 * (e.g. `yMMMd`) produces locale-correct output: `Mar 15, 2026` in `en`, `15 مارس
 * 2026` in `ar`, and Arabic-Indic digits when Arabic-numbering systems are active.
 *
 * The default [Locale] resolves through [AppCompatDelegate.getApplicationLocales]
 * so this stays in sync with the user's per-app language even when the system
 * locale differs.
 */
@Singleton
class LocaleFormatter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun current(): Locale =
        AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()

    private fun pattern(skeleton: String, locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            DateFormat.getBestDateTimePattern(locale, skeleton),
            locale
        )

    /** Medium date, e.g. "Mar 15, 2026" / "15 مارس 2026". */
    fun formatDate(date: LocalDate, locale: Locale = current()): String =
        date.format(pattern("yMMMd", locale))

    /** Numeric date, e.g. "3/15/26" / "15‏/3‏/26". */
    fun formatDateShort(date: LocalDate, locale: Locale = current()): String =
        date.format(pattern("yMd", locale))

    /** Long date with weekday, e.g. "Sunday, March 15, 2026". */
    fun formatDateLong(date: LocalDate, locale: Locale = current()): String =
        date.format(pattern("EEEEyMMMMd", locale))

    /** Time-of-day, respecting the device's 12/24-hour preference. */
    fun formatTime(time: LocalTime, locale: Locale = current()): String =
        time.format(pattern(if (DateFormat.is24HourFormat(context)) "Hm" else "hm", locale))

    /** Date + time, e.g. "Mar 15, 2026, 2:30 PM". */
    fun formatDateTime(dateTime: LocalDateTime, locale: Locale = current()): String =
        dateTime.format(
            pattern(if (DateFormat.is24HourFormat(context)) "yMMMdHm" else "yMMMdhm", locale)
        )

    /** "Today" / "Yesterday" / "N days ago" / fall back to [formatDate]. */
    fun formatRelativeDate(date: LocalDate, locale: Locale = current()): String {
        val days = ChronoUnit.DAYS.between(date, LocalDate.now())
        return when {
            days == 0L -> context.getString(R.string.locale_today)
            days == 1L -> context.getString(R.string.locale_yesterday)
            days in 2..6 -> context.resources.getQuantityString(
                R.plurals.locale_days_ago,
                days.toInt(),
                formatNumber(days, locale)
            )
            else -> formatDate(date, locale)
        }
    }

    /** Locale-grouped number, with Arabic-Indic digits under `ar`. */
    fun formatNumber(number: Number, locale: Locale = current()): String =
        NumberFormat.getInstance(locale).format(number)

    /** Percentage from a 0-100 input, e.g. 85.5 → "85.5%" / "٨٥٫٥٪". */
    fun formatPercentage(value: Double, locale: Locale = current()): String =
        NumberFormat.getPercentInstance(locale)
            .apply { maximumFractionDigits = 1 }
            .format(value / 100.0)

    /** Locale-correct currency, e.g. "SAR 1,500.00" / "١٬٥٠٠٫٠٠ ر.س.‏". */
    fun formatCurrency(
        amount: Double,
        currencyCode: String = "SAR",
        locale: Locale = current()
    ): String =
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }.format(amount)

    /**
     * Wrap mixed-direction text (e.g. "ID: ABC-123") with bidi marks so the
     * embedded LTR run doesn't reorder when surrounded by RTL content.
     */
    fun unicodeWrap(text: String, isRtlContext: Boolean = current().language == "ar"): String =
        BidiFormatter.getInstance(isRtlContext).unicodeWrap(text)
}
