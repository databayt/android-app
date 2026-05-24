package org.hogwarts.android.feature.announcements.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.announcements.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAnnouncement: (String) -> Unit,
    viewModel: AnnouncementsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val formatter = viewModel.localeFormatter

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.announcements_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.announcements_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.announcements.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AppleInsetGroupedList(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                state = listState
            ) {
                // Type filter chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppleSpacing.Compact),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                    ) {
                        val allLabel = stringResource(R.string.announcements_filter_all)
                        val filters = listOf(null to allLabel) + AnnouncementType.entries.map {
                            it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
                        }
                        filters.forEach { (type, label) ->
                            FilterChip(
                                selected = uiState.selectedFilter == type,
                                onClick = { viewModel.onFilterChanged(type) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }

                if (uiState.announcements.isEmpty()) {
                    item {
                        EmptyState(
                            icon = HogwartsIcons.Notifications,
                            title = stringResource(R.string.announcements_no_announcements_title),
                            subtitle = stringResource(R.string.announcements_no_announcements_subtitle)
                        )
                    }
                } else {
                    // Group by important vs regular
                    val important = uiState.announcements.filter { it.isImportant }
                    val regular = uiState.announcements.filter { !it.isImportant }

                    if (important.isNotEmpty()) {
                        item {
                            AppleListSection(header = stringResource(R.string.announcements_section_important)) {
                                important.forEachIndexed { index, announcement ->
                                    AnnouncementRow(
                                        announcement = announcement,
                                        formatter = formatter,
                                        showDivider = index < important.size - 1,
                                        onClick = { onNavigateToAnnouncement(announcement.id) }
                                    )
                                }
                            }
                        }
                    }

                    if (regular.isNotEmpty()) {
                        item {
                            AppleListSection(header = stringResource(R.string.announcements_section_recent)) {
                                regular.forEachIndexed { index, announcement ->
                                    AnnouncementRow(
                                        announcement = announcement,
                                        formatter = formatter,
                                        showDivider = index < regular.size - 1,
                                        onClick = { onNavigateToAnnouncement(announcement.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        Text(
                            text = stringResource(R.string.announcements_showing_cached, uiState.error ?: ""),
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
private fun AnnouncementRow(
    announcement: Announcement,
    formatter: LocaleFormatter,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val typeColor = when (announcement.type) {
        AnnouncementType.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary
        AnnouncementType.EVENT -> MaterialTheme.colorScheme.tertiary
        AnnouncementType.NEWS -> MaterialTheme.colorScheme.secondary
        AnnouncementType.ALERT -> MaterialTheme.colorScheme.error
    }

    AppleListRow(
        showDivider = showDivider,
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    text = announcement.type.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    color = typeColor
                )
            }
            Text(
                text = announcement.content.take(120) + if (announcement.content.length > 120) "..." else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
            ) {
                Text(
                    text = formatter.formatDate(announcement.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                announcement.authorName?.let { author ->
                    Text(
                        text = stringResource(R.string.announcements_by_author, author),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                announcement.venue?.let { venue ->
                    Text(
                        text = stringResource(R.string.announcements_at_venue, venue),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
