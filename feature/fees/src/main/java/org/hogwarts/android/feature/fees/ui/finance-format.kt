package org.hogwarts.android.feature.fees.ui

import android.icu.text.ListFormatter
import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.icu.util.ULocale
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import org.hogwarts.android.core.designsystem.locale.currentLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

/**
 * Money, dates and name lists the way the two web finance surfaces print them.
 *
 * - The family surface uses `formatCurrency` / `formatDate` from
 *   `lib/i18n-format.ts`: Eastern Arabic digits under `ar`, the currency's own
 *   minor units (`lib/payment/currency.ts`), Gregorian dates.
 * - The staff hub uses `formatCompactMoney` / `formatMoney` from
 *   `finance/lib/format-money.ts`, which force Latin digits in both languages
 *   and abbreviate from 10,000 up.
 */
internal class FinanceFormat(locale: Locale) {
    val arabic = locale.language == "ar"
    val lang: String = if (arabic) "ar" else "en"

    private val familyLocale = ULocale(if (arabic) "ar@numbers=arab" else "en")
    private val staffLocale = ULocale(if (arabic) "ar-SA@numbers=latn" else "en-US")
    private val dateLocale: Locale = if (arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
    private val digits = DecimalStyle.of(if (arabic) Locale.forLanguageTag("ar-u-nu-arab") else Locale.ENGLISH)

    /** `formatCurrency(value, lang, currency)`. */
    fun money(value: Double, currency: String): String = runCatching {
        val places = decimalPlaces(currency)
        NumberFormat.getCurrencyInstance(familyLocale).apply {
            this.currency = Currency.getInstance(currency.uppercase())
            minimumFractionDigits = places
            maximumFractionDigits = places
            roundingMode = HALF_EXPAND
        }.format(value)
    }.getOrElse { "%.2f %s".format(Locale.ENGLISH, value, currency) }

    /**
     * `formatCompactMoney` without its currency symbol: the dashboard route the
     * staff figures come from carries no `currency`. Below 10,000 the full
     * figure; from there Intl's compact notation ("48k", "48 ألف"), which needs
     * `android.icu.number` (API 30) — older devices keep the full figure.
     */
    fun compact(value: Double): String {
        if (kotlin.math.abs(value) >= COMPACT_THRESHOLD && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val text = android.icu.number.NumberFormatter.withLocale(staffLocale)
                .notation(android.icu.number.Notation.compactShort())
                .precision(android.icu.number.Precision.maxFraction(1))
                .roundingMode(java.math.RoundingMode.HALF_UP)
                .format(value)
                .toString()
            // The house style is a lowercase Latin suffix; a translated word stays as written.
            val cased = text.replace(Regex("(?<=\\d)([KMBT])\\b")) { it.value.lowercase() }
            // Intl leads an Arabic compact figure with RLM ("\u200F48 ألف"); ICU does not.
            return if (arabic && !cased.startsWith(RLM)) RLM + cased.replace('\u00A0', ' ') else cased
        }
        return NumberFormat.getNumberInstance(staffLocale).apply {
            maximumFractionDigits = 0
            roundingMode = HALF_EXPAND
        }.format(value)
    }

    /** A count the web renders straight into JSX: Latin digits in both languages. */
    fun latin(value: Int): String = value.toString()

    /** `{ day: "numeric", month: "long", year: "numeric" }` — "October 1, 2026". */
    fun longDate(isoDay: String): String = date(isoDay, if (arabic) "d MMMM y" else "MMMM d, y")

    /** `{ day: "numeric", month: "short", year: "numeric" }` — "Oct 1, 2026". */
    fun shortDate(isoDay: String): String = date(isoDay, if (arabic) "d MMMM y" else "MMM d, y")

    private fun date(isoDay: String, pattern: String): String = runCatching {
        LocalDate.parse(isoDay.take(10)).format(DateTimeFormatter.ofPattern(pattern, dateLocale).withDecimalStyle(digits))
    }.getOrDefault("")

    /** `Intl.ListFormat(lang, { style: "long", type: "conjunction" })`. */
    fun names(names: List<String>): String = when (names.size) {
        0 -> ""
        1 -> names.first()
        else -> {
            // Each name keeps its own direction (a Latin name inside the Arabic "و" list).
            val isolated = if (arabic) names.map { "\u2068$it\u2069" } else names
            runCatching { ListFormatter.getInstance(ULocale(lang)).format(isolated) }
                .getOrElse { isolated.joinToString(if (arabic) "، " else ", ") }
        }
    }

    companion object {
        private const val COMPACT_THRESHOLD = 10_000.0
        private const val RLM = "\u200F"
        /** Intl rounds half away from zero ("halfExpand"); ICU defaults to half-even. */
        private const val HALF_EXPAND = java.math.BigDecimal.ROUND_HALF_UP
        private val THREE_DECIMAL = setOf("BHD", "IQD", "JOD", "KWD", "LYD", "OMR", "TND")
        private val ZERO_DECIMAL = setOf(
            "BIF", "CLP", "DJF", "GNF", "JPY", "KMF", "KRW", "MGA", "PYG", "RWF", "UGX", "VND", "VUV", "XAF", "XOF", "XPF",
        )

        /** `getDecimalPlaces()` in `lib/payment/currency.ts`. */
        fun decimalPlaces(currency: String): Int = when (currency.uppercase()) {
            in ZERO_DECIMAL -> 0
            in THREE_DECIMAL -> 3
            else -> 2
        }
    }
}

/** Left-to-right isolate for codes (invoice and payment numbers) inside Arabic text. */
internal fun ltr(text: String): String = "\u2066$text\u2069"

@Composable
@ReadOnlyComposable
internal fun financeFormat(): FinanceFormat = FinanceFormat(currentLocale())
