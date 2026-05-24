package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.profile.domain.model.ContributionGraphData
import org.hogwarts.android.feature.profile.domain.model.DailyContribution

/**
 * GitHub-style contribution heatmap.
 * Mirrors web `profile/graph.tsx` — 53 weeks x 7 days grid with intensity levels 0..4.
 */
@Composable
fun ContributionGraph(
    data: ContributionGraphData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            Text(
                text = "${data.totalActivities} activities in ${data.year}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 53 weeks scrolled horizontally
        val grid = data.contributions.groupByWeek()
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = AppleSpacing.Tiny)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                grid.forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(7) { day ->
                            val cell = week.getOrNull(day)
                            ContributionCell(level = cell?.level ?: 0)
                        }
                    }
                }
            }
        }

        ContributionLegend()

        ContributionStatsRow(data)
    }
}

@Composable
private fun ContributionCell(level: Int) {
    val color = levelColor(level)
    Box(
        modifier = Modifier
            .size(11.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
private fun levelColor(level: Int): Color {
    val scheme = MaterialTheme.colorScheme
    val base = scheme.primary
    return when (level.coerceIn(0, 4)) {
        0 -> scheme.surfaceVariant.copy(alpha = 0.5f)
        1 -> base.copy(alpha = 0.25f)
        2 -> base.copy(alpha = 0.5f)
        3 -> base.copy(alpha = 0.75f)
        else -> base
    }
}

@Composable
private fun ContributionLegend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        repeat(5) { idx -> ContributionCell(level = idx) }
        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContributionStatsRow(data: ContributionGraphData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        StatCell("Active days", data.summary.activeDays.toString())
        StatCell("Streak", "${data.summary.currentStreak}d")
        StatCell("Longest", "${data.summary.longestStreak}d")
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun List<DailyContribution>.groupByWeek(): List<List<DailyContribution>> {
    if (isEmpty()) return emptyList()
    val weeks = mutableListOf<MutableList<DailyContribution>>()
    var current = mutableListOf<DailyContribution>()
    forEach { day ->
        current.add(day)
        if (current.size == 7) {
            weeks.add(current)
            current = mutableListOf()
        }
    }
    if (current.isNotEmpty()) weeks.add(current)
    return weeks
}
