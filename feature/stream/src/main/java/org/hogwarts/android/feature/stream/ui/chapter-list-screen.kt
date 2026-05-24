package org.hogwarts.android.feature.stream.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import org.hogwarts.android.feature.stream.R
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVideoLesson: (String, String) -> Unit,
    onNavigateToTextLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit,
    viewModel: ChapterListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stream_chapters_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.stream_back))
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
            if (uiState.chapters.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    icon = Icons.Default.MenuBook,
                    title = stringResource(R.string.stream_no_chapters_title),
                    subtitle = stringResource(R.string.stream_no_chapters_subtitle),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.chapters, key = { it.chapter.id }) { cwl ->
                        ChapterAccordionItem(
                            chapterWithLessons = cwl,
                            courseId = viewModel.courseId,
                            onToggle = { viewModel.toggleChapter(cwl.chapter.id) },
                            onNavigateToVideoLesson = onNavigateToVideoLesson,
                            onNavigateToTextLesson = onNavigateToTextLesson,
                            onNavigateToQuiz = onNavigateToQuiz
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterAccordionItem(
    chapterWithLessons: ChapterWithLessons,
    courseId: String,
    onToggle: () -> Unit,
    onNavigateToVideoLesson: (String, String) -> Unit,
    onNavigateToTextLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit
) {
    val chapter = chapterWithLessons.chapter
    val progress = if (chapter.lessonCount > 0) {
        chapter.completedLessons.toFloat() / chapter.lessonCount
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Chapter header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.stream_chapter_number, chapter.orderIndex + 1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${chapter.completedLessons}/${chapter.lessonCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (chapterWithLessons.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (chapterWithLessons.isExpanded) stringResource(R.string.stream_collapse) else stringResource(R.string.stream_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded lessons
            AnimatedVisibility(visible = chapterWithLessons.isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    chapterWithLessons.lessons.forEach { lesson ->
                        LessonRow(
                            lesson = lesson,
                            onClick = {
                                if (!lesson.isLocked) {
                                    when (lesson.type) {
                                        LessonType.VIDEO -> onNavigateToVideoLesson(courseId, lesson.id)
                                        LessonType.TEXT -> onNavigateToTextLesson(courseId, lesson.id)
                                        LessonType.QUIZ -> onNavigateToQuiz(courseId, lesson.id)
                                    }
                                }
                            }
                        )
                    }
                    if (chapterWithLessons.lessons.isEmpty() && !chapterWithLessons.isLoadingLessons) {
                        Text(
                            text = stringResource(R.string.stream_no_lessons_available),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(
    lesson: Lesson,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !lesson.isLocked, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon
        val icon = when {
            lesson.isLocked -> Icons.Default.Lock
            lesson.isCompleted -> Icons.Default.CheckCircle
            lesson.type == LessonType.VIDEO -> Icons.Default.PlayCircle
            lesson.type == LessonType.QUIZ -> Icons.Default.Quiz
            else -> Icons.Default.MenuBook
        }
        val tint = when {
            lesson.isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            lesson.isCompleted -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (lesson.isLocked) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = lesson.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (lesson.duration.isNotBlank()) {
                    Text(
                        text = lesson.duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
