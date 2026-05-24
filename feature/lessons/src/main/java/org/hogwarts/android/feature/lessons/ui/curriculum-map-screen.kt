package org.hogwarts.android.feature.lessons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.lessons.R
import org.hogwarts.android.feature.lessons.domain.model.CurriculumSubject
import org.hogwarts.android.feature.lessons.domain.model.CurriculumTopic
import org.hogwarts.android.feature.lessons.domain.model.TopicStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurriculumMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: CurriculumMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lessons_curriculum_map_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.lessons_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Term selector
                if (uiState.terms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.terms) { term ->
                            FilterChip(
                                selected = uiState.selectedTermId == term.termId,
                                onClick = { viewModel.selectTerm(term.termId) },
                                label = { Text(term.termName) }
                            )
                        }
                    }
                }

                val selectedTerm = uiState.selectedTerm
                if (selectedTerm == null && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Default.MenuBook,
                        title = "No Curriculum Data",
                        subtitle = "No curriculum maps available.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedTerm != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        items(selectedTerm.subjects, key = { it.subjectId }) { subject ->
                            SubjectCurriculumCard(subject = subject)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectCurriculumCard(
    subject: CurriculumSubject,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subject.subjectName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Progress summary
            val completed = subject.topics.count { it.status == TopicStatus.COMPLETED }
            val total = subject.topics.size
            Text(
                text = "$completed/$total topics completed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Topic rows with progress dots
            subject.topics.forEach { topic ->
                TopicRow(topic = topic)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TopicRow(topic: CurriculumTopic) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Progress dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    when (topic.status) {
                        TopicStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        TopicStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                        TopicStatus.NOT_STARTED -> MaterialTheme.colorScheme.outlineVariant
                    }
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topic.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Week ${topic.weekNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = when (topic.status) {
                TopicStatus.COMPLETED -> "Done"
                TopicStatus.IN_PROGRESS -> "In Progress"
                TopicStatus.NOT_STARTED -> "Upcoming"
            },
            style = MaterialTheme.typography.labelSmall,
            color = when (topic.status) {
                TopicStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                TopicStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                TopicStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
