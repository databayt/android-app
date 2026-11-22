package org.hogwarts.android.core.common.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SchoolClockTest {

    private val monday = Instant.parse("2026-11-23T14:05:00Z")

    private fun khartoum(at: Instant = monday) = SchoolZone("Africa/Khartoum") { at }

    @Test
    fun `the clock tells the school's time and weekday`() {
        val zone = khartoum()
        assertEquals(LocalDateTime.of(2026, 11, 23, 16, 5), LocalDateTime.now(zone.clock()))
        assertEquals(1, zone.weekday())
    }

    @Test
    fun `the weekday follows the school's midnight, not UTC's`() {
        assertEquals(1, khartoum(Instant.parse("2026-11-22T22:30:00Z")).weekday())
        assertEquals(0, khartoum(Instant.parse("2026-11-22T12:30:00Z")).weekday())
    }

    @Test
    fun `a zone from the server overrides the brand`() {
        val zone = khartoum()
        zone.accept("Asia/Riyadh")
        assertEquals(LocalDateTime.of(2026, 11, 23, 17, 5), LocalDateTime.now(zone.clock()))
    }

    @Test
    fun `a clock made before the server answered follows the later zone`() {
        val zone = khartoum()
        val clock = zone.clock()
        zone.accept("Asia/Riyadh")
        assertEquals(ZoneId.of("Asia/Riyadh"), clock.zone)
    }

    @Test
    fun `blank null and unknown server zones change nothing`() {
        val zone = khartoum()
        zone.accept("")
        zone.accept(null)
        zone.accept("Mars/X")
        assertEquals(ZoneId.of("Africa/Khartoum"), zone.id)
    }

    @Test
    fun `no brand zone falls back to the device zone`() {
        assertEquals(ZoneId.systemDefault(), SchoolZone("").id)
    }

    @Test
    fun `an invalid brand zone falls back to the device zone without throwing`() {
        assertEquals(ZoneId.systemDefault(), SchoolZone("Not/AZone").id)
    }
}
