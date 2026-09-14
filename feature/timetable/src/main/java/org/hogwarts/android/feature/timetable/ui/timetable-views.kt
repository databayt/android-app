package org.hogwarts.android.feature.timetable.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.core.designsystem.kit.ChoiceChip
import org.hogwarts.android.core.designsystem.kit.ChoiceChipRow
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.timetable.R
import org.hogwarts.android.feature.timetable.domain.model.GridMode
import org.hogwarts.android.feature.timetable.domain.model.JoinTarget
import org.hogwarts.android.feature.timetable.domain.model.NowKind
import org.hogwarts.android.feature.timetable.domain.model.RangeMode
import org.hogwarts.android.feature.timetable.domain.model.WeekTimetable
import org.hogwarts.android.feature.timetable.domain.model.currentOrNext
import org.hogwarts.android.feature.timetable.domain.model.visibleDays
import org.hogwarts.android.feature.timetable.domain.model.workload
import org.hogwarts.android.feature.timetable.ui.components.ClosureNotice
import org.hogwarts.android.feature.timetable.ui.components.DayRows
import org.hogwarts.android.feature.timetable.ui.components.EmptyDay
import org.hogwarts.android.feature.timetable.ui.components.GridSkeleton
import org.hogwarts.android.feature.timetable.ui.components.NowCard
import org.hogwarts.android.feature.timetable.ui.components.TimetableGrid

/**
 * `student-view.tsx`: the closure notice, the Today/Week toggle (the phone
 * leads with the single day), and the grid. Day mode is today, or the next
 * working day when the school is shut today.
 */
@Composable
internal fun StudentView(state: TimetableUiState, week: WeekTimetable, actions: TimetableActions) {
    ClosureNotice(week.closure)
    if (week.slots.isEmpty()) {
        Text(
            stringResource(R.string.timetable_no_data),
            style = HogwartsTheme.type.body,
            color = HogwartsTheme.colors.mutedForeground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        )
        return
    }
    ChoiceChipRow {
        // The leading half is what the toggle opens on: the day, on a phone.
        listOf(RangeMode.Day to R.string.timetable_range_day, RangeMode.Week to R.string.timetable_range_week).forEach { (mode, label) ->
            ChoiceChip(label = stringResource(label), selected = state.range == mode, onClick = { actions.onPickRange(mode) })
        }
    }
    Spacer(Modifier.height(32.dp))
    TimetableGrid(
        week = week,
        days = visibleDays(state.range, week.workingDays, week.today),
        mode = GridMode.Class,
        // Only meaningful when there are other days to contrast against.
        highlightToday = state.range == RangeMode.Week,
        nowMinutes = state.nowMinutes,
        onInspect = { actions.onInspect(it.id) },
    )
}

/**
 * `teacher-view.tsx`: the teaching load, the closure notice, the class now or
 * next, the classroom and subject filters, then the day's rows (Today) or the
 * week grid (Week View).
 */
@Composable
internal fun TeacherView(
    state: TimetableUiState,
    week: WeekTimetable,
    actions: TimetableActions,
    onJoin: (JoinTarget) -> Unit,
) {
    val load = workload(week.slots)
    StatPanel(
        title = stringResource(R.string.timetable_teacher_title),
        description = state.userName,
        columns = 3,
        items = listOf(
            StatItem("days", stringResource(R.string.timetable_days_per_week), load.daysPerWeek.toString()),
            StatItem("periods", stringResource(R.string.timetable_periods_per_week), load.periodsPerWeek.toString()),
            StatItem("classes", stringResource(R.string.timetable_classes), load.classes.toString()),
        ),
    )
    ClosureNotice(week.closure)

    val today = week.rowsFor(week.today)
    if (state.tab == TimetableTab.Today) {
        currentOrNext(today, state.nowMinutes)?.let { now ->
            NowCard(
                now = now,
                label = stringResource(if (now.kind == NowKind.Current) R.string.timetable_currently_teaching else R.string.timetable_next_class),
                teacherView = true,
                nowMinutes = state.nowMinutes,
                onJoin = onJoin,
            )
        }
    }

    // Filters appear once there is more than one of either to choose between.
    val classrooms = week.slots.mapNotNull { it.section }.distinct().sorted()
    val subjects = week.slots.mapNotNull { it.subject }.distinct().sorted()
    if (classrooms.size > 1) {
        FilterRow(stringResource(R.string.timetable_all_classrooms), classrooms, state.classroomFilter, actions.onFilterClassroom)
    }
    if (subjects.size > 1) {
        FilterRow(stringResource(R.string.timetable_all_subjects), subjects, state.subjectFilter, actions.onFilterSubject)
    }
    val filtered = week.filtered(state.classroomFilter, state.subjectFilter)

    if (state.tab == TimetableTab.Today) {
        val rows = today.filter { row ->
            row.isBreak || ((state.classroomFilter == null || row.className == state.classroomFilter) &&
                (state.subjectFilter == null || row.subject == state.subjectFilter))
        }
        if (rows.none { it.isClass }) {
            val dayName = stringArrayResource(R.array.timetable_day_names).getOrElse(week.today) { "" }
            EmptyDay(stringResource(R.string.timetable_no_classes_day, dayName))
        } else {
            DayRows(rows = rows, teacherView = true, nowMinutes = state.nowMinutes, onJoin = onJoin)
        }
    } else {
        TimetableGrid(
            week = filtered,
            days = week.workingDays,
            mode = GridMode.Teacher,
            highlightToday = false,
            nowMinutes = state.nowMinutes,
            onInspect = { actions.onInspect(it.id) },
        )
    }
}

