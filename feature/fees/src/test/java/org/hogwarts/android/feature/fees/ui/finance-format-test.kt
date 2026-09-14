package org.hogwarts.android.feature.fees.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

/** The web's formatters, byte for byte where the platform ICU agrees. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FinanceFormatTest {

    private val en = FinanceFormat(Locale.ENGLISH)
    private val ar = FinanceFormat(Locale.forLanguageTag("ar"))

    @Test
    fun `family money uses the currency's own minor units`() {
        assertEquals("SDG 100,600.00", en.money(100600.0, "SDG"))
        assertEquals("KWD 1,234.500", en.money(1234.5, "KWD"))
        assertEquals("¥1,235", en.money(1234.5, "JPY"))
    }

    @Test
    fun `family money in Arabic uses Eastern Arabic digits`() {
        val text = ar.money(1250.0, "SDG")
        assertTrue(text, text.contains("١٬٢٥٠٫٠٠"))
    }

    @Test
    fun `staff figures are Latin and compact from ten thousand`() {
        assertEquals("9,000", en.compact(9000.0))
        assertEquals("48k", en.compact(48000.0))
        assertEquals("10.6m", en.compact(10_593_000.0))
        assertEquals("\u200F48 ألف", ar.compact(48000.0))
    }

    @Test
    fun `dates are the stored day, Gregorian, in the reader's digits`() {
        assertEquals("October 1, 2026", en.longDate("2026-10-01T00:00:00.000Z"))
        assertEquals("Oct 1, 2026", en.shortDate("2026-10-01"))
        assertEquals("١ أكتوبر ٢٠٢٦", ar.longDate("2026-10-01"))
    }

    @Test
    fun `names join the way each language joins a list`() {
        assertEquals("Omar and Sara", en.names(listOf("Omar", "Sara")))
        assertEquals("Solo", en.names(listOf("Solo")))
    }
}
