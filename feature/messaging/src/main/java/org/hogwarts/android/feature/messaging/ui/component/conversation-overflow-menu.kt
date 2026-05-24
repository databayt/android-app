package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.feature.messaging.R

@Composable
fun ConversationOverflowMenu(
    isPinned: Boolean,
    isMuted: Boolean,
    onPinToggle: () -> Unit,
    onMuteToggle: () -> Unit,
    onArchive: () -> Unit,
    onLeave: () -> Unit,
    onViewInfo: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.messaging_accessibility_more_options),
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.messaging_ui_details)) },
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
            onClick = { expanded = false; onViewInfo() },
        )
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(
                        if (isPinned) R.string.messaging_actions_unpin
                        else R.string.messaging_actions_pin
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                )
            },
            onClick = { expanded = false; onPinToggle() },
        )
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(
                        if (isMuted) R.string.messaging_actions_unmute
                        else R.string.messaging_actions_mute
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.NotificationsOff else Icons.Filled.Notifications,
                    contentDescription = null,
                )
            },
            onClick = { expanded = false; onMuteToggle() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.messaging_actions_archive)) },
            leadingIcon = { Icon(Icons.Filled.Archive, contentDescription = null) },
            onClick = { expanded = false; onArchive() },
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.messaging_actions_leave),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            onClick = { expanded = false; onLeave() },
        )
    }
}
