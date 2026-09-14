package org.hogwarts.android.feature.attendance.ui.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.kit.ProgressBar
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.feature.attendance.domain.model.AttendanceEntry
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStats
import org.hogwarts.android.feature.attendance.domain.model.ChildAttendance
import org.hogwarts.android.feature.attendance.domain.model.StudentAttendance
import org.hogwarts.android.feature.attendance.ui.AttendancePage
import org.hogwarts.android.feature.attendance.ui.attendanceFormat
import org.hogwarts.android.feature.attendance.ui.components.SkeletonBlock
import org.hogwarts.android.feature.attendance.ui.statusName

@Composable
fun MineScreen(
    nav: @Composable () -> Unit,
    viewModel: MineViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MineView(state = state, nav = nav, onRetry = viewModel::load)
}

/**
 * Student / guardian view of `/attendance` — the phone branch of
 * `attendance/overview/student-guardian-overview.tsx`.
 */
@Composable
fun MineView(state: MineUiState, nav: @Composable () -> Unit, onRetry: () -> Unit) {
    AttendancePage(nav = nav) {
        when (state) {
            MineUiState.Loading -> Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SkeletonBlock(Modifier.fillMaxWidth().height(48.dp))
                SkeletonBlock(Modifier.fillMaxWidth().height(180.dp))
                SkeletonBlock(Modifier.fillMaxWidth().height(160.dp))
            }
            is MineUiState.Student -> StudentOverview(state.attendance)
            is MineUiState.Guardian -> GuardianOverview(state.children)
            is MineUiState.Unavailable -> Unavailable(state.failed, onRetry)
        }
    }
}

@Composable
private fun Heading(title: String, description: String) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column {
        // A bare <h2>: preflight leaves it at the body's 16px, regular weight.
        Text(title, style = type.rowTitle.copy(fontWeight = FontWeight.Normal), color = colors.foreground)
        Text(description, style = type.body, color = colors.mutedForeground)
    }
}

@Composable
private fun StudentOverview(attendance: StudentAttendance) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Heading(stringResource(R.string.attendance_student_title), stringResource(R.string.attendance_student_description))
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            PhoneStats(attendance.stats)
            Column {
                SectionHeader(stringResource(R.string.attendance_recent_activity))
                if (attendance.records.isEmpty()) {
                    Text(
                        stringResource(R.string.attendance_no_recent_records),
                        style = type.body,
                        color = colors.mutedForeground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    )
                } else {
                    RecordRows(attendance.records.take(10))
                }
            }
        }
    }
}

@Composable
private fun GuardianOverview(children: List<ChildAttendance>) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val format = attendanceFormat()
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Heading(stringResource(R.string.attendance_guardian_title), stringResource(R.string.attendance_guardian_description))
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            if (children.size > 1) {
                val avg = Math.round(children.sumOf { it.stats.rate } / children.size.toFloat())
                val absent = children.sumOf { it.stats.absent }
                val late = children.sumOf { it.stats.late }
                StatPanel(
                    items = listOf(
                        StatItem("children", stringResource(R.string.attendance_stat_children), format.count(children.size)),
                        StatItem("rate", stringResource(R.string.attendance_stat_rate), format.percent(avg)),
                        StatItem("absent", stringResource(R.string.attendance_stat_absent), format.count(absent), tone = if (absent > 0) StatTone.Negative else StatTone.Default),
                        StatItem("late", stringResource(R.string.attendance_stat_late), format.count(late), tone = if (late > 0) StatTone.Warning else StatTone.Default),
                    ),
                )
            }
            children.forEach { child ->
                Column {
                    SectionHeader(title = child.name, description = child.className.ifEmpty { null })
                    PhoneStats(child.stats)
                    if (child.recentAbsences.isNotEmpty()) {
                        Text(
                            stringResource(R.string.attendance_recent_absences),
                            style = type.caption.copy(fontWeight = FontWeight.Medium),
                            color = colors.mutedForeground,
                            modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
                        )
                        RecordRows(child.recentAbsences.take(3))
                    }
                }
            }
        }
    }
}

/** One student's figures: the rate across the top with its bar, then present · absent · late · excused. */
@Composable
private fun PhoneStats(stats: AttendanceStats) {
    val format = attendanceFormat()
    StatPanel(
        items = listOf(
            StatItem(
                key = "rate",
                label = stringResource(R.string.attendance_stat_rate),
                value = format.percent(stats.rate),
                hint = stringResource(R.string.attendance_out_of_days, format.count(stats.totalDays)),
                wide = true,
                extra = { ProgressBar(stats.rate.toFloat()) },
            ),
            StatItem("present", stringResource(R.string.attendance_stat_present), format.count(stats.present), tone = StatTone.Positive),
            StatItem("absent", stringResource(R.string.attendance_stat_absent), format.count(stats.absent), tone = if (stats.absent > 0) StatTone.Negative else StatTone.Default),
            StatItem("late", stringResource(R.string.attendance_stat_late), format.count(stats.late), tone = if (stats.late > 0) StatTone.Warning else StatTone.Default),
            StatItem("excused", stringResource(R.string.attendance_stat_excused), format.count(stats.excused)),
        ),
    )
}

@Composable
private fun RecordRows(records: List<AttendanceEntry>) {
    val format = attendanceFormat()
    ListRows(
        divided = true,
        rows = records.map { record ->
            {
                ListRow(
                    title = format.shortDay(record.date),
                    description = record.className,
                    trailing = {
                        LabelBadge(
                            statusName(record.status),
                            variant = when (record.status.uppercase()) {
                                "PRESENT" -> BadgeVariant.Default
                                "ABSENT" -> BadgeVariant.Destructive
                                else -> BadgeVariant.Secondary
                            },
                        )
                    },
                )
            }
        },
    )
}

@Composable
private fun Unavailable(failed: Boolean, onRetry: () -> Unit) {
    val colors = HogwartsTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(48.dp))
        Text(
            stringResource(if (failed) R.string.attendance_unable_to_load else R.string.attendance_no_data),
            style = HogwartsTheme.type.body,
            color = colors.mutedForeground,
            textAlign = TextAlign.Center,
        )
        if (failed) PillButton(stringResource(R.string.attendance_retry), onClick = onRetry, variant = PillVariant.Outline)
    }
}
