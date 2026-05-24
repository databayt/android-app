package org.hogwarts.android.feature.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.FilterChipsRow
import org.hogwarts.android.feature.notifications.R
import org.hogwarts.android.feature.notifications.domain.NotificationConfig
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationPriority
import org.hogwarts.android.feature.notifications.domain.model.NotificationType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPreferences: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.notifications_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPreferences) {
                        Icon(
                            HogwartsIcons.Settings,
                            contentDescription = stringResource(R.string.notifications_preferences_title)
                        )
                    }
                    TextButton(onClick = { viewModel.markAllRead() }) {
                        Text(stringResource(R.string.notifications_read_all))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // All / Unread tabs (mirror web center.tsx tabs).
            FilterTabsRow(
                selected = uiState.selectedTab,
                unreadCount = uiState.unreadCount,
                totalCount = uiState.notifications.size,
                onSelected = viewModel::onTabSelected
            )

            // Type filter chips (mirror web filter bar).
            val typeChipKeys = listOf(ALL_KEY) + NotificationType.entries.map { it.name }
            val typeLabels = typeChipKeys.associateWith { key ->
                if (key == ALL_KEY) stringResource(R.string.notifications_filter_all_types)
                else stringResource(typeFilterLabelRes(NotificationType.valueOf(key)))
            }
            FilterChipsRow(
                chips = typeChipKeys.map { typeLabels.getValue(it) },
                selectedChip = typeLabels[uiState.selectedType?.name ?: ALL_KEY],
                onChipSelected = { label ->
                    val key = typeLabels.entries.firstOrNull { it.value == label }?.key
                    viewModel.onTypeFilterSelected(
                        if (key == null || key == ALL_KEY) null else NotificationType.valueOf(key)
                    )
                },
                showFadeEdges = false
            )

            if (uiState.isLoading && uiState.notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                return@Column
            }

            val filtered = uiState.filtered
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = HogwartsIcons.Notifications,
                        title = stringResource(R.string.notifications_no_notifications_title),
                        subtitle = stringResource(R.string.notifications_no_notifications_subtitle)
                    )
                }
                return@Column
            }

            // Group by Today / Yesterday / This Week / Earlier (mirror web grouping).
            val groups = filtered.groupBy { groupKeyFor(it.createdAt) }
            val groupOrder = listOf(GroupKey.TODAY, GroupKey.YESTERDAY, GroupKey.THIS_WEEK, GroupKey.EARLIER)

            AppleInsetGroupedList(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                groupOrder.forEach { key ->
                    val groupItems = groups[key].orEmpty()
                    if (groupItems.isEmpty()) return@forEach
                    item(key = "section-${key.name}") {
                        AppleListSection(header = stringResource(groupHeaderRes(key))) {
                            groupItems.forEachIndexed { index, n ->
                                NotificationRow(
                                    notification = n,
                                    timeLabel = timeFormatter.format(n.createdAt),
                                    showDivider = index < groupItems.size - 1,
                                    onClick = { viewModel.markRead(n.id) }
                                )
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item(key = "cached-banner") {
                        Text(
                            text = stringResource(R.string.notifications_showing_cached, uiState.error ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AppleSpacing.Standard)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabsRow(
    selected: NotificationConfig.FilterTab,
    unreadCount: Int,
    totalCount: Int,
    onSelected: (NotificationConfig.FilterTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppleSpacing.Standard, vertical = AppleSpacing.Tiny),
        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
    ) {
        FilterChip(
            selected = selected == NotificationConfig.FilterTab.ALL,
            onClick = { onSelected(NotificationConfig.FilterTab.ALL) },
            label = {
                BadgedBox(badge = { if (totalCount > 0) Badge { Text(totalCount.toString()) } }) {
                    Text(stringResource(R.string.notifications_tab_all))
                }
            }
        )
        FilterChip(
            selected = selected == NotificationConfig.FilterTab.UNREAD,
            onClick = { onSelected(NotificationConfig.FilterTab.UNREAD) },
            label = {
                BadgedBox(badge = {
                    if (unreadCount > 0) Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text(unreadCount.toString())
                    }
                }) {
                    Text(stringResource(R.string.notifications_tab_unread))
                }
            }
        )
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    timeLabel: String,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    AppleListRow(
        showDivider = showDivider,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small),
            verticalAlignment = Alignment.Top
        ) {
            // Urgent priority gets a destructive accent bar (mirrors web "border-s-destructive").
            if (notification.priority == NotificationPriority.URGENT) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.error)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                if (notification.priority == NotificationPriority.URGENT) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.notifications_priority_urgent)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }
    }
}

private const val ALL_KEY = "__ALL__"

private enum class GroupKey { TODAY, YESTERDAY, THIS_WEEK, EARLIER }

private fun groupKeyFor(at: Instant): GroupKey {
    val today = LocalDate.now(ZoneId.systemDefault())
    val that = at.atZone(ZoneId.systemDefault()).toLocalDate()
    val days = ChronoUnit.DAYS.between(that, today)
    return when {
        days <= 0L -> GroupKey.TODAY
        days == 1L -> GroupKey.YESTERDAY
        days < 7L -> GroupKey.THIS_WEEK
        else -> GroupKey.EARLIER
    }
}

private fun groupHeaderRes(key: GroupKey): Int = when (key) {
    GroupKey.TODAY -> R.string.notifications_group_today
    GroupKey.YESTERDAY -> R.string.notifications_group_yesterday
    GroupKey.THIS_WEEK -> R.string.notifications_group_this_week
    GroupKey.EARLIER -> R.string.notifications_group_earlier
}

private fun typeFilterLabelRes(type: NotificationType) = when (type) {
    NotificationType.ANNOUNCEMENT -> R.string.notifications_type_announcement
    NotificationType.ATTENDANCE -> R.string.notifications_type_attendance
    NotificationType.GRADE -> R.string.notifications_type_grade
    NotificationType.FEE -> R.string.notifications_type_fee
    NotificationType.MESSAGE -> R.string.notifications_type_message
    NotificationType.TIMETABLE -> R.string.notifications_type_timetable
    NotificationType.GENERAL -> R.string.notifications_type_general
}
