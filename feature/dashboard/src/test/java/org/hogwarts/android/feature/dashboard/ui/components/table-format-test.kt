package org.hogwarts.android.feature.dashboard.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The two billing tables print numbers the way the WEB prints them, which is
 * not the way the rest of this app does. Pinned here because the difference is
 * deliberate and reads like a bug to anyone who meets it cold.
 */
class TableFormatTest {

    @Test
    fun `the tables use latin digits with thousands separators`() {
        val f = tableNumberFormat()
        // `new Intl.NumberFormat()` on an en runtime, which is what the web's
        // `DetailedUsageTable` resolves to even on the Arabic dashboard.
        assertEquals("3,111", f.format(3111))
        assertEquals("98", f.format(98))
        assertEquals("3.2", f.format(3.2))
    }

    @Test
    fun `every amount carries a dollar sign and two decimals`() {
        // `$${row.amount.toFixed(2)}` — the currency code beside it is ignored,
        // exactly as the web ignores it.
        assertEquals("$320.00", invoiceAmount(320.0))
        assertEquals("$90.50", invoiceAmount(90.5))
        assertEquals("$1234.00", invoiceAmount(1234.0))
    }

    @Test
    fun `the invoice date is the arabic day-first form the web hardcodes`() {
        // `formatDate(row.date, "ar")` → "04‏/03‏/2031": Latin digits, day
        // first, a RLM after the day and after the month.
        assertEquals("04‏/03‏/2031", invoiceDate("2031-03-04T00:00:00.000Z"))
        assertEquals("18‏/02‏/2031", invoiceDate("2031-02-18T21:45:00.000Z"))
    }

    @Test
    fun `the date is read in UTC, never on the device clock`() {
        // The web formats on a server that runs in UTC. Late-evening instants
        // are where a device-local read would slide the invoice a day.
        assertEquals("31‏/12‏/2030", invoiceDate("2030-12-31T23:59:00.000Z"))
    }

    @Test
    fun `an unparseable date is shown as it arrived rather than guessed at`() {
        assertEquals("", invoiceDate(""))
        assertEquals("soon", invoiceDate("soon"))
    }
}
