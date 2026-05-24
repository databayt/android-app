package org.hogwarts.android.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.profile.domain.model.PinnedItem

/**
 * Web reference: profile/pinned.tsx — grid of pinned cards.
 */
@Composable
fun PinnedItemsList(
    items: List<PinnedItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Text(
            text = "No pinned items yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(AppleSpacing.Standard)
        )
        return
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
    ) {
        items.forEach { PinnedItemCard(it) }
    }
}

@Composable
private fun PinnedItemCard(item: PinnedItem) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(AppleSpacing.Standard),
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AssistChip(
                    onClick = {},
                    label = { Text(item.category, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.stats.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)) {
                    item.stats.forEach { stat ->
                        Box {
                            Column {
                                Text(
                                    text = stat.value,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stat.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
