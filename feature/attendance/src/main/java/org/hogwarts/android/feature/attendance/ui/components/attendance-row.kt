package org.hogwarts.android.feature.attendance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStatus

/**
 * A single attendance record row in the attendance list.
 */
@Composable
fun AttendanceRow(
    record: AttendanceRecord,
    formatter: LocaleFormatter,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    AppleListRow(
        showDivider = showDivider,
        trailingContent = {
            StatusBadge(
                text = record.status.name.lowercase().replaceFirstChar { it.uppercase() },
                color = record.status.toColor()
            )
        },
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.className,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = formatter.formatDate(record.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (record.checkInTime != null) {
                Text(
                    text = stringResource(
                        R.string.attendance_checked_in,
                        formatter.formatTime(record.checkInTime)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AttendanceStatus.toColor() = when (this) {
    AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.tertiary
    AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
    AttendanceStatus.LATE -> MaterialTheme.colorScheme.secondary
    AttendanceStatus.EXCUSED -> MaterialTheme.colorScheme.primary
}
