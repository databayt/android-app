package org.hogwarts.android.feature.messaging.ui.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.domain.model.ChatSummary
import org.hogwarts.android.feature.messaging.ui.common.AvatarSkin
import org.hogwarts.android.feature.messaging.ui.common.ChevronEndGlyph
import org.hogwarts.android.feature.messaging.ui.common.WaAvatar
import org.hogwarts.android.feature.messaging.ui.common.WaListRow
import org.hogwarts.android.feature.messaging.ui.common.WaSectionHeading
import org.hogwarts.android.feature.messaging.ui.common.WaTabEmpty
import org.hogwarts.android.feature.messaging.ui.common.WaTabPage
import org.hogwarts.android.feature.messaging.ui.common.bottomHairline
import org.hogwarts.android.feature.messaging.ui.common.mirrorInRtl
import org.hogwarts.android.feature.messaging.ui.common.wa
import org.hogwarts.android.feature.messaging.ui.common.waType

/**
 * `tabs/updates-view.tsx`. The web fills this page from its own server action,
 * which reads announcements through the viewer's audience filter. The mobile
 * API has no such route: `GET /api/mobile/announcements` filters on
 * `published` alone, and the web deliberately refuses it because a student
 * would see staff-only notices. So the page draws the web's own failure state
 * rather than show the wrong list or an invented one.
 */
@Composable
fun UpdatesTab(modifier: Modifier = Modifier) {
    WaTabPage(stringResource(R.string.messages_tab_updates), modifier) {
        WaTabEmpty(stringResource(R.string.messages_load_failed), stringResource(R.string.messages_updates_empty_body))
    }
}

/**
 * `tabs/calls-view.tsx`. The web lists the viewer's live-class sessions from a
 * server action; the mobile API has no calls feed (only
 * `conference/:id/join`), so this is the web's failure state too.
 */
@Composable
fun CallsTab(modifier: Modifier = Modifier) {
    WaTabPage(stringResource(R.string.messages_tab_calls), modifier) {
        WaTabEmpty(stringResource(R.string.messages_load_failed), stringResource(R.string.messages_calls_empty_body))
    }
}

/**
 * `tabs/communities-view.tsx`: the school is the community and its rooms are
 * the conversations that are not 1:1, so the page is the inbox read another
 * way. The list route returns no participants, so the web's member counts are
 * left off rather than guessed.
 */
@Composable
fun CommunitiesTab(
    schoolName: String?,
    chats: List<ChatSummary>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rooms = chats.filter { it.isGroup }.sortedByDescending { it.lastMessageAt }
    WaTabPage(stringResource(R.string.messages_tab_communities), modifier) {
        if (rooms.isEmpty()) {
            WaTabEmpty(
                stringResource(R.string.messages_communities_empty_title),
                stringResource(R.string.messages_communities_empty_body),
            )
        } else {
            WaListRow(
                title = schoolName.orEmpty(),
                avatarIcon = R.drawable.ic_wa_tab_communities_fill_32,
                avatarSkin = AvatarSkin.Group,
            )
            WaSectionHeading(stringResource(R.string.messages_communities_rooms))
            val fallback = stringResource(R.string.messages_group_fallback)
            rooms.forEach { room ->
                WaListRow(
                    title = room.title ?: fallback,
                    avatarUrl = room.avatarUrl,
                    avatarIcon = R.drawable.ic_wa_group_16,
                    avatarSkin = AvatarSkin.Group,
                    onClick = { onOpen(room.id) },
                )
            }
        }
    }
}

data class SettingsRow(val id: String, val label: String, val icon: ImageVector, val onClick: (() -> Unit)?)

/**
 * `tabs/settings-view.tsx`: a profile card over grouped rows, each leading to
 * a page the app already has. Starred messages has no handler on the web
 * either, so its row does nothing there too.
 */
@Composable
fun SettingsTab(
    name: String,
    status: String?,
    avatarUrl: String?,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenChats: () -> Unit,
    onOpenDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = listOf(
        listOf(
            SettingsRow("profile", stringResource(R.string.messages_settings_profile), Icons.Outlined.PersonOutline, onOpenProfile),
            SettingsRow("starred", stringResource(R.string.messages_settings_starred), Icons.Outlined.StarOutline, null),
        ),
        listOf(
            SettingsRow("notifications", stringResource(R.string.messages_settings_notifications), Icons.Outlined.NotificationsNone, onOpenNotifications),
            SettingsRow("chats", stringResource(R.string.messages_tab_chats), Icons.Outlined.ChatBubbleOutline, onOpenChats),
        ),
        listOf(
            SettingsRow("dashboard", stringResource(R.string.messages_settings_dashboard), Icons.Outlined.Dashboard, onOpenDashboard),
        ),
    )
    WaTabPage(stringResource(R.string.messages_tab_settings), modifier, background = wa.surfaceCtaFilters) {
        Box(Modifier.padding(horizontal = 16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(wa.surfacePrimary)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WaAvatar(url = avatarUrl, size = 56.dp, skin = AvatarSkin.Person, borderWidth = 0.dp)
                Column {
                    Text(name, style = waType(19f, 24f, FontWeight.SemiBold, -0.38f), color = wa.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (!status.isNullOrEmpty()) {
                        Text(status, style = waType(15f, 20f), color = wa.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        groups.forEach { group ->
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(wa.surfacePrimary)) {
                    group.forEachIndexed { j, row ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { row.onClick?.invoke() }
                                .padding(start = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(row.icon, null, tint = wa.textPrimary, modifier = Modifier.size(22.dp))
                            Row(
                                Modifier
                                    .weight(1f)
                                    .then(if (j < group.size - 1) Modifier.bottomHairline(wa.borderSeparator) else Modifier)
                                    .padding(top = 13.dp, bottom = 13.dp, end = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(row.label, style = waType(17f, 22f, tracking = -0.34f), color = wa.textPrimary, modifier = Modifier.weight(1f), maxLines = 1)
                                Image(ChevronEndGlyph, null, Modifier.size(14.dp).mirrorInRtl(), colorFilter = ColorFilter.tint(wa.textSecondary))
                            }
                        }
                    }
                }
            }
        }
    }
}
