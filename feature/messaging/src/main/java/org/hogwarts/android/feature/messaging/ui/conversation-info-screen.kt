package org.hogwarts.android.feature.messaging.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.domain.model.MessageSearchResult
import org.hogwarts.android.feature.messaging.domain.model.Participant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationInfoScreen(
    onNavigateBack: () -> Unit,
    onAfterExit: () -> Unit = onNavigateBack,
    viewModel: ConversationInfoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.navigateAwayAfterAction) {
        if (state.navigateAwayAfterAction) {
            viewModel.consumeNavigateAway()
            onAfterExit()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isGroup)
                            stringResource(R.string.messaging_info_group_info)
                        else stringResource(R.string.messaging_info_contact_info),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.messaging_ui_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val conv = state.conversation
        if (state.isLoading || conv == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        UserAvatar(name = conv.title, imageUrl = conv.avatarUrl, size = 96.dp)
                        Text(
                            text = conv.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (viewModel.isGroup) {
                            Text(
                                text = stringResource(
                                    R.string.messaging_ui_members,
                                    conv.participants.size,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider()
                }

                // Mute toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onToggleMute() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.Filled.NotificationsOff
                            else Icons.Filled.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.messaging_info_mute_notifications),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = state.isMuted,
                            onCheckedChange = { viewModel.onToggleMute() },
                        )
                    }
                    HorizontalDivider()
                }

                // Starred messages
                if (state.starredMessages.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = Icons.Filled.Star,
                            title = stringResource(R.string.messaging_info_starred_messages),
                            count = state.starredMessages.size,
                        )
                    }
                    items(state.starredMessages, key = { it.id }) { msg ->
                        StarredMessageRow(msg)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                // Participants
                if (conv.participants.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = null,
                            title = stringResource(R.string.messaging_info_participants),
                            count = conv.participants.size,
                        )
                    }
                    items(conv.participants, key = { it.id }) { participant ->
                        ParticipantRow(participant = participant)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                // Destructive actions
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    DestructiveRow(
                        icon = Icons.Filled.Archive,
                        text = stringResource(R.string.messaging_actions_archive),
                        onClick = viewModel::onRequestArchive,
                    )
                    HorizontalDivider()
                }
                if (viewModel.isGroup) {
                    item {
                        DestructiveRow(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            text = stringResource(R.string.messaging_info_exit_group),
                            onClick = viewModel::onRequestExit,
                        )
                    }
                } else {
                    item {
                        DestructiveRow(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            text = stringResource(R.string.messaging_info_delete_conversation),
                            onClick = viewModel::onRequestExit,
                        )
                    }
                }
            }
        }

        if (state.confirmingExit) {
            AlertDialog(
                onDismissRequest = viewModel::onDismissExit,
                title = { Text(stringResource(R.string.messaging_confirmation_leave_conversation)) },
                text = {
                    Text(stringResource(R.string.messaging_confirmation_leave_conversation_description))
                },
                confirmButton = {
                    TextButton(onClick = viewModel::onExitConfirmed) {
                        Text(
                            text = stringResource(R.string.messaging_actions_leave),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onDismissExit) {
                        Text(stringResource(R.string.messaging_actions_cancel))
                    }
                },
            )
        }

        if (state.confirmingArchive) {
            AlertDialog(
                onDismissRequest = viewModel::onDismissArchive,
                title = { Text(stringResource(R.string.messaging_confirmation_archive_conversation)) },
                text = {
                    Text(stringResource(R.string.messaging_confirmation_archive_conversation_description))
                },
                confirmButton = {
                    TextButton(onClick = viewModel::onArchiveConfirmed) {
                        Text(stringResource(R.string.messaging_actions_archive))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onDismissArchive) {
                        Text(stringResource(R.string.messaging_actions_cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector?, title: String, count: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = if (count != null) "$title ($count)" else title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StarredMessageRow(msg: MessageSearchResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = msg.senderName.ifEmpty { "" },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = msg.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
        )
    }
}

@Composable
private fun ParticipantRow(participant: Participant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UserAvatar(
            name = participant.name,
            imageUrl = participant.avatarUrl,
            size = 40.dp,
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = participant.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            val roleLabel = participantRoleLabel(participant.role)
            if (roleLabel != null) {
                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun participantRoleLabel(role: String?): String? {
    if (role.isNullOrBlank()) return null
    val resId = when (role.lowercase()) {
        "owner" -> R.string.messaging_roles_owner
        "admin" -> R.string.messaging_roles_admin
        "member" -> R.string.messaging_roles_member
        "read_only" -> R.string.messaging_roles_read_only
        "guest" -> R.string.messaging_roles_guest
        else -> null
    }
    return if (resId != null) stringResource(resId) else role
}

@Composable
private fun DestructiveRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
