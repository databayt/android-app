package org.hogwarts.android.feature.timetable.domain.model

import org.hogwarts.android.feature.timetable.data.remote.dto.ChildDto
import org.hogwarts.android.feature.timetable.data.remote.dto.LiveClassDto
import org.hogwarts.android.feature.timetable.data.remote.dto.SlotDto
import org.hogwarts.android.feature.timetable.data.remote.dto.TimetableBundle
import org.hogwarts.android.feature.timetable.data.remote.dto.TodayPeriodDto
import kotlin.math.abs

/**
 * A wall-clock time as minutes since midnight. Periods are stored as
 * `1970-01-01THH:mm:00.000Z` and mean the school's own clock, so the digits
 * are read as they are — never shifted through a time zone.
 */
@JvmInline
value class WallTime(val minutes: Int) : Comparable<WallTime> {
    val hhmm: String get() = "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"
    override fun compareTo(other: WallTime): Int = minutes.compareTo(other.minutes)

    companion object {
        private val HHMM = Regex("""(\d{1,2}):(\d{2})""")

        /** "1970-01-01T07:15:00.000Z" or "07:15" → 07:15; anything else → null. */
        fun parse(value: String?): WallTime? {
            if (value.isNullOrBlank()) return null
            val match = HHMM.find(value.substringAfter('T', value)) ?: return null
            val h = match.groupValues[1].toInt()
            val m = match.groupValues[2].toInt()
            return if (h in 0..23 && m in 0..59) WallTime(h * 60 + m) else null
        }
    }
}

/** One row of the school day. [key] ties week slots to it: "HH:mm-HH:mm". */
data class Period(val key: String, val name: String, val start: WallTime, val end: WallTime, val isBreak: Boolean)

/** Today's online delivery of a class — `LiveClassJoinInfo`. */
data class LiveClass(val sessionId: String?, val provider: String?, val meetingUrl: String?) {
    /** `LiveJoinButton`: an external room (or a standing link) opens its URL; a LiveKit session opens `/live/{id}/room`. */
    val joinTarget: JoinTarget?
        get() = if (provider == "external" || sessionId == null) {
            meetingUrl?.takeIf { it.isNotBlank() }?.let { JoinTarget.Url(it) }
        } else {
            JoinTarget.Href("/live/$sessionId/room")
        }

    /** A materialized session today — what the web's live indicator and "Online" badge key on. */
    val isOnlineToday: Boolean get() = sessionId != null
}

sealed interface JoinTarget {
    data class Url(val url: String) : JoinTarget
    data class Href(val href: String) : JoinTarget
}

data class Slot(
    val id: String,
    val day: Int,
    val periodKey: String,
    val periodName: String,
    val start: WallTime,
    val end: WallTime,
    val subject: String?,
    val teacher: String?,
    val section: String?,
    val room: String?,
    val liveClass: LiveClass?,
)

/** One period of one day — `getTodaySchedule`'s row, breaks and free periods included. */
data class DayRow(
    val periodName: String,
    val start: WallTime,
    val end: WallTime,
    val isBreak: Boolean,
    val subject: String?,
    val className: String?,
    val teacher: String?,
    val room: String?,
    val timetableId: String?,
    val liveClass: LiveClass?,
) {
    /** A period with a class in it; breaks and free periods are not. */
    val isClass: Boolean get() = !isBreak && timetableId != null
}

data class Closure(val title: String?)

/** Everything the role views lay out, built from what the mobile routes return. */
data class WeekTimetable(
    val periods: List<Period>,
    val workingDays: List<Int>,
    val slots: List<Slot>,
    /** The school's weekday (0 = Sunday). */
    val today: Int,
    val closure: Closure?,
    /** The server's day (student / teacher); null when only the week is known. */
    val todayRows: List<DayRow>?,
) {
    val teachingPeriods: List<Period> get() = periods.filter { !it.isBreak }

    fun slotAt(day: Int, periodKey: String): Slot? = slots.firstOrNull { it.day == day && it.periodKey == periodKey }

    /** The day as rows: the server's own for today, else laid out from the week. */
    fun rowsFor(day: Int): List<DayRow> {
        if (day == today && todayRows != null) return todayRows
        return periods.map { period ->
            val slot = if (period.isBreak) null else slotAt(day, period.key)
            DayRow(
                periodName = period.name,
                start = period.start,
                end = period.end,
                isBreak = period.isBreak,
                subject = slot?.subject,
                className = slot?.section,
                teacher = slot?.teacher,
                room = slot?.room,
                timetableId = slot?.id,
                liveClass = slot?.liveClass,
            )
        }
    }
}

