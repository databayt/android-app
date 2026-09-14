package org.hogwarts.android.feature.notifications.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class NotificationFormatTest {

    private val now = Instant.parse("2026-09-14T12:00:00Z")
    private fun before(d: Duration) = now.minus(d)

    @Test
    fun `relative time buckets match date-fns`() {
        assertEquals(Ago.LessThanMinute, ago(before(Duration.ofSeconds(20)), now))
        assertEquals(Ago.Minutes(1), ago(before(Duration.ofSeconds(40)), now))
        assertEquals(Ago.Minutes(44), ago(before(Duration.ofMinutes(44)), now))
        assertEquals(Ago.Hours(1), ago(before(Duration.ofMinutes(45)), now))
        assertEquals(Ago.Hours(3), ago(before(Duration.ofMinutes(170)), now))
        assertEquals(Ago.Days(1), ago(before(Duration.ofHours(30)), now))
        assertEquals(Ago.Days(6), ago(before(Duration.ofDays(6)), now))
        assertEquals(Ago.Date, ago(before(Duration.ofDays(7)), now))
    }

    @Test
    fun `older dates read day-first in Arabic with Latin digits`() {
        val date = Instant.parse("2026-07-20T09:00:00Z")
        assertEquals("Jul 20, 2026", notificationDate(date, "en", ZoneOffset.UTC))
        assertEquals("20 يوليو 2026", notificationDate(date, "ar", ZoneOffset.UTC))
    }

    @Test
    fun `links reduce to a path on our hosts and leave for others`() {
        assertEquals(NotificationTarget.Path("/assignments/a-1"), notificationTarget("/assignments/a-1"))
        assertEquals(NotificationTarget.Path("/finance/invoice?id=i-1"), notificationTarget("/ar/finance/invoice?id=i-1"))
        assertEquals(NotificationTarget.Path("/en-route"), notificationTarget("/en-route"))
        assertEquals(NotificationTarget.Path("/attendance"), notificationTarget("https://demo.databayt.org/en/attendance"))
        assertEquals(NotificationTarget.Path("/"), notificationTarget("https://school.balqalam.com/ar"))
        assertEquals(NotificationTarget.External("https://pay.example.com/c/1"), notificationTarget("https://pay.example.com/c/1"))
        assertNull(notificationTarget(null))
        assertNull(notificationTarget("  "))
    }
}
