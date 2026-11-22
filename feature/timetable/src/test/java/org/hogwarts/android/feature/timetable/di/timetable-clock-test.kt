package org.hogwarts.android.feature.timetable.di

import org.hogwarts.android.core.common.time.SchoolZone
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TimetableClockTest {

    @Test
    fun `the timetable clock tells the school's zone`() {
        val zone = SchoolZone("Africa/Khartoum") { Instant.parse("2026-11-23T14:05:00Z") }
        val clock = TimetableModule.provideClock(zone)
        assertEquals(ZoneId.of("Africa/Khartoum"), clock.zone)
        assertEquals(Instant.parse("2026-11-23T14:05:00Z"), clock.instant())
    }
}
