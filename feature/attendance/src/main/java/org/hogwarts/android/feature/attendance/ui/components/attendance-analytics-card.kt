package org.hogwarts.android.feature.attendance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.feature.attendance.domain.model.AttendanceAnalytics

@Composable
fun AttendanceAnalyticsCard(
    analytics: AttendanceAnalytics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AppleSpacing.Standard),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        // Attendance percentage ring
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AttendanceRing(
                percentage = analytics.attendancePercentage,
                modifier = Modifier.size(80.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)) {
                StatRow(stringResource(R.string.attendance_present), analytics.presentDays, MaterialTheme.colorScheme.primary)
                StatRow(stringResource(R.string.attendance_absent), analytics.absentDays, MaterialTheme.colorScheme.error)
                StatRow(stringResource(R.string.attendance_late), analytics.lateDays, MaterialTheme.colorScheme.tertiary)
                StatRow(stringResource(R.string.attendance_excused), analytics.excusedDays, MaterialTheme.colorScheme.secondary)
            }
        }

        // Monthly bar chart
        if (analytics.monthlyBreakdown.isNotEmpty()) {
            Text(
                text = stringResource(R.string.attendance_monthly_trend),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MonthlyChart(
                data = analytics.monthlyBreakdown.takeLast(6),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
        }
    }
}

@Composable
private fun AttendanceRing(
    percentage: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - radius * 2) / 2,
            (size.height - radius * 2) / 2
        )
        val arcSize = Size(radius * 2, radius * 2)

        // Background track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = percentage / 100f * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun StatRow(label: String, count: Int, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MonthlyChart(
    data: List<org.hogwarts.android.feature.attendance.domain.model.MonthlyAttendance>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val barWidth = size.width / (data.size * 2 + 1)
        val maxDays = data.maxOf { it.totalDays }.toFloat().coerceAtLeast(1f)

        data.forEachIndexed { index, month ->
            val x = barWidth * (index * 2 + 1)
            val presentHeight = (month.presentDays / maxDays) * size.height
            val absentHeight = (month.absentDays / maxDays) * size.height

            // Present bar
            drawRect(
                color = primaryColor,
                topLeft = Offset(x, size.height - presentHeight),
                size = Size(barWidth * 0.6f, presentHeight)
            )
            // Absent bar
            drawRect(
                color = errorColor,
                topLeft = Offset(x + barWidth * 0.6f, size.height - absentHeight),
                size = Size(barWidth * 0.4f, absentHeight)
            )
        }
    }
}
