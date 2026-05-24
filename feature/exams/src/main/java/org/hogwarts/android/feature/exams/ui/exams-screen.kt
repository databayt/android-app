package org.hogwarts.android.feature.exams.ui

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
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.exams.domain.model.ExamStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExam: (String) -> Unit,
    viewModel: ExamsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val formatter = viewModel.localeFormatter

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exams_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.exams_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.exams.isEmpty()) {
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
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppleSpacing.Compact),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                    ) {
                        val filters = listOf(null to stringResource(R.string.exams_filter_all)) + ExamStatus.entries.map {
                            it.name to it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
                        }
                        filters.forEach { (status, label) ->
                            FilterChip(
                                selected = uiState.selectedStatusFilter == status,
                                onClick = { viewModel.onStatusFilterChanged(status) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                if (uiState.exams.isEmpty()) {
                    item {
                        EmptyState(
                            icon = HogwartsIcons.Exams,
                            title = stringResource(R.string.exams_no_exams_title),
                            subtitle = stringResource(R.string.exams_no_exams_subtitle)
                        )
                    }
                } else {
                    item {
                        AppleListSection(header = stringResource(R.string.exams_section_upcoming)) {
                            uiState.exams.forEachIndexed { index, exam ->
                                AppleListRow(
                                    showDivider = index < uiState.exams.size - 1,
                                    onClick = { onNavigateToExam(exam.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = exam.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${exam.subjectName} • ${formatter.formatDate(exam.date)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${formatter.formatTime(exam.startTime)} - ${formatter.formatTime(exam.endTime)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                                        ) {
                                            StatusBadge(
                                                text = exam.status.name.lowercase().replaceFirstChar { it.uppercase() },
                                                color = when (exam.status) {
                                                    ExamStatus.UPCOMING -> MaterialTheme.colorScheme.primary
                                                    ExamStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                                                    ExamStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                                                    ExamStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                                }
                                            )
                                            if (exam.result != null) {
                                                Text(
                                                    text = "${exam.result.marksObtained}/${exam.maxMarks ?: "—"}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        Text(
                            text = stringResource(R.string.exams_showing_cached, uiState.error ?: ""),
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
