package org.hogwarts.android.feature.subjects.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.SubjectDetail
import org.hogwarts.android.feature.subjects.ui.components.AssignmentTiles
import org.hogwarts.android.feature.subjects.ui.components.BookTile
import org.hogwarts.android.feature.subjects.ui.components.ChaptersRow
import org.hogwarts.android.feature.subjects.ui.components.ExamTiles
import org.hogwarts.android.feature.subjects.ui.components.MaterialsBoard
import org.hogwarts.android.feature.subjects.ui.components.PageRule
import org.hogwarts.android.feature.subjects.ui.components.PageSectionHeader
import org.hogwarts.android.feature.subjects.ui.components.QBankSection
import org.hogwarts.android.feature.subjects.ui.components.SubjectPageHero
import org.hogwarts.android.feature.subjects.ui.components.VideosShelf
import org.hogwarts.android.feature.subjects.ui.components.gradeLine
import org.hogwarts.android.feature.subjects.ui.components.stageLine
import org.hogwarts.android.feature.subjects.ui.components.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenHref: (String) -> Unit = {},
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
                    onOpenHref = onOpenHref,
                )
            }
        }
    }
}

/**
 * `/subjects/[slug]` as the web lays it out on a phone (`[slug]/layout.tsx`
 * + `page.tsx`): the banner, a rule, the chapters row, then the content
 * sections in `catalog-content-sections.tsx` order — materials, videos,
 * exams, question bank, assignments. Videos, exams and assignments appear
 * only when the subject has some; the web drops those headings when empty.
 *
 * Every "see all" and every tile opens the web page it links to on the site,
 * through the shell's href opener — the same destinations, in the same
 * order, rather than native screens the web does not have.
 */
@Composable
private fun SubjectDetailContent(
    detail: SubjectDetail,
    isArabic: Boolean,
    innerPadding: PaddingValues,
    listStateKey: String,
    error: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpenHref: (String) -> Unit,
) {
    val subject = detail.subject
    val accentColor = parseColor(subject.color) ?: Color(0xFF1E40AF)
    val slug = subject.slug
    val materialsHref = "/subjects/$slug/materials"
    val qbankHref = "/exams/qbank?catalogSubjectId=${subject.id}"
    val examsHref = "/exams/upcoming?catalogSubjectId=${subject.id}"
    val videosHref = "/lumos/dashboard/$slug"
    val level = subject.levels.firstOrNull()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item(key = "hero") {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(Modifier.padding(horizontal = 8.dp)) {
                    SubjectPageHero(
                        name = subject.name,
                        imageUrl = detail.bannerUrl ?: subject.thumbnailUrl,
                        color = subject.color,
                        chapters = subject.totalChapters,
                        lessons = subject.totalLessons,
                    )
                }
                PageRule(Modifier.padding(horizontal = 16.dp))
            }
        }

        item(key = "chapters") {
            Box(Modifier.padding(horizontal = 16.dp)) {
                if (detail.chapters.isEmpty()) {
                    EmptyTopicsCard()
                } else {
                    ChaptersRow(
                        chapters = detail.chapters,
                        subjectImageUrl = subject.thumbnailUrl,
                        subjectColor = subject.color,
                        onOpenChapters = { onOpenHref("/subjects/$slug/chapters") },
                    )
                }
            }
        }

        item(key = "materials") {
            PageSection(
                title = stringResource(R.string.subjects_section_materials),
                actionLabel = stringResource(R.string.subjects_see_all),
                onAction = { onOpenHref(materialsHref) },
            ) {
                MaterialsBoard(
                    tiles = listOf(
                        // The book itself opens the reader; the web's summary
                        // tile has no page yet, so it is drawn but inert.
                        BookTile(null, Color.Transparent, detail.textbookReaderHref?.let { href -> { onOpenHref(href) } }),
                        BookTile(stringResource(R.string.subjects_page_summary), Color(0xFF10B981), null),
                        BookTile(stringResource(R.string.subjects_page_qbank_book), Color(0xFF0284C7)) { onOpenHref(qbankHref) },
                        BookTile(stringResource(R.string.subjects_page_exams_book), Color(0xFFE11D48)) { onOpenHref(examsHref) },
                        BookTile(stringResource(R.string.subjects_page_references_book), Color(0xFF7C3AED)) {
                            onOpenHref("$materialsHref#material-type-REFERENCE")
                        },
                    ),
                    coverUrl = detail.textbookCoverUrl,
                    accentColor = accentColor,
                    stage = stageLine(level),
                    title = subject.name,
                    grade = gradeLine(subject.grades.firstOrNull(), level),
                )
            }
        }

        if (detail.videos.isNotEmpty()) {
            item(key = "videos") {
                PageSection(
                    title = stringResource(R.string.subjects_section_videos),
                    actionLabel = stringResource(R.string.subjects_continue_watching),
                    onAction = { onOpenHref(videosHref) },
                ) {
                    VideosShelf(
                        videos = detail.videos,
                        accentColor = accentColor,
                        onOpen = { video -> onOpenHref("$videosHref/${video.catalogLessonId}") },
                    )
                }
            }
        }

        if (detail.exams.isNotEmpty()) {
            item(key = "exams") {
                PageSection(
                    title = stringResource(R.string.subjects_section_exams),
                    actionLabel = stringResource(R.string.subjects_see_all),
                    onAction = { onOpenHref(examsHref) },
                ) {
                    ExamTiles(exams = detail.exams, onOpen = { onOpenHref(examsHref) })
                }
            }
        }

        item(key = "qbank") {
            PageSection(
                title = stringResource(R.string.subjects_section_qbank),
                actionLabel = stringResource(R.string.subjects_explore_qbank),
                onAction = { onOpenHref(qbankHref) },
            ) {
                QBankSection(stats = detail.questionStats)
            }
        }

        if (detail.assignments.isNotEmpty()) {
            item(key = "assignments") {
                PageSection(title = stringResource(R.string.subjects_section_assignments)) {
                    AssignmentTiles(assignments = detail.assignments, accentColor = accentColor)
                }
            }
        }

        if (error != null) {
            item(key = "error") {
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

/** A section: its header in the gutter, its shelf running to the edge. */
@Composable
private fun PageSection(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.padding(horizontal = 16.dp)) {
            PageSectionHeader(title = title, actionLabel = actionLabel, onAction = onAction)
        }
        Box(Modifier.padding(start = 16.dp)) { content() }
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
