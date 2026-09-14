package org.hogwarts.android.feature.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.notifications.R
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.domain.model.NotificationPriority
import java.time.Instant

/**
 * `/notifications` and `/notifications/unread` on a phone — the notification
 * center under the section's tabs. The app shell draws the header above it.
 */
@Composable
fun NotificationsScreen(
    initialTab: NotificationsTab,
    onOpenHref: (String) -> Unit,
    onOpenPreferences: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.start(initialTab) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    NotificationsContent(
        state = state,
        now = Instant.now(),
        onSelectTab = { tab -> if (tab == NotificationsTab.Settings) onOpenPreferences() else viewModel.selectTab(tab) },
        onMarkAllRead = viewModel::markAllRead,
        onOpen = { notification ->
            when (val target = viewModel.open(notification)) {
                is NotificationTarget.Path -> onOpenHref(target.href)
                is NotificationTarget.External -> runCatching { uriHandler.openUri(target.url) }
                null -> Unit
            }
        },
        onDelete = viewModel::delete,
        onPage = viewModel::goToPage,
        onRetry = viewModel::retry,
    )
}

@Composable
internal fun NotificationsContent(
    state: NotificationsUiState,
    now: Instant,
    onSelectTab: (NotificationsTab) -> Unit,
    onMarkAllRead: () -> Unit,
    onOpen: (AppNotification) -> Unit,
    onDelete: (AppNotification) -> Unit,
    onPage: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    NotificationsFrame(
        tab = state.tab,
        unreadCount = state.unreadCount,
        markingAll = state.markingAll,
        onSelectTab = onSelectTab,
        onMarkAllRead = onMarkAllRead,
    ) {
        when {
            state.isLoading && state.page == null -> NotificationSkeleton()
            state.failed -> LoadFailed(onRetry)
            state.items.isEmpty() -> EmptyNotifications()
            else -> {
                if (state.isOffline) {
                    Text(
                        stringResource(R.string.notifications_offline),
                        style = HogwartsTheme.type.caption,
                        color = HogwartsTheme.colors.mutedForeground,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                Column(Modifier.fillMaxWidth()) {
                    state.items.forEach { notification ->
                        NotificationCard(
                            notification = notification,
                            now = now,
                            deleting = notification.id in state.deleting,
                            onOpen = { onOpen(notification) },
                            onDelete = { onDelete(notification) },
                        )
                    }
                }
                state.page?.let { page -> if (page.totalPages > 1) Pagination(page.page, page.totalPages, onPage) }
            }
        }
    }
}

/** `card.tsx` — avatar circle with the type's icon, the unread dot, the line, the time, and the X. */
@Composable
private fun NotificationCard(
    notification: AppNotification,
    now: Instant,
    deleting: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val kind = NotificationKind.fromWire(notification.type)
    val urgent = notification.priority == NotificationPriority.Urgent
    val stateLabel = stringResource(if (notification.isRead) R.string.notifications_a11y_read else R.string.notifications_a11y_unread)
    val typeLabel = kind?.let { stringResource(it.labelRes()) }
    val showTypeLabel = typeLabel != null && typeLabel != notification.title
    val priorityLabel = when (notification.priority) {
        NotificationPriority.Urgent -> stringResource(R.string.notifications_priority_urgent)
        NotificationPriority.High -> stringResource(R.string.notifications_priority_high)
        else -> null
    }

    Column(
        Modifier
            .fillMaxWidth()
            .alpha(if (deleting) 0.5f else 1f)
            .background(if (notification.isRead) Color.Transparent else colors.muted.copy(alpha = 0.3f))
            .clickable(enabled = !deleting, onClick = onOpen)
            .semantics { contentDescription = "$stateLabel: ${notification.title}" },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(HogwartsShapes.Pill)
                        .background(if (urgent) colors.destructive.copy(alpha = 0.1f) else colors.muted),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        kind.icon(),
                        contentDescription = null,
                        tint = if (urgent) colors.destructive else colors.mutedForeground,
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (!notification.isRead) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(14.dp)
                            .border(2.dp, if (colors.isDark) colors.background else Color.White, HogwartsShapes.Pill)
                            .padding(2.dp)
                            .clip(HogwartsShapes.Pill)
                            .background(colors.primary),
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                if (showTypeLabel || priorityLabel != null) {
                    Row(
                        Modifier.padding(bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (showTypeLabel) {
                            Text(typeLabel.orEmpty(), style = type.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = colors.mutedForeground)
                        }
                        if (priorityLabel != null) {
                            PriorityBadge(priorityLabel, if (urgent) BadgeVariant.Destructive else BadgeVariant.Outline)
                        }
                    }
                }
                val line = buildAnnotatedString {
                    notification.actorName?.let { withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("$it ") } }
                    append(notification.title)
                    if (notification.body.isNotBlank() && notification.body != notification.title) {
                        append(". ")
                        withStyle(SpanStyle(color = colors.mutedForeground)) { append(notification.body) }
                    }
                }
                Text(
                    line,
                    style = type.body,
                    color = if (notification.isRead) colors.mutedForeground else colors.foreground,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    timeLabel(notification.createdAt, now),
                    style = type.caption,
                    color = colors.mutedForeground,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Box(
                Modifier
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(HogwartsShapes.Md)
                    .clickable(enabled = !deleting, role = Role.Button, onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.notifications_a11y_delete),
                    tint = colors.mutedForeground,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

/** `Badge` at `h-4 px-1.5 text-[10px]`. */
@Composable
private fun PriorityBadge(label: String, variant: BadgeVariant) {
    val colors = HogwartsTheme.colors
    val destructive = variant == BadgeVariant.Destructive
    Text(
        label,
        style = HogwartsTheme.type.caption.copy(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
        color = if (destructive) Color.White else colors.foreground,
        maxLines = 1,
        modifier = Modifier
            .clip(HogwartsShapes.Md)
            .background(if (destructive) colors.destructive else Color.Transparent)
            .border(1.dp, if (destructive) Color.Transparent else colors.border, HogwartsShapes.Md)
            .padding(horizontal = 6.dp),
    )
}

@Composable
private fun timeLabel(created: Instant, now: Instant): String {
    val resources = LocalContext.current.resources
    return when (val distance = ago(created, now)) {
        Ago.LessThanMinute -> stringResource(R.string.notifications_ago_less_than_minute)
        is Ago.Minutes -> resources.getQuantityString(R.plurals.notifications_ago_minutes, distance.count, distance.count.toString())
        is Ago.Hours -> resources.getQuantityString(R.plurals.notifications_ago_hours, distance.count, distance.count.toString())
        is Ago.Days -> resources.getQuantityString(R.plurals.notifications_ago_days, distance.count, distance.count.toString())
        Ago.Date -> notificationDate(created, currentLocale().language.takeIf { it == "en" } ?: "ar")
    }
}

/** `list.tsx` empty state: a bell in a grey circle, the heading and its line. */
@Composable
private fun EmptyNotifications() {
    val colors = HogwartsTheme.colors
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.clip(HogwartsShapes.Pill).background(colors.muted).padding(16.dp)) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.notifications_empty_title), style = HogwartsTheme.type.section, color = colors.foreground)
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.notifications_empty_description),
            style = HogwartsTheme.type.body,
            color = colors.mutedForeground,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 250.dp),
        )
    }
}

