package org.hogwarts.android.core.designsystem.atom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.R
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons

@Composable
fun SyncStatusBanner(
    isSyncing: Boolean,
    lastSyncText: String?,
    hasError: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isSyncing || hasError || lastSyncText != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val backgroundColor = when {
            hasError -> MaterialTheme.colorScheme.errorContainer
            isSyncing -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        val contentColor = when {
            hasError -> MaterialTheme.colorScheme.onErrorContainer
            isSyncing -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = AppleSpacing.Standard, vertical = AppleSpacing.Compact),
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            } else if (hasError) {
                Icon(
                    imageVector = HogwartsIcons.ErrorIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            } else {
                Icon(
                    imageVector = HogwartsIcons.Checkmark,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }

            Text(
                text = when {
                    isSyncing -> "Syncing..."
                    hasError -> "Sync failed"
                    lastSyncText != null -> "Last synced: $lastSyncText"
                    else -> ""
                },
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            if (hasError) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.action_retry), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