data class Child(val id: String, val name: String, val grade: String?, val photoUrl: String? = null)

internal fun periodKey(start: WallTime, end: WallTime) = "${start.hhmm}-${end.hhmm}"

private fun LiveClassDto.toModel() = LiveClass(sessionId = sessionId, provider = provider, meetingUrl = meetingUrl)

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

internal fun SlotDto.toSlot(): Slot? {
    val start = WallTime.parse(startTime) ?: return null
    val end = WallTime.parse(endTime) ?: start
    return Slot(
        id = id,
        day = dayOfWeek,
        periodKey = periodKey(start, end),
        periodName = periodName.orEmpty(),
        start = start,
        end = end,
        subject = subjectName.clean(),
        teacher = teacherName.clean(),
        section = sectionName.clean(),
        room = classroom.clean(),
        liveClass = liveClass?.toModel(),
    )
}

private fun TodayPeriodDto.toRow(): DayRow? {
    val start = WallTime.parse(startTime) ?: return null
    return DayRow(
        periodName = periodName.orEmpty(),
        start = start,
        end = WallTime.parse(endTime) ?: start,
        isBreak = isBreak,
        subject = subject.clean(),
        className = className.clean(),
        teacher = teacher.clean(),
        room = room.clean(),
        timetableId = timetableId,
        liveClass = liveClass?.toModel(),
    )
}

fun ChildDto.toChild() = Child(
    id = id,
    name = listOfNotNull(givenName.clean(), familyName.clean()).joinToString(" "),
    grade = grade.clean() ?: section.clean(),
    photoUrl = photoUrl.clean(),
)

/**
 * Lay the week out from what the phone can read.
 *
 * Rows are the school's periods — every one, breaks included — when the day
 * came back with them (`getTodaySchedule` lists them all); otherwise only the
 * periods that carry a slot. Columns are the weekdays that carry a slot: no
 * mobile route returns the school's configured working days.
 */
fun TimetableBundle.toWeek(deviceToday: Int): WeekTimetable {
    val slots = slots.mapNotNull { it.toSlot() }
    val todayRows = today?.periods?.mapNotNull { it.toRow() }
    val periods = buildMap {
        todayRows?.forEach { row ->
            val key = periodKey(row.start, row.end)
            putIfAbsent(key, Period(key, row.periodName, row.start, row.end, row.isBreak))
        }
        slots.forEach { slot ->
            putIfAbsent(slot.periodKey, Period(slot.periodKey, slot.periodName, slot.start, slot.end, isBreak = false))
        }
    }.values.sortedWith(compareBy({ it.start }, { it.end }))
    return WeekTimetable(
        periods = periods,
        workingDays = slots.map { it.day }.filter { it in 0..6 }.distinct().sorted(),
        slots = slots,
        today = today?.dayOfWeek ?: deviceToday,
        closure = today?.closure?.let { Closure(it.title.clean()) },
        todayRows = todayRows,
    )
}

// ---------------------------------------------------------------------------
// Rules ported from the web views, kept pure for tests.
// ---------------------------------------------------------------------------

enum class RangeMode { Day, Week }

/**
 * `student-view.tsx`: day mode narrows the grid to today, and on a day the
 * school is shut (a Friday) falls forward to the next working day, which the
 * column header names for itself.
 */
