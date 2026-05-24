package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.feature.messaging.domain.model.MessageReaction

/** Aggregated reaction chips shown below a bubble.
 *  Tapping a chip toggles the current user's reaction for that emoji. */
@Composable
fun MessageReactions(
    reactions: List<MessageReaction>,
    currentUserId: String,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (reactions.isEmpty()) return
    val grouped = reactions.groupBy { it.emoji }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        grouped.forEach { (emoji, list) ->
            val count = list.size
            val mine = list.any { it.userId == currentUserId }
            val bg = if (mine) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
            Row(
                modifier = Modifier
                    .background(bg, RoundedCornerShape(12.dp))
                    .clickable { onToggle(emoji) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = emoji, fontSize = 14.sp)
                if (count > 1) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (mine) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
