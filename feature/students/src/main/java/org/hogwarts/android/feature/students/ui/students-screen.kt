package org.hogwarts.android.feature.students.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.students.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.HogwartsSearchBar
import org.hogwarts.android.feature.students.ui.components.StudentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStudent: (String) -> Unit,
    viewModel: StudentsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.students_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.students_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.students.isEmpty()) {
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
                    HogwartsSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        placeholder = stringResource(R.string.students_search_placeholder)
                    )
                }

                if (uiState.students.isEmpty()) {
                    item {
                        EmptyState(
                            icon = HogwartsIcons.Students,
                            title = stringResource(R.string.students_empty_title),
                            subtitle = if (uiState.searchQuery.isNotEmpty())
                                stringResource(R.string.students_empty_search)
                            else
                                stringResource(R.string.students_empty_none)
                        )
                    }
                } else {
                    item {
                        AppleListSection(
                            header = stringResource(R.string.students_count, uiState.students.size)
                        ) {
                            uiState.students.forEachIndexed { index, student ->
                                StudentCard(
                                    student = student,
                                    showDivider = index < uiState.students.size - 1,
                                    onClick = { onNavigateToStudent(student.id) }
                                )
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        Text(
                            text = stringResource(R.string.students_cached_data, uiState.error ?: ""),
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
