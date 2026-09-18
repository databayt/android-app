package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.data.remote.PeriodDto
import org.hogwarts.android.feature.dashboard.data.remote.TodayTimetableDto
import org.hogwarts.android.feature.timetable.domain.model.GridMode
import org.hogwarts.android.feature.timetable.domain.model.Period
import org.hogwarts.android.feature.timetable.domain.model.Slot
import org.hogwarts.android.feature.timetable.domain.model.WallTime
import org.hogwarts.android.feature.timetable.domain.model.WeekTimetable
import org.hogwarts.android.feature.timetable.ui.components.TimetableGrid

/**
 * The day's classes on the phone dashboard — hogwarts `today-timetable.tsx`.
 *
 * It is /timetable's own day mode, not a second timetable: the SAME grid the
 * timetable screen renders, narrowed to one day column beside the period
 * column. Nothing here re-implements a cell, a break row or a time label; a
 * grid that drifted from the real one would be worse than no grid.
 *
 * WHO GETS IT is the whole design. The web returns a filtered day only for
 * STUDENT (their sections) and TEACHER (their slots); every other role would
 * get whichever class happened to sort first per period — an arbitrary class,
 * not "the school's day" — so an admin, an accountant or a staff member gets
 * nothing rather than a plausible-looking lie. [role] repeats that gate on the
 * phone even though the route already applies it.
 *
 * Best-effort like its neighbour, the next-action banner: a closed day or a
 * day with no classes renders nothing. The dashboard already opens with a
 * widget that says what today is; a second empty state under it would be the
 * same silence twice.
 */
@Composable
fun TodayTimetable(
    timetable: TodayTimetableDto?,
    role: UserRole,
    weekday: Int,
    onOpenTimetable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (role != UserRole.STUDENT && role != UserRole.TEACHER) return
    if (timetable == null || timetable.closure != null) return
    val week = remember(timetable, role) { timetable.toWeek(teacher = role == UserRole.TEACHER) } ?: return

    Column(modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(
                if (timetable.isTodayOr(weekday)) R.string.dash_today_classes else R.string.dash_next_classes,
            ),
            linkLabel = stringResource(R.string.dash_full_timetable),
            onLinkClick = onOpenTimetable,
        )
        TimetableGrid(
            week = week,
            days = listOf(timetable.dayOfWeek),
            mode = if (role == UserRole.TEACHER) GridMode.Teacher else GridMode.Class,
            // The single column IS the day, so the highlight has nothing to
            // contrast against and would only wash the colour out of it.
            highlightToday = false,
            // Deliberately inert. The web withholds `liveIndicators` from this
            // card: the live lamp is decided by comparing a wall clock against
            // period bounds, and joining a class stays on /timetable and /live
            // where the time gate lives. No slot here carries a live class, so
            // no lamp can light and the clock never matters.
            nowMinutes = -1,
        )
    }
}

/**
 * The route's day as the grid's week: every period is a row (breaks included),
 * and only a period carrying a timetable slot becomes a cell. Null when the
 * day has no classes on it at all — the web renders nothing in that case.
 */
private fun TodayTimetableDto.toWeek(teacher: Boolean): WeekTimetable? {
    val rows = periods.mapNotNull { dto -> dto.toPeriod()?.let { dto to it } }
    val slots = rows.mapNotNull { (dto, period) -> dto.toSlot(period, dayOfWeek, teacher) }
    if (slots.isEmpty()) return null
    return WeekTimetable(
        periods = rows.map { it.second },
        workingDays = listOf(dayOfWeek),
        slots = slots,
        today = dayOfWeek,
        closure = null,
        todayRows = null,
    )
}

private fun PeriodDto.toPeriod(): Period? {
    val start = WallTime.parse(startTime) ?: return null
    return Period(
        key = periodId,
        name = periodName.orEmpty(),
        start = start,
        end = WallTime.parse(endTime) ?: start,
        isBreak = isBreak,
    )
}

private fun PeriodDto.toSlot(period: Period, day: Int, teacher: Boolean): Slot? {
    val id = timetableId ?: return null
    if (isBreak) return null
    return Slot(
        id = id,
        day = day,
        periodKey = period.key,
        periodName = period.name,
        start = period.start,
        end = period.end,
        subject = subject,
        teacher = this.teacher,
        // The teacher cell reads "<subject> - <section>" over the room, which
        // is `getSlotDisplay`'s teacher branch once the dashboard hands it a
        // section name. The grid's teacher mode puts `section` on the first
        // line, so the composed line goes there — which also means the subject
        // colour hashes the same string the web colours by.
        section = if (teacher) listOfNotNull(subject, className).joinToString(" - ").ifEmpty { null } else null,
        room = room,
        liveClass = null,
    )
}
