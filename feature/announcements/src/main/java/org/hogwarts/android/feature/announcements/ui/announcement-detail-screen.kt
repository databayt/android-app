package org.hogwarts.android.feature.announcements.ui

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.announcements.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnnouncementDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.announcements_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.announcements_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.announcement == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.announcement == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FormError(message = uiState.error ?: stringResource(R.string.announcements_detail_load_failed))
                }
            }
            uiState.announcement != null -> {
                AnnouncementDetailContent(
                    announcement = uiState.announcement!!,
                    formatter = viewModel.localeFormatter,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun AnnouncementDetailContent(
    announcement: Announcement,
    formatter: LocaleFormatter,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    AppleInsetGroupedList(
        modifier = modifier,
        state = listState
    ) {
        // Header Section
        item {
            AppleListSection {
                AppleListRow(showDivider = false) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = announcement.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = when (announcement.type) {
                                    AnnouncementType.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary
                                    AnnouncementType.EVENT -> MaterialTheme.colorScheme.secondary
                                    AnnouncementType.NEWS -> MaterialTheme.colorScheme.tertiary
                                    AnnouncementType.ALERT -> MaterialTheme.colorScheme.error
                                }
                            )
                            if (announcement.isImportant) {
                                StatusBadge(
                                    text = stringResource(R.string.announcements_detail_important),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                        ) {
                            Text(
                                text = formatter.formatDateLong(announcement.date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (announcement.authorName != null) {
                                Text(
                                    text = stringResource(R.string.announcements_by_author, announcement.authorName),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Content Section
        item {
            AppleListSection(header = stringResource(R.string.announcements_detail_section_details)) {
                AppleListRow(showDivider = false) {
                    Text(
                        text = announcement.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Event Details (if applicable)
        if (announcement.venue != null || announcement.startTime != null) {
            item {
                AppleListSection(header = stringResource(R.string.announcements_detail_section_event)) {
                    if (announcement.venue != null) {
                        AppleListRow(showDivider = announcement.startTime != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.announcements_detail_venue),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = announcement.venue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (announcement.startTime != null) {
                        AppleListRow(showDivider = false) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.announcements_detail_time),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (announcement.endTime != null) {
                                        stringResource(
                                            R.string.announcements_detail_time_range,
                                            formatter.formatTime(announcement.startTime),
                                            formatter.formatTime(announcement.endTime)
                                        )
                                    } else {
                                        formatter.formatTime(announcement.startTime)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Attachment Section
        if (!announcement.attachmentUrl.isNullOrBlank()) {
            item {
                AppleListSection(header = stringResource(R.string.announcements_detail_section_attachment)) {
                    AppleListRow(showDivider = false) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(announcement.attachmentUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                HogwartsIcons.Download,
                                contentDescription = null,
                                modifier = Modifier.padding(end = AppleSpacing.Compact)
                            )
                            Text(stringResource(R.string.announcements_detail_view_attachment))
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun AnnouncementDetailPreview() {
    HogwartsTheme {
        AnnouncementDetailContent(
            announcement = Announcement(
                id = "1",
                title = "Parent-Teacher Conference",
                content = "We are pleased to invite all parents and guardians to our annual Parent-Teacher Conference scheduled for next week. Please ensure you attend the designated time slot for your child's class.",
                type = AnnouncementType.EVENT,
                authorName = "Principal Ahmed",
                date = LocalDate.now(),
                venue = "Main Hall",
                isImportant = true,
                attachmentUrl = "https://example.com/schedule.pdf"
            ),
            formatter = LocaleFormatter(LocalContext.current)
        )
    }
}
