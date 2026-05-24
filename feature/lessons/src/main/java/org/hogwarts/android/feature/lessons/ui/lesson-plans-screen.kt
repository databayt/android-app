package org.hogwarts.android.feature.lessons.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.lessons.R
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan
import org.hogwarts.android.feature.lessons.domain.model.LessonPlanStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPlansScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLesson: (String) -> Unit,
    onNavigateToCurriculum: () -> Unit,
    onNavigateToResources: () -> Unit,
    onNavigateToForm: () -> Unit,
    viewModel: LessonPlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lessons_plans_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.lessons_back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToResources) {
                        Icon(Icons.Default.Folder, contentDescription = stringResource(R.string.lessons_resources_action))
                    }
                    IconButton(onClick = onNavigateToCurriculum) {
                        Icon(Icons.Default.Map, contentDescription = stringResource(R.string.lessons_curriculum_action))
                    }
                    IconButton(onClick = onNavigateToForm) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.lessons_new_plan_action))
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
                // Subject filter chips
                if (uiState.availableSubjects.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedSubjectName == null,
                                onClick = { viewModel.selectSubject(null) },
                                label = { Text(stringResource(R.string.lessons_filter_all_subjects)) }
                            )
                        }
                        items(uiState.availableSubjects) { subject ->
                            FilterChip(
                                selected = uiState.selectedSubjectName == subject,
                                onClick = { viewModel.selectSubject(subject) },
                                label = { Text(subject) }
                            )
                        }
                    }
                }

                // Class filter chips
                if (uiState.availableClasses.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedClassId == null,
                                onClick = { viewModel.selectClass(null) },
                                label = { Text(stringResource(R.string.lessons_filter_all_classes)) }
                            )
                        }
                        items(uiState.availableClasses) { classId ->
                            FilterChip(
                                selected = uiState.selectedClassId == classId,
                                onClick = { viewModel.selectClass(classId) },
                                label = { Text(classId) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.filteredPlans.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Default.MenuBook,
                        title = stringResource(R.string.lessons_no_plans_title),
                        subtitle = stringResource(R.string.lessons_no_plans_subtitle),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        items(uiState.filteredPlans, key = { it.id }) { plan ->
                            LessonPlanCard(
                                plan = plan,
                                onClick = { onNavigateToLesson(plan.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonPlanCard(
    plan: LessonPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.topic,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    text = plan.status.displayName(),
                    color = plan.status.color()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${plan.subjectName} - ${plan.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${plan.objectives.size} objectives",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${plan.resources.size} resources",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonPlanStatus.displayName(): String = when (this) {
    LessonPlanStatus.DRAFT -> "Draft"
    LessonPlanStatus.PUBLISHED -> "Published"
    LessonPlanStatus.COMPLETED -> "Completed"
}

@Composable
private fun LessonPlanStatus.color() = when (this) {
    LessonPlanStatus.DRAFT -> MaterialTheme.colorScheme.outline
    LessonPlanStatus.PUBLISHED -> MaterialTheme.colorScheme.primary
    LessonPlanStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
}
