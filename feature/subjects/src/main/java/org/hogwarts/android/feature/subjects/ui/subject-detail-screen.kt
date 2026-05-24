package org.hogwarts.android.feature.subjects.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.SubjectDetail
import org.hogwarts.android.feature.subjects.ui.components.AssignmentsSection
import org.hogwarts.android.feature.subjects.ui.components.ChapterSection
import org.hogwarts.android.feature.subjects.ui.components.ContentSection
import org.hogwarts.android.feature.subjects.ui.components.ExamsSection
import org.hogwarts.android.feature.subjects.ui.components.MaterialsSection
import org.hogwarts.android.feature.subjects.ui.components.QBankSection
import org.hogwarts.android.feature.subjects.ui.components.SubjectHero
import org.hogwarts.android.feature.subjects.ui.components.VideosSection
import org.hogwarts.android.feature.subjects.ui.components.gradeLabel
import org.hogwarts.android.feature.subjects.ui.components.levelLabel
import org.hogwarts.android.feature.subjects.ui.components.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.subjectDetail?.subject?.name
                            ?: stringResource(R.string.subjects_detail_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.subjects_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.subjectDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            uiState.subjectDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.error
                            ?: stringResource(R.string.subjects_detail_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                val detail = uiState.subjectDetail!!
                SubjectDetailContent(
                    detail = detail,
                    isArabic = isArabic,
                    innerPadding = innerPadding,
                    listStateKey = detail.subject.id,
                    error = uiState.error,
                    listState = listState,
                )
            }
        }
    }
}

@Composable
private fun SubjectDetailContent(
    detail: SubjectDetail,
    isArabic: Boolean,
    innerPadding: PaddingValues,
    listStateKey: String,
    error: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
) {
    val subject = detail.subject
    val heroSubtitle = buildHeroSubtitle(
        topics = detail.totalTopics.takeIf { it > 0 } ?: subject.totalLessons,
        chapters = subject.totalChapters,
    )
    val accentColor = parseColor(subject.color) ?: MaterialTheme.colorScheme.primary

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            horizontal = AppleSpacing.Standard,
            vertical = AppleSpacing.Compact,
        ),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard),
    ) {
        item(key = "hero") {
            SubjectHero(
                name = subject.name,
                imageUrl = detail.bannerUrl ?: subject.thumbnailUrl,
                color = subject.color,
                subtitle = heroSubtitle,
            )
        }

        item(key = "overview") {
            OverviewSection(detail = detail, isArabic = isArabic)
        }

        if (!subject.description.isNullOrBlank()) {
            item(key = "description") {
                SectionHeading(
                    text = stringResource(R.string.subjects_detail_description),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subject.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (detail.chapters.isEmpty()) {
            item(key = "empty_topics") {
                EmptyTopicsCard()
            }
        } else {
            items(
                items = detail.chapters,
                key = { it.id },
            ) { chapter ->
                ChapterSection(
                    chapter = chapter,
                    subjectFallbackColor = subject.color,
                )
            }
        }

        if (detail.videos.isNotEmpty()) {
            item(key = "section_videos") {
                ContentSection(
                    title = stringResource(R.string.subjects_section_videos),
                    actionLabel = stringResource(R.string.subjects_continue_watching),
                    onActionClick = {},
                ) {
                    VideosSection(
                        videos = detail.videos,
                        accentColor = accentColor,
                        viewsLabel = stringResource(R.string.subjects_views),
                    )
                }
            }
        }

        item(key = "section_materials") {
            ContentSection(
                title = stringResource(R.string.subjects_section_materials),
                actionLabel = stringResource(R.string.subjects_see_all),
                onActionClick = {},
            ) {
                MaterialsSection(
                    materials = detail.materials,
                    accentColor = accentColor,
                    textbookPdfUrl = null,
                    textbookCoverUrl = null,
                )
            }
        }

        item(key = "section_exams") {
            ContentSection(
                title = stringResource(R.string.subjects_section_exams),
                actionLabel = stringResource(R.string.subjects_see_all),
                onActionClick = {},
            ) {
                ExamsSection(exams = detail.exams)
            }
        }

        item(key = "section_qbank") {
            ContentSection(
                title = stringResource(R.string.subjects_section_qbank),
                actionLabel = stringResource(R.string.subjects_explore_qbank),
                onActionClick = {},
            ) {
                QBankSection(stats = detail.questionStats)
            }
        }

        item(key = "section_assignments") {
            ContentSection(
                title = stringResource(R.string.subjects_section_assignments),
            ) {
                AssignmentsSection(
                    assignments = detail.assignments,
                    accentColor = accentColor,
                    assignmentsEmptyLabel = stringResource(R.string.subjects_assignments_empty),
                )
            }
        }

        if (error != null) {
            item(key = "error") {
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun buildHeroSubtitle(topics: Int, chapters: Int): String? {
    val parts = buildList {
        if (chapters > 0) {
            add(stringResource(R.string.subjects_chapters_count, chapters))
        }
        if (topics > 0) {
            add(stringResource(R.string.subjects_topics_count, topics))
        }
    }
    return if (parts.isEmpty()) null else parts.joinToString(" • ")
}

@Composable
private fun OverviewSection(detail: SubjectDetail, isArabic: Boolean) {
    val subject = detail.subject
    SectionHeading(text = stringResource(R.string.subjects_detail_overview))
    Spacer(Modifier.height(8.dp))
    OverviewRow(
        label = stringResource(R.string.subjects_detail_department),
        value = subject.department,
    )
    if (subject.levels.isNotEmpty()) {
        val levelLabels = mutableListOf<String>()
        for (level in subject.levels) {
            levelLabels += levelLabel(level)
        }
        OverviewRow(
            label = stringResource(R.string.subjects_detail_levels),
            value = levelLabels.joinToString(" • "),
        )
    }
    val gradeText = gradeLabel(subject.grades, isArabic)
    if (!gradeText.isNullOrBlank()) {
        OverviewRow(
            label = stringResource(R.string.subjects_detail_grades),
            value = gradeText,
        )
    }
    if (!detail.curriculum.isNullOrBlank()) {
        OverviewRow(
            label = stringResource(R.string.subjects_detail_curriculum),
            value = detail.curriculum,
        )
    }
    if (subject.averageRating > 0f) {
        OverviewRow(
            label = stringResource(R.string.subjects_detail_rating),
            value = "%.1f (%d)".format(subject.averageRating, subject.ratingCount),
        )
    }
    if (detail.tags.isNotEmpty()) {
        OverviewRow(
            label = stringResource(R.string.subjects_detail_tags),
            value = detail.tags.joinToString(", "),
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun OverviewRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

@Composable
private fun EmptyTopicsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = HogwartsIcons.Subjects,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp),
            )
            Text(
                text = stringResource(R.string.subjects_detail_no_topics),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
