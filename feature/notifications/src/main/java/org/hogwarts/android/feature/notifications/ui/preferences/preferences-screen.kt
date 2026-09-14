package org.hogwarts.android.feature.notifications.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.kit.FormAlertTone
import org.hogwarts.android.core.designsystem.kit.FormButton
import org.hogwarts.android.core.designsystem.kit.FormButtonVariant
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.kit.ToggleSwitch
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.notifications.R
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.domain.model.PreferenceMatrix
import org.hogwarts.android.feature.notifications.ui.NotificationsFrame
import org.hogwarts.android.feature.notifications.ui.NotificationsTab
import org.hogwarts.android.feature.notifications.ui.icon
import org.hogwarts.android.feature.notifications.ui.labelRes

/**
 * `/notifications/preferences` on a phone, under the section's tabs with
 * Settings selected. The mobile API saves the per-type channel switches only,
 * so the web's quiet-hours and digest cards are left out.
 */
@Composable
fun NotificationPreferencesScreen(
    onSelectTab: (NotificationsTab) -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PreferencesContent(
        state = state,
        onSelectTab = onSelectTab,
        onMarkAllRead = viewModel::markAllRead,
        onToggle = viewModel::toggle,
        onReset = viewModel::reset,
        onSave = viewModel::save,
        onRetry = viewModel::load,
    )
}

@Composable
internal fun PreferencesContent(
    state: PreferencesUiState,
    onSelectTab: (NotificationsTab) -> Unit,
    onMarkAllRead: () -> Unit,
    onToggle: (NotificationKind, NotificationChannel, Boolean) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    NotificationsFrame(
        tab = NotificationsTab.Settings,
        unreadCount = state.unreadCount,
        markingAll = state.markingAll,
        onSelectTab = { if (it != NotificationsTab.Settings) onSelectTab(it) },
        onMarkAllRead = onMarkAllRead,
    ) {
        Text(
            stringResource(R.string.notifications_preferences_title),
            style = HogwartsTheme.type.bannerHeadline.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
            color = colors.foreground,
        )
        Text(
            stringResource(R.string.notifications_preferences_description),
            style = HogwartsTheme.type.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
            color = colors.mutedForeground,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(24.dp))

        val matrix = state.matrix
        when {
            state.isLoading && matrix == null -> Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.mutedForeground, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
            matrix == null -> Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.notifications_preferences_load_failed), style = HogwartsTheme.type.body, color = colors.mutedForeground)
                Spacer(Modifier.height(16.dp))
                PillButton(stringResource(R.string.notifications_retry), onClick = onRetry, variant = PillVariant.Outline)
            }
            else -> {
                TypesCard(matrix, onToggle)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)) {
                    FormButton(
                        label = stringResource(R.string.notifications_reset),
                        onClick = onReset,
                        variant = FormButtonVariant.Outline,
                        enabled = !state.isSaving,
                        height = 36.dp,
                        modifier = Modifier.width(IntrinsicSize.Max),
                    )
                    FormButton(
                        label = stringResource(R.string.notifications_save),
                        onClick = onSave,
                        loading = state.isSaving,
                        height = 36.dp,
                        modifier = Modifier.width(IntrinsicSize.Max),
                    )
                }
                state.outcome?.let { outcome ->
                    Spacer(Modifier.height(16.dp))
                    FormAlert(
                        message = stringResource(if (outcome == SaveOutcome.Saved) R.string.notifications_preferences_saved else R.string.notifications_preferences_save_failed),
                        tone = if (outcome == SaveOutcome.Saved) FormAlertTone.Success else FormAlertTone.Error,
                    )
                }
                Spacer(Modifier.height(24.dp))
                HelpCard()
            }
        }
    }
}

@Composable
private fun PrefCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(HogwartsTheme.colors.card)
            .border(1.dp, HogwartsTheme.colors.border, HogwartsShapes.Card)
            .padding(24.dp),
        content = content,
    )
}


