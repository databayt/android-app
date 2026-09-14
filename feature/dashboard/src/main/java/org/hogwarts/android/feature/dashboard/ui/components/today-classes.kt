package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.TileFace
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.data.remote.PeriodDto
import org.hogwarts.android.feature.dashboard.data.remote.TodayTimetableDto

private val SUBJECT_TINTS = listOf(TileTint.Blue, TileTint.Green, TileTint.Orange, TileTint.Purple, TileTint.Teal, TileTint.Pink, TileTint.Indigo)

/**
 * Today's classes for students and teachers — the web's `today-timetable.tsx`
 * section (title, "Full timetable" link) with each period as a kit row: the
 * subject, who and where, and the period's time. A closed day or a day with no
 * periods renders nothing, as on the web.
 */
@Composable
fun TodayClasses(
    timetable: TodayTimetableDto?,
    teacherView: Boolean,
    onOpenTimetable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = timetable?.periods.orEmpty()
    if (timetable == null || timetable.closure != null || periods.none { !it.isBreak }) return
    val colors = HogwartsTheme.colors

    Column(modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.dash_today_classes),
            linkLabel = stringResource(R.string.dash_full_timetable),
            onLinkClick = onOpenTimetable,
        )
        ListRows(
            rows = periods.map { period ->
                {
                    if (period.isBreak) {
                        Text(
                            listOfNotNull(stringResource(R.string.dash_break), period.timeRange()).joinToString(" · "),
                            style = HogwartsTheme.type.caption,
                            color = colors.mutedForeground,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        ListRow(
                            title = period.subject ?: period.periodName.orEmpty(),
                            art = { TileFace(tint = tintFor(period.subject)) },
                            description = if (teacherView) period.className else period.teacher,
                            meta = listOfNotNull(period.periodName, period.timeRange(), period.room).joinToString(" · "),
                        )
                    }
                }
            },
        )
    }
}

private fun tintFor(subject: String?): TileTint =
    SUBJECT_TINTS[Math.floorMod(subject.orEmpty().hashCode(), SUBJECT_TINTS.size)]

/** Periods arrive as ISO times or "HH:mm"; show "HH:mm–HH:mm". */
internal fun PeriodDto.timeRange(): String? {
    fun hhmm(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val match = Regex("""(\d{2}):(\d{2})""").find(value.substringAfter('T', value)) ?: return null
        return "${match.groupValues[1]}:${match.groupValues[2]}"
    }
    val start = hhmm(startTime) ?: return null
    val range = hhmm(endTime)?.let { "$start–$it" } ?: start
    // Left-to-right isolate: in Arabic text the range must still read start–end.
    return "\u2066$range\u2069"
}
