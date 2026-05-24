package org.hogwarts.android.feature.grades.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import org.hogwarts.android.feature.grades.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.grades.ui.components.GradeRow

/**
 * Grades screen showing exam/assignment results with filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    onNavigateBack: () -> Unit,
    viewModel: GradesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.grades_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.grades_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val filteredRecords = viewModel.getFilteredRecords()

            AppleInsetGroupedList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = listState
            ) {
                // Filter chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppleSpacing.Compact),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                    ) {
                        GradesFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = uiState.selectedFilter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = {
                                    Text(
                                        text = stringResource(filter.labelResId),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }

                if (filteredRecords.isEmpty()) {
                    item {
                        EmptyState(
                            icon = HogwartsIcons.Grades,
                            title = stringResource(R.string.grades_empty_title),
                            subtitle = stringResource(R.string.grades_empty_subtitle)
                        )
                    }
                } else {
                    // Grade records
                    item {
                        AppleListSection(header = stringResource(R.string.grades_section_results)) {
                            filteredRecords.forEachIndexed { index, record ->
                                GradeRow(
                                    record = record,
                                    showDivider = index < filteredRecords.size - 1
                                )
                            }
                        }
                    }
                }

                // Error message
                if (uiState.error != null) {
                    item {
                        Text(
                            text = stringResource(R.string.grades_showing_cached_data, uiState.error ?: ""),
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
