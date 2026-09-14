package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Test

class HeadlineTest {

    private fun boldRanges(template: String?, mark: String): Pair<String, List<String>> {
        val text = headline(template, mark)
        val bold = text.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }.map { text.text.substring(it.start, it.end) }
        return text.text to bold
    }

    @Test
    fun `double-star segment is bold with the mark substituted`() {
        val (text, bold) = boldRanges("**{mark} invoices** are still unpaid", "3")
        assertEquals("3 invoices are still unpaid", text)
        assertEquals(listOf("3 invoices"), bold)
    }

    @Test
    fun `arabic template keeps its bold phrase`() {
        val (text, bold) = boldRanges("**حصة {mark}** هي القادمة", "الرياضيات")
        assertEquals("حصة الرياضيات هي القادمة", text)
        assertEquals(listOf("حصة الرياضيات"), bold)
    }

    @Test
    fun `template without stars bolds the text up to the mark`() {
        val (text, bold) = boldRanges("Next: {mark} today", "Science")
        assertEquals("Next: Science today", text)
        assertEquals(listOf("Next: Science"), bold)
    }

    @Test
    fun `unknown kind renders the mark alone in bold`() {
        assertEquals("Mark" to listOf("Mark"), boldRanges(null, "Mark"))
    }

    @Test
    fun `period time range reads ISO and plain times`() {
        val p = org.hogwarts.android.feature.dashboard.data.remote.PeriodDto(startTime = "1970-01-01T07:30:00.000Z", endTime = "08:15")
        assertEquals("\u206607:30–08:15\u2069", p.timeRange())
    }
}
