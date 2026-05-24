package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.feature.messaging.R

private val QUICK_REACTIONS = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

data class MessageActionSheetState(
    val canEdit: Boolean,
    val canDelete: Boolean,
    val isStarred: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionSheet(
    state: MessageActionSheetState,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onQuickReact: (String) -> Unit,
    onMoreReactions: () -> Unit,
    onCopy: () -> Unit,
    onForward: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStar: () -> Unit,
    onPin: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            // Quick reaction bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    items(QUICK_REACTIONS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onQuickReact(emoji) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onMoreReactions),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ActionItem(
                label = stringResource(R.string.messaging_actions_reply),
                icon = Icons.AutoMirrored.Filled.Reply,
                onClick = onReply,
            )
            ActionItem(
                label = stringResource(R.string.messaging_actions_copy),
                icon = Icons.Filled.ContentCopy,
                onClick = onCopy,
            )
            ActionItem(
                label = stringResource(R.string.messaging_actions_forward),
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = onForward,
            )
            ActionItem(
                label = stringResource(
                    if (state.isStarred) R.string.messaging_actions_unstar
                    else R.string.messaging_actions_star
                ),
                icon = if (state.isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                onClick = onToggleStar,
            )
            ActionItem(
                label = stringResource(R.string.messaging_actions_pin),
                icon = Icons.Filled.PushPin,
                onClick = onPin,
            )
            if (state.canEdit) {
                ActionItem(
                    label = stringResource(R.string.messaging_actions_edit),
                    icon = Icons.Filled.Edit,
                    onClick = onEdit,
                )
            }
            if (state.canDelete) {
                ActionItem(
                    label = stringResource(R.string.messaging_actions_delete),
                    icon = Icons.Filled.Delete,
                    onClick = onDelete,
                    destructive = true,
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (destructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (destructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}
