package org.hogwarts.android.feature.subjects.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.HogwartsSearchBar
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.ui.components.SubjectCard
import org.hogwarts.android.feature.subjects.ui.components.gradeLabel
import org.hogwarts.android.feature.subjects.ui.components.levelLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubject: (String) -> Unit,
    viewModel: SubjectsListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subjects_title)) },
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
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading && uiState.subjects.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = AppleSpacing.Standard,
                            vertical = AppleSpacing.Compact,
                        ),
                        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
                    ) {
                        item(key = "search") {
                            HogwartsSearchBar(
                                query = uiState.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChanged,
                                placeholder = stringResource(
                                    R.string.subjects_search_placeholder,
                                ),
                            )
                        }

                        if (uiState.subjects.isEmpty()) {
                            item(key = "empty") {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.subjects_no_subjects_found,
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        } else {
                            items(
                                items = uiState.subjects,
                                key = { it.id },
                            ) { subject ->
                                SubjectCard(
                                    subject = subject,
                                    levelLabel = levelLabel(subject.primaryLevel),
                                    gradeLabel = gradeLabel(subject.grades, isArabic),
                                    onClick = { onNavigateToSubject(subject.id) },
                                )
                            }
                        }

                        if (uiState.error != null) {
                            item(key = "error") {
                                Text(
                                    text = uiState.error ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(AppleSpacing.Standard),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
