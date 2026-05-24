package org.hogwarts.android.feature.notifications.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.notifications.R
import org.hogwarts.android.feature.notifications.domain.NotificationConfig
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_preferences_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.notifications_back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = !uiState.isSaving && !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.notifications_preferences_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        AppleInsetGroupedList(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // One section per channel; rows are notification types with a switch.
            // Mirrors the type × channel matrix from the web preferences-form.
            NotificationConfig.supportedChannels.forEach { channel ->
                item(key = "section-${channel.name}") {
                    AppleListSection(
                        header = stringResource(channelLabelRes(channel)),
                        footer = stringResource(channelFooterRes(channel))
                    ) {
                        val types = NotificationConfig.configurableTypes
                        types.forEachIndexed { index, type ->
                            val enabled = uiState.isEnabled(type, channel)
                            AppleListRow(showDivider = index < types.size - 1) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    androidx.compose.foundation.layout.Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(typeLabelRes(type)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Switch(
                                            checked = enabled,
                                            onCheckedChange = { viewModel.toggle(type, channel, it) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                item(key = "error") {
                    Text(
                        text = stringResource(R.string.notifications_preferences_error, error),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(AppleSpacing.Standard)
                    )
                }
            }
        }
    }
}

private fun channelLabelRes(channel: NotificationChannel) = when (channel) {
    NotificationChannel.IN_APP -> R.string.notifications_channel_in_app
    NotificationChannel.EMAIL -> R.string.notifications_channel_email
    NotificationChannel.PUSH -> R.string.notifications_channel_push
    NotificationChannel.SMS -> R.string.notifications_channel_sms
    NotificationChannel.WHATSAPP -> R.string.notifications_channel_whatsapp
}

private fun channelFooterRes(channel: NotificationChannel) = when (channel) {
    NotificationChannel.IN_APP -> R.string.notifications_channel_in_app_footer
    NotificationChannel.EMAIL -> R.string.notifications_channel_email_footer
    NotificationChannel.PUSH -> R.string.notifications_channel_push_footer
    NotificationChannel.SMS -> R.string.notifications_channel_sms_footer
    NotificationChannel.WHATSAPP -> R.string.notifications_channel_whatsapp_footer
}

private fun typeLabelRes(type: org.hogwarts.android.feature.notifications.domain.model.NotificationType) =
    when (type) {
        org.hogwarts.android.feature.notifications.domain.model.NotificationType.ANNOUNCEMENT ->
            R.string.notifications_type_announcement
        org.hogwarts.android.feature.notifications.domain.model.NotificationType.ATTENDANCE ->
            R.string.notifications_type_attendance
        org.hogwarts.android.feature.notifications.domain.model.NotificationType.GRADE ->
            R.string.notifications_type_grade
        org.hogwarts.android.feature.notifications.domain.model.NotificationType.FEE ->
            R.string.notifications_type_fee
        org.hogwarts.android.feature.notifications.domain.model.NotificationType.MESSAGE ->
            R.string.notifications_type_message
        org.hogwarts.android.feature.notifications.domain.model.NotificationType.TIMETABLE ->
            R.string.notifications_type_timetable
        org.hogwarts.android.feature.notifications.domain.model.NotificationType.GENERAL ->
            R.string.notifications_type_general
    }
