package org.hogwarts.android.feature.timetable.domain

import org.hogwarts.android.feature.timetable.domain.model.DayRow
import org.hogwarts.android.feature.timetable.domain.model.GridMode
import org.hogwarts.android.feature.timetable.domain.model.LiveClass
import org.hogwarts.android.feature.timetable.domain.model.JoinTarget
import org.hogwarts.android.feature.timetable.domain.model.LiveStatus
import org.hogwarts.android.feature.timetable.domain.model.NowKind
import org.hogwarts.android.feature.timetable.domain.model.Period
import org.hogwarts.android.feature.timetable.domain.model.RangeMode
import org.hogwarts.android.feature.timetable.domain.model.WallTime
import org.hogwarts.android.feature.timetable.domain.model.activePeriodKey
import org.hogwarts.android.feature.timetable.domain.model.breaksBeforePeriod
import org.hogwarts.android.feature.timetable.domain.model.cellText
import org.hogwarts.android.feature.timetable.domain.model.currentOrNext
import org.hogwarts.android.feature.timetable.domain.model.isRowJoinable
import org.hogwarts.android.feature.timetable.domain.model.liveStatus
import org.hogwarts.android.feature.timetable.domain.model.periodNumber
import org.hogwarts.android.feature.timetable.domain.model.subjectColorIndex
import org.hogwarts.android.feature.timetable.domain.model.toWeek
import org.hogwarts.android.feature.timetable.domain.model.visibleDays
import org.hogwarts.android.feature.timetable.domain.model.workload
import org.hogwarts.android.feature.timetable.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimetableRulesTest {

    private fun wt(hhmm: String) = WallTime.parse(hhmm)!!
    private fun period(name: String, start: String, end: String, isBreak: Boolean = false) =
        Period("$start-$end", name, wt(start), wt(end), isBreak)

    @Test
    fun `period times are wall clock, read without a time zone`() {
        assertEquals("07:15", WallTime.parse("1970-01-01T07:15:00.000Z")!!.hhmm)
        assertEquals("23:50", WallTime.parse("1970-01-01T23:50:00.000Z")!!.hhmm)
        assertEquals(8 * 60 + 5, WallTime.parse("08:05")!!.minutes)
        assertNull(WallTime.parse(null))
        assertNull(WallTime.parse("soon"))
    }

    @Test
    fun `day mode falls forward to the next working day`() {
        val week = listOf(0, 1, 2, 3, 4)
        assertEquals(listOf(2), visibleDays(RangeMode.Day, week, today = 2))
        assertEquals(listOf(0), visibleDays(RangeMode.Day, week, today = 5)) // Friday → Sunday
        assertEquals(listOf(0), visibleDays(RangeMode.Day, week, today = 6)) // Saturday → Sunday
        assertEquals(week, visibleDays(RangeMode.Week, week, today = 5))
        assertEquals(emptyList<Int>(), visibleDays(RangeMode.Day, emptyList(), today = 1))
    }

    @Test
    fun `breaks sit before the teaching period they precede`() {
        val p1 = period("Period 1", "07:30", "08:15")
        val tea = period("Break", "08:15", "08:30", isBreak = true)
        val lunch = period("Lunch", "08:30", "08:40", isBreak = true)
        val p2 = period("Period 2", "08:40", "09:25")
        val map = breaksBeforePeriod(listOf(p2, lunch, p1, tea))
        assertEquals(listOf(tea, lunch), map[p2.key])
        assertNull(map[p1.key])
    }

    @Test
    fun `the active period is the one running, else the next`() {
        val periods = listOf(period("Period 1", "07:30", "08:15"), period("Period 2", "08:20", "09:05"))
        assertEquals(periods[0].key, activePeriodKey(periods, wt("07:45").minutes))
        assertEquals(periods[1].key, activePeriodKey(periods, wt("08:17").minutes))
        assertNull(activePeriodKey(periods, wt("12:00").minutes))
    }

    @Test
    fun `lamp and join window follow the web`() {
        val start = wt("09:00")
        val end = wt("09:45")
        assertEquals(LiveStatus.Upcoming, liveStatus(start, end, wt("08:30").minutes))
        assertEquals(LiveStatus.Live, liveStatus(start, end, wt("09:10").minutes))
        assertEquals(LiveStatus.Missed, liveStatus(start, end, wt("09:45").minutes))
        assertEquals(true, isRowJoinable(start, end, wt("08:52").minutes))
        assertEquals(false, isRowJoinable(start, end, wt("08:45").minutes))
        assertEquals(true, isRowJoinable(start, end, wt("09:30").minutes))
    }

    @Test
    fun `current or next skips breaks and free periods`() {
        fun row(start: String, end: String, id: String?, isBreak: Boolean = false) =
            DayRow("P", wt(start), wt(end), isBreak, "Maths", null, null, null, id, null)
        val rows = listOf(row("07:30", "08:15", "a"), row("08:15", "08:30", null, isBreak = true), row("08:30", "09:15", null), row("09:15", "10:00", "b"))
        assertEquals(NowKind.Current, currentOrNext(rows, wt("07:40").minutes)?.kind)
        val next = currentOrNext(rows, wt("08:20").minutes)!!
        assertEquals(NowKind.Next, next.kind)
        assertEquals("b", next.row.timetableId)
        assertNull(currentOrNext(rows, wt("11:00").minutes))
    }

    @Test
    fun `a subject gets the same palette colour as on the web`() {
        // Values computed with the web's getSubjectColorIndex.
        assertEquals(3, subjectColorIndex("Mathematics"))
        assertEquals(4, subjectColorIndex("الرياضيات"))
        assertEquals(0, subjectColorIndex("Science"))
        assertEquals(2, subjectColorIndex("اللغة العربية"))
        assertEquals(0, subjectColorIndex(""))
    }

    @Test
    fun `join goes to the external room or the web live room`() {
        assertEquals(JoinTarget.Url("https://meet.example/abc"), LiveClass(null, "external", "https://meet.example/abc").joinTarget)
        assertEquals(JoinTarget.Href("/live/s-1/room"), LiveClass("s-1", "livekit", null).joinTarget)
        assertNull(LiveClass(null, "external", null).joinTarget)
    }

    @Test
    fun `the week takes every period and break from the day`() {
        val week = Fixtures.studentWeek(arabic = false, today = 1).toWeek(deviceToday = 5)
        assertEquals(listOf("Period 1", "Period 2", "Break", "Period 3"), week.periods.map { it.name })
        assertEquals(listOf(0, 1, 2, 3, 4), week.workingDays)
        assertEquals(1, week.today)
        assertEquals("Mathematics", week.slotAt(0, week.periods.first().key)?.subject)
        assertEquals(4, week.rowsFor(1).size)
        // Another day is laid out from the week, with the free period empty.
        assertNull(week.rowsFor(3).last().timetableId)
    }

    @Test
    fun `without the day the rows come from the slots and today is the device's`() {
        val bundle = Fixtures.studentWeek(arabic = false).copy(today = null)
        val week = bundle.toWeek(deviceToday = 4)
        assertEquals(3, week.periods.size)
        assertEquals(4, week.today)
        assertNull(week.closure)
    }

    @Test
    fun `cells and labels`() {
        val slot = Fixtures.studentWeek(arabic = false).toWeek(1).slots.first()
        assertEquals("Mathematics", cellText(slot, GridMode.Class).primary)
        assertEquals("Ms. Rowan", cellText(slot, GridMode.Class).secondary)
        assertEquals("Grade 5 A", cellText(slot, GridMode.Teacher).primary)
        assertEquals("B12", cellText(slot, GridMode.Teacher).secondary)
        assertEquals("3", periodNumber("Period 3"))
        assertEquals("الأولى", periodNumber("الأولى"))
    }

    @Test
    fun `workload counts days, periods and sections`() {
        val slots = Fixtures.teacherWeek(arabic = false).toWeek(1).slots
        val load = workload(slots)
        assertEquals(5, load.daysPerWeek)
        assertEquals(slots.size, load.periodsPerWeek)
        assertEquals(2, load.classes)
    }
}
