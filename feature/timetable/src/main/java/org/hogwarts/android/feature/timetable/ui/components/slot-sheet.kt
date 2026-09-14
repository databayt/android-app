package org.hogwarts.android.feature.timetable.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.timetable.R
import org.hogwarts.android.feature.timetable.domain.model.JoinTarget
import org.hogwarts.android.feature.timetable.domain.model.LiveStatus
import org.hogwarts.android.feature.timetable.domain.model.Slot
import org.hogwarts.android.feature.timetable.domain.model.isRowJoinable
import org.hogwarts.android.feature.timetable.domain.model.liveStatus
import org.hogwarts.android.feature.timetable.domain.model.periodNumber
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * `slot-detail-dialog.tsx`: the class a reader tapped — subject, then its date,
 * day, time and period in words, then the teacher, how soon it starts, and the
 * way into its room while it is joinable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSheet(
    slot: Slot,
    today: Int,
    nowMinutes: Int,
    onJoin: (JoinTarget) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val dayNames = stringArrayResource(R.array.timetable_day_names)
    val periodWords = stringArrayResource(R.array.timetable_detail_periods)
    val periodLabel = periodNumber(slot.periodName).toIntOrNull()?.let { periodWords.getOrNull(it - 1) } ?: periodNumber(slot.periodName)
    val date = LocalDate.now().let { now -> now.plusDays((slot.day - now.dayOfWeek.value % 7).toLong()) }
    val desc = listOf(ltr("${date.dayOfMonth}/${date.monthValue}"), dayNames.getOrElse(slot.day) { "" }, ltr(slot.start.hhmm), periodLabel)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
    val isToday = slot.day == today
    val until = slot.start.minutes - nowMinutes
    val status = when {
        isToday && liveStatus(slot.start, slot.end, nowMinutes) == LiveStatus.Upcoming && until >= 60 ->
            stringResource(R.string.timetable_detail_starts_in_hours, (until / 60f).roundToInt().toString())
        isToday && liveStatus(slot.start, slot.end, nowMinutes) == LiveStatus.Upcoming && until > 0 ->
            stringResource(R.string.timetable_detail_starts_in, until.toString())
        else -> null
    }
    val target = slot.liveClass?.joinTarget?.takeIf { isToday && isRowJoinable(slot.start, slot.end, nowMinutes) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.background) {
        Column(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(56.dp).padding(bottom = 12.dp))
            Text(slot.subject ?: stringResource(R.string.timetable_detail_title), style = type.section, color = colors.foreground, textAlign = TextAlign.Center)
            Text(desc, style = type.caption, color = colors.mutedForeground, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
            slot.teacher?.let { Text(it, style = type.caption, color = colors.mutedForeground, textAlign = TextAlign.Center) }
            status?.let {
                Text(it, style = type.caption.copy(fontWeight = FontWeight.Medium), color = colors.foreground, modifier = Modifier.padding(top = 8.dp))
            }
            if (target != null) {
                Column(Modifier.padding(top = 16.dp)) {
                    PillButton(stringResource(R.string.timetable_detail_enter), onClick = { onJoin(target) }, icon = Icons.Outlined.Videocam)
                }
            }
        }
    }
}