/** "Notification Types": each type with its five channel switches, two to a row on a phone. */
@Composable
private fun TypesCard(matrix: PreferenceMatrix, onToggle: (NotificationKind, NotificationChannel, Boolean) -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    PrefCard {
        Text(stringResource(R.string.notifications_types_title), style = type.cardTitle.copy(fontSize = 16.sp, lineHeight = 24.sp), color = colors.foreground)
        Text(
            stringResource(R.string.notifications_types_description),
            style = type.body,
            color = colors.mutedForeground,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp),
        )
        NotificationKind.entries.forEachIndexed { index, kind ->
            if (index > 0) {
                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp).height(1.dp).background(colors.border))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(kind.icon(), contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.padding(top = 4.dp).size(20.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(kind.labelRes()), style = type.bodyMedium, color = colors.foreground, modifier = Modifier.weight(1f, fill = false))
                        if (kind.requiresAction) {
                            LabelBadge(stringResource(R.string.notifications_action_required), variant = BadgeVariant.Outline)
                        }
                    }
                    NotificationChannel.entries.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { channel ->
                                ChannelSwitch(
                                    channel = channel,
                                    checked = matrix.isOn(kind, channel),
                                    onChange = { onToggle(kind, channel, it) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelSwitch(channel: NotificationChannel, checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier) {
    val colors = HogwartsTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleSwitch(checked = checked, onCheckedChange = onChange)
        Icon(channel.icon(), contentDescription = null, tint = colors.foreground, modifier = Modifier.size(14.dp))
        Text(stringResource(channel.labelRes()), style = HogwartsTheme.type.caption, color = colors.foreground)
    }
}

/** The page's closing "Channel Settings" card: what each channel and setting means. */
@Composable
private fun HelpCard() {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    PrefCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Settings, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(20.dp))
            Text(stringResource(R.string.notifications_channel_settings), style = type.section, color = colors.foreground)
        }
        Spacer(Modifier.height(24.dp))
        HelpHeading(Icons.Outlined.Notifications, stringResource(R.string.notifications_delivery_methods))
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                Triple(NotificationChannel.InApp, R.string.notifications_delivery_in_app, R.string.notifications_delivery_in_app_description),
                Triple(NotificationChannel.Email, R.string.notifications_delivery_email, R.string.notifications_delivery_email_description),
                Triple(NotificationChannel.Push, R.string.notifications_delivery_push, R.string.notifications_delivery_push_description),
                Triple(NotificationChannel.Sms, R.string.notifications_delivery_sms, R.string.notifications_delivery_sms_description),
            ).forEach { (channel, name, description) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(HogwartsShapes.Lg)
                        .background(colors.muted.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(channel.icon(), contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                    Column {
                        Text(stringResource(name), style = type.bodyMedium, color = colors.foreground)
                        Text(stringResource(description), style = type.caption, color = colors.mutedForeground)
                    }
                }
            }
        }
        HelpSection(Icons.Outlined.DarkMode, R.string.notifications_quiet_hours_title, R.string.notifications_quiet_hours_description)
        HelpSection(Icons.Outlined.Inbox, R.string.notifications_digest_title, R.string.notifications_digest_description)
        HelpSection(Icons.Outlined.WarningAmber, R.string.notifications_action_required, R.string.notifications_types_description)
    }
}

@Composable
private fun HelpHeading(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = HogwartsTheme.colors.foreground, modifier = Modifier.size(16.dp))
        Text(title, style = HogwartsTheme.type.cardTitle, color = HogwartsTheme.colors.foreground)
    }
}

@Composable
private fun HelpSection(icon: ImageVector, title: Int, description: Int) {
    Box(Modifier.fillMaxWidth().padding(top = 24.dp).height(1.dp).background(HogwartsTheme.colors.border))
    Column(Modifier.padding(top = 16.dp)) {
        HelpHeading(icon, stringResource(title))
        Text(
            stringResource(description),
            style = HogwartsTheme.type.body,
            color = HogwartsTheme.colors.mutedForeground,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