fun visibleDays(range: RangeMode, workingDays: List<Int>, today: Int): List<Int> {
    if (range == RangeMode.Week || workingDays.isEmpty()) return workingDays
    if (today in workingDays) return listOf(today)
    for (offset in 1..7) {
        val candidate = (today + offset) % 7
        if (candidate in workingDays) return listOf(candidate)
    }
    return workingDays
}

/** `simple-grid.tsx`: every break sits before the teaching period it precedes, in time order. */
fun breaksBeforePeriod(periods: List<Period>): Map<String, List<Period>> {
    val map = LinkedHashMap<String, List<Period>>()
    var pending = mutableListOf<Period>()
    for (p in periods.sortedBy { it.start }) {
        if (p.isBreak) {
            pending.add(p)
            continue
        }
        if (pending.isNotEmpty()) {
            map[p.key] = pending
            pending = mutableListOf()
        }
    }
    return map
}

/** The period the reader is in, else the next to start — the one cell worth looking at. */
fun activePeriodKey(teachingPeriods: List<Period>, nowMinutes: Int): String? {
    var next: Period? = null
    for (p in teachingPeriods) {
        if (nowMinutes >= p.start.minutes && nowMinutes < p.end.minutes) return p.key
        if (p.start.minutes > nowMinutes && (next == null || p.start < next.start)) next = p
    }
    return next?.key
}

enum class LiveStatus { Live, Upcoming, Missed }

/** `liveSlotStatus`: which lamp is lit, for today's slots only. */
fun liveStatus(start: WallTime, end: WallTime, nowMinutes: Int): LiveStatus = when {
    nowMinutes >= end.minutes -> LiveStatus.Missed
    nowMinutes >= start.minutes -> LiveStatus.Live
    else -> LiveStatus.Upcoming
}

/** `isRowLiveJoinable`: in progress, or starting within [windowMinutes]. */
fun isRowJoinable(start: WallTime, end: WallTime, nowMinutes: Int, windowMinutes: Int = 10): Boolean {
    if (nowMinutes >= start.minutes && nowMinutes < end.minutes) return true
    val until = start.minutes - nowMinutes
    return until in 1..windowMinutes
}

enum class NowKind { Current, Next }

data class NowRow(val kind: NowKind, val row: DayRow)

/** `getCurrentClass`: the class running now, else the next one today. Breaks and free periods are skipped. */
fun currentOrNext(rows: List<DayRow>, nowMinutes: Int): NowRow? {
    for (row in rows) {
        if (!row.isClass) continue
        if (nowMinutes >= row.start.minutes && nowMinutes < row.end.minutes) return NowRow(NowKind.Current, row)
        if (nowMinutes < row.start.minutes) return NowRow(NowKind.Next, row)
    }
    return null
}

/**
 * `getSubjectColorIndex`: a 31-polynomial hash over the whole name, which is
 * exactly Java's `String.hashCode`, so a subject gets the web's colour.
 */
fun subjectColorIndex(name: String, paletteSize: Int = 5): Int {
    if (name.isEmpty()) return 0
    return (abs(name.hashCode().toLong()) % paletteSize).toInt()
}

enum class GridMode { Class, Teacher }

data class CellText(val primary: String, val secondary: String)

/** `getSlotDisplay`. The phone's slots name their section, the web's weekly ones do not; the web's lines are kept. */
fun cellText(slot: Slot, mode: GridMode): CellText = when (mode) {
    GridMode.Teacher -> CellText(primary = slot.section ?: slot.subject.orEmpty(), secondary = slot.room.orEmpty())
    GridMode.Class -> CellText(primary = slot.subject.orEmpty(), secondary = slot.teacher.orEmpty())
}

/** The period cell's label: stored names are "Period 1"; the dictionary word replaces the English prefix. */
fun periodNumber(name: String): String = name.replace(Regex("^period\\s+", RegexOption.IGNORE_CASE), "")

data class Workload(val daysPerWeek: Int, val periodsPerWeek: Int, val classes: Int)

fun workload(slots: List<Slot>) = Workload(
    daysPerWeek = slots.map { it.day }.distinct().size,
    periodsPerWeek = slots.size,
    classes = slots.map { it.section ?: it.subject }.distinct().size,
)
