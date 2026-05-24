package org.hogwarts.android.core.common.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocaleFormatterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val formatter = LocaleFormatter(context)

    @After
    fun resetLocales() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    @Test
    fun `formatNumber under ar returns Eastern Arabic digits`() {
        val out = formatter.formatNumber(1234, Locale("ar"))
        // Eastern Arabic digit '1' is U+0661.
        assertTrue("Expected Arabic-Indic digits in $out", out.any { it in '٠'..'٩' })
    }

    @Test
    fun `formatNumber under en uses ASCII digits and a comma group`() {
        val out = formatter.formatNumber(1234, Locale.ENGLISH)
        assertEquals("1,234", out)
    }

    @Test
    fun `formatRelativeDate today resolves the today resource`() {
        val out = formatter.formatRelativeDate(LocalDate.now(), Locale.ENGLISH)
        assertEquals("Today", out)
    }

    @Test
    fun `formatRelativeDate yesterday resolves the yesterday resource`() {
        val out = formatter.formatRelativeDate(LocalDate.now().minusDays(1), Locale.ENGLISH)
        assertEquals("Yesterday", out)
    }

    @Test
    fun `formatCurrency under en SAR contains the amount and currency code`() {
        val out = formatter.formatCurrency(1500.00, "SAR", Locale.ENGLISH)
        assertTrue("Expected '1,500.00' inside $out", out.contains("1,500.00"))
        assertTrue("Expected 'SAR' inside $out", out.contains("SAR"))
    }

    @Test
    fun `formatTime emits hours and minutes for the locale`() {
        val out = formatter.formatTime(LocalTime.of(14, 30), Locale.ENGLISH)
        // Don't pin 12 vs 24h — just assert digits appear; the device default
        // governs the AM/PM suffix and that's the behavior we want.
        assertTrue("Expected digits in $out", out.any { it.isDigit() })
    }

    @Test
    fun `unicodeWrap inserts bidi marks around mixed text in rtl context`() {
        val wrapped = formatter.unicodeWrap("ID: ABC-123", isRtlContext = true)
        // BidiFormatter inserts U+200F (RLM) when wrapping LTR data into an RTL context.
        assertTrue("Expected RLM marker in $wrapped", wrapped.any { it == '‏' })
    }

    @Test
    fun `current locale source follows AppCompatDelegate`() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
        val out = formatter.formatNumber(7)
        // When AppCompatDelegate has 'ar' set, the no-arg current() path should
        // emit Arabic-Indic digits.
        assertTrue("Expected Arabic digit in $out", out.any { it in '٠'..'٩' })
    }
}
