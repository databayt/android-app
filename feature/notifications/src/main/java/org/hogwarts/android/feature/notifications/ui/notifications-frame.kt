package org.hogwarts.android.feature.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.PageNav
import org.hogwarts.android.core.designsystem.kit.PageNavItem
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.notifications.R

/**
 * `notifications/layout.tsx`, shared by the list and the preferences page:
 * the tabs, "Mark all as read" while anything is unread, then the page —
 * `space-y-6` apart, on the 16dp page gutter.
 */
@Composable
internal fun NotificationsFrame(
    tab: NotificationsTab,
    unreadCount: Int,
    markingAll: Boolean,
    onSelectTab: (NotificationsTab) -> Unit,
    onMarkAllRead: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(HogwartsTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        PageNav(
            items = listOf(
                PageNavItem(NotificationsTab.All.name, stringResource(R.string.notifications_tab_all)),
                PageNavItem(NotificationsTab.Unread.name, stringResource(R.string.notifications_tab_unread), badge = unreadCount),
                PageNavItem(NotificationsTab.Settings.name, stringResource(R.string.notifications_tab_settings)),
            ),
            selectedKey = tab.name,
            onSelect = { onSelectTab(NotificationsTab.valueOf(it.key)) },
        )
        Spacer(Modifier.height(24.dp))
        if (unreadCount > 0) {
            MarkAllReadButton(busy = markingAll, onClick = onMarkAllRead)
            Spacer(Modifier.height(24.dp))
        }
        content()
    }
}

/** `mark-all-read-button.tsx`: a small ghost button, check-check icon then the label. */
@Composable
private fun MarkAllReadButton(busy: Boolean, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    Row(
        Modifier
            .height(32.dp)
            .clip(HogwartsShapes.Md)
            .clickable(enabled = !busy, role = Role.Button, onClick = onClick)
            .alpha(if (busy) 0.5f else 1f)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(color = colors.foreground, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
        } else {
            Icon(Icons.Outlined.DoneAll, contentDescription = null, tint = colors.foreground, modifier = Modifier.size(16.dp))
        }
        Text(stringResource(R.string.notifications_mark_all_read), style = HogwartsTheme.type.bodyMedium, color = colors.foreground)
    }
}
