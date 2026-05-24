package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.profile.domain.model.ActivityFeedItem
import java.text.DateFormat
import java.util.Date

/**
 * Web reference: profile/activity.tsx
 */
@Composable
fun ActivityFeed(
    items: List<ActivityFeedItem>,
    modifier: Modifier = Modifier,
    emptyState: @Composable () -> Unit = { ActivityEmptyState() }
) {
    if (items.isEmpty()) {
        emptyState()
        return
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
    ) {
        items.forEachIndexed { index, item ->
            ActivityRow(item)
            if (index != items.lastIndex) HorizontalDivider()
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityFeedItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = formatRelative(item.timestampMillis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityEmptyState() {
    Text(
        text = "No recent activity",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(AppleSpacing.Standard)
    )
}

private fun formatRelative(timestampMillis: Long): String {
    val delta = System.currentTimeMillis() - timestampMillis
    val minutes = delta / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> DateFormat.getDateInstance(DateFormat.SHORT).format(Date(timestampMillis))
    }
}
