package org.hogwarts.android.feature.reportcards.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.reportcards.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import org.hogwarts.android.feature.reportcards.domain.model.ReportCardStatus

/**
 * Report Cards list screen.
 *
 * Shows report cards grouped by academic year/term with GPA, rank, and status badges.
 * For guardians: includes a child selector at the top to switch between children.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCardsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ReportCardsListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_cards_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.report_cards_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.reportCards.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: stringResource(R.string.report_cards_something_went_wrong),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.report_cards_tap_to_retry),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = AppleSpacing.Compact)
                                .clickable { viewModel.retry() }
                        )
                    }
                }
            }
            else -> {
                ReportCardsListContent(
                    uiState = uiState,
                    isGuardian = uiState.isGuardian,
                    onSelectChild = { viewModel.selectChild(it) },
                    onCardClick = onNavigateToDetail,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ReportCardsListContent(
    uiState: ReportCardsListUiState,
    isGuardian: Boolean,
    onSelectChild: (String) -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    AppleInsetGroupedList(
        modifier = modifier,
        state = listState
    ) {
        // Guardian child selector
        if (isGuardian && uiState.children.size > 1) {
            item {
                AppleListSection(header = stringResource(R.string.report_cards_select_child)) {
                    uiState.children.forEachIndexed { index, child ->
                        AppleListRow(
                            showDivider = index < uiState.children.size - 1
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectChild(child.id) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = child.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (child.id == uiState.selectedChildId) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (child.id == uiState.selectedChildId) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                                if (child.id == uiState.selectedChildId) {
                                    Icon(
                                        imageVector = HogwartsIcons.Check,
                                        contentDescription = stringResource(R.string.report_cards_selected),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Report cards grouped by academic year
        val groupedByYear = uiState.reportCards.groupBy { it.academicYear }

        if (uiState.reportCards.isEmpty()) {
            item {
                EmptyState(
                    icon = HogwartsIcons.Grades,
                    title = stringResource(R.string.report_cards_empty_title),
                    subtitle = stringResource(R.string.report_cards_empty_subtitle)
                )
            }
        } else {
            groupedByYear.forEach { (year, cards) ->
                item {
                    AppleListSection(header = year) {
                        cards.forEachIndexed { index, reportCard ->
                            AppleListRow(
                                showDivider = index < cards.size - 1
                            ) {
                                ReportCardRow(
                                    reportCard = reportCard,
                                    onClick = { onCardClick(reportCard.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error banner when showing cached data
        if (uiState.error != null && uiState.reportCards.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.report_cards_showing_cached, uiState.error ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(AppleSpacing.Standard)
                )
            }
        }
    }
}

/**
 * Single report card row in the list.
 */
@Composable
private fun ReportCardRow(
    reportCard: ReportCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppleSpacing.Compact)
    ) {
        // Term name and status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reportCard.termName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(
                text = reportCard.status.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                color = when (reportCard.status) {
                    ReportCardStatus.PUBLISHED -> MaterialTheme.colorScheme.primary
                    ReportCardStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // GPA and rank info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppleSpacing.Compact),
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
        ) {
            if (reportCard.gpa != null) {
                Text(
                    text = stringResource(R.string.report_cards_gpa_label, "%.2f".format(reportCard.gpa)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            if (reportCard.rankDisplay != null) {
                Text(
                    text = stringResource(R.string.report_cards_rank_label, reportCard.rankDisplay!!),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (reportCard.overallPercentage != null) {
                Text(
                    text = "${"%.1f".format(reportCard.overallPercentage)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Previews ---

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun ReportCardsListPreview() {
    HogwartsTheme {
        ReportCardsListContent(
            uiState = ReportCardsListUiState(
                isLoading = false,
                reportCards = listOf(
                    ReportCard(
                        id = "1",
                        studentId = "s1",
                        studentName = "Harry Potter",
                        termId = "t1",
                        termName = "Term 1",
                        academicYear = "2025-2026",
                        gpa = 3.85f,
                        rank = 3,
                        totalStudents = 45,
                        status = ReportCardStatus.PUBLISHED
                    ),
                    ReportCard(
                        id = "2",
                        studentId = "s1",
                        studentName = "Harry Potter",
                        termId = "t2",
                        termName = "Term 2",
                        academicYear = "2025-2026",
                        gpa = 3.92f,
                        rank = 2,
                        totalStudents = 45,
                        status = ReportCardStatus.DRAFT
                    ),
                    ReportCard(
                        id = "3",
                        studentId = "s1",
                        studentName = "Harry Potter",
                        termId = "t3",
                        termName = "Term 2",
                        academicYear = "2024-2025",
                        gpa = 3.70f,
                        rank = 5,
                        totalStudents = 44,
                        status = ReportCardStatus.PUBLISHED
                    )
                )
            ),
            isGuardian = false,
            onSelectChild = {},
            onCardClick = {}
        )
    }
}

@Preview(name = "Guardian View")
@Composable
private fun ReportCardsListGuardianPreview() {
    HogwartsTheme {
        ReportCardsListContent(
            uiState = ReportCardsListUiState(
                isLoading = false,
                isGuardian = true,
                children = listOf(
                    ChildInfo(id = "s1", name = "Harry Potter"),
                    ChildInfo(id = "s2", name = "Ginny Weasley")
                ),
                selectedChildId = "s1",
                reportCards = listOf(
                    ReportCard(
                        id = "1",
                        studentId = "s1",
                        studentName = "Harry Potter",
                        termId = "t1",
                        termName = "Term 1",
                        academicYear = "2025-2026",
                        gpa = 3.85f,
                        rank = 3,
                        totalStudents = 45,
                        status = ReportCardStatus.PUBLISHED
                    )
                )
            ),
            isGuardian = true,
            onSelectChild = {},
            onCardClick = {}
        )
    }
}

@Preview(name = "Empty State")
@Composable
private fun ReportCardsListEmptyPreview() {
    HogwartsTheme {
        ReportCardsListContent(
            uiState = ReportCardsListUiState(isLoading = false),
            isGuardian = false,
            onSelectChild = {},
            onCardClick = {}
        )
    }
}