/**
 * `guardian-view.tsx`: the child picker, the chosen child, the class the child
 * is in (or goes to next), then the day's rows or the week grid.
 */
@Composable
internal fun GuardianView(state: TimetableUiState, actions: TimetableActions, onJoin: (JoinTarget) -> Unit) {
    if (state.children.isEmpty()) {
        if (state.isLoading) GridSkeleton() else FormAlert(stringResource(R.string.timetable_no_children))
        return
    }
    Column {
        SectionHeader(
            title = stringResource(R.string.timetable_guardian_title),
            description = stringResource(R.string.timetable_guardian_description),
        )
        ChoiceChipRow {
            state.children.forEach { child ->
                ChoiceChip(label = child.name, selected = child.id == state.selectedChildId, onClick = { actions.onSelectChild(child.id) })
            }
        }
    }
    val child = state.selectedChild
    if (child != null) {
        ListRow(
            title = child.name,
            description = child.grade,
            leading = { UserAvatar(name = child.name, imageUrl = child.photoUrl, size = 56.dp) },
        )
    }
    val week = state.week
    if (week == null) {
        GridSkeleton()
        return
    }
    ClosureNotice(week.closure)
    val today = week.rowsFor(week.today)
    if (state.tab == TimetableTab.Today) {
        currentOrNext(today, state.nowMinutes)?.let { now ->
            val name = child?.name.orEmpty()
            NowCard(
                now = now,
                label = stringResource(if (now.kind == NowKind.Current) R.string.timetable_child_is_in else R.string.timetable_child_next_class, name),
                teacherView = false,
                nowMinutes = state.nowMinutes,
                onJoin = onJoin,
            )
        }
        if (today.none { it.isClass }) {
            EmptyDay(stringResource(R.string.timetable_no_classes_today))
        } else {
            DayRows(rows = today, teacherView = false, nowMinutes = state.nowMinutes, onJoin = onJoin)
        }
    } else {
        TimetableGrid(
            week = week,
            days = week.workingDays,
            mode = GridMode.Class,
            highlightToday = false,
            nowMinutes = state.nowMinutes,
            onInspect = { actions.onInspect(it.id) },
        )
    }
}

/**
 * `admin-view.tsx` picks a classroom or a teacher and edits its grid. No mobile
 * route reads a classroom's or another teacher's week, and every edit is a
 * web server action, so the page hands the admin to the web.
 */
@Composable
internal fun AdminView(onOpenHref: (String) -> Unit) {
    Text(
        stringResource(R.string.timetable_admin_on_web),
        style = HogwartsTheme.type.body,
        color = HogwartsTheme.colors.mutedForeground,
    )
    PillButton(stringResource(R.string.timetable_open_on_web), onClick = { onOpenHref("/timetable") }, variant = PillVariant.Muted)
}

@Composable
private fun FilterRow(allLabel: String, options: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    ChoiceChipRow {
        ChoiceChip(label = allLabel, selected = selected == null, onClick = { onSelect(null) })
        options.forEach { option ->
            ChoiceChip(label = option, selected = selected == option, onClick = { onSelect(option) }, labelMaxWidth = 144.dp)
        }
    }
}

private fun WeekTimetable.filtered(classroom: String?, subject: String?): WeekTimetable {
    if (classroom == null && subject == null) return this
    return copy(slots = slots.filter { (classroom == null || it.section == classroom) && (subject == null || it.subject == subject) })
}