@Composable
private fun LoadFailed(onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.notifications_load_failed), style = HogwartsTheme.type.body, color = HogwartsTheme.colors.mutedForeground)
        Spacer(Modifier.height(16.dp))
        PillButton(label = stringResource(R.string.notifications_retry), onClick = onRetry, variant = PillVariant.Outline)
    }
}

/** `NotificationCenterSkeleton`: six rows of avatar and lines. */
@Composable
private fun NotificationSkeleton() {
    val muted = HogwartsTheme.colors.muted
    Column(Modifier.fillMaxWidth()) {
        repeat(6) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(48.dp).clip(HogwartsShapes.Pill).background(muted))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.fillMaxWidth(0.8f).height(16.dp).clip(HogwartsShapes.Md).background(muted))
                    Box(Modifier.fillMaxWidth(0.6f).height(16.dp).clip(HogwartsShapes.Md).background(muted))
                    Box(Modifier.size(width = 96.dp, height = 12.dp).clip(HogwartsShapes.Md).background(muted))
                }
            }
        }
    }
}

/** `content.tsx` pagination: Previous, up to five page numbers, Next. */
@Composable
private fun Pagination(page: Int, totalPages: Int, onPage: (Int) -> Unit) {
    val numbers = when {
        totalPages <= 5 -> 1..totalPages
        page <= 3 -> 1..5
        page >= totalPages - 2 -> (totalPages - 4)..totalPages
        else -> (page - 2)..(page + 2)
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PagerButton(enabled = page > 1, onClick = { onPage(page - 1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(16.dp), tint = HogwartsTheme.colors.foreground)
            Text(stringResource(R.string.notifications_previous), style = HogwartsTheme.type.bodyMedium, color = HogwartsTheme.colors.foreground)
        }
        numbers.forEach { number ->
            val current = number == page
            Box(
                Modifier
                    .size(32.dp)
                    .clip(HogwartsShapes.Md)
                    .background(if (current) HogwartsTheme.colors.primary else HogwartsTheme.colors.background)
                    .border(1.dp, if (current) Color.Transparent else HogwartsTheme.colors.border, HogwartsShapes.Md)
                    .clickable(enabled = !current, role = Role.Button) { onPage(number) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    number.toString(),
                    style = HogwartsTheme.type.bodyMedium,
                    color = if (current) HogwartsTheme.colors.primaryForeground else HogwartsTheme.colors.foreground,
                )
            }
        }
        PagerButton(enabled = page < totalPages, onClick = { onPage(page + 1) }) {
            Text(stringResource(R.string.notifications_next), style = HogwartsTheme.type.bodyMedium, color = HogwartsTheme.colors.foreground)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = HogwartsTheme.colors.foreground)
        }
    }
}

@Composable
private fun PagerButton(enabled: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        Modifier
            .height(32.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(HogwartsShapes.Md)
            .border(1.dp, HogwartsTheme.colors.border, HogwartsShapes.Md)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) { content() }
}
