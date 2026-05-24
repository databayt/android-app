package org.hogwarts.android.feature.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.stream.R
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.ui.components.StreamCoursesHeroSection
import org.hogwarts.android.feature.stream.ui.components.StreamGradeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCourse: (String) -> Unit,
    viewModel: CourseCatalogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.stream_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero — mirrors web stream/courses/content.tsx
                item(span = { GridItemSpan(maxLineSpan) }) {
                    StreamCoursesHeroSection(
                        title = stringResource(R.string.stream_courses_title)
                    )
                }

                // Search bar
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = { Text(stringResource(R.string.stream_search_courses)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(999.dp)
                    )
                }

                // Grade filter — 1..12 pills. Hidden when the entry path locked
                // the grade (student direct-from-home), since the student isn't
                // meant to switch grades from this screen.
                if (!uiState.gradeFilterLocked) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        StreamGradeFilter(
                            activeGrade = uiState.activeGrade,
                            onGradeSelect = viewModel::selectGrade,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (uiState.filteredCourses.isEmpty() && !uiState.isLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = stringResource(R.string.stream_no_courses_title),
                            subtitle = stringResource(R.string.stream_no_courses_subtitle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                        )
                    }
                } else {
                    items(
                        items = uiState.filteredCourses,
                        key = { it.id },
                        span = { GridItemSpan(1) }
                    ) { course ->
                        CatalogCourseCard(
                            course = course,
                            onClick = { onNavigateToCourse(course.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Catalog card — web parity with stream/courses/course-card.tsx.
 * Shows thumbnail, level label, title, course type (by chapter count), rating + enrollments.
 */
@Composable
private fun CatalogCourseCard(
    course: Course,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = course.thumbnailUrl,
            contentDescription = course.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(12.dp))

        // Label prefers the first grade in the list — matches web parity where the
        // card shows a single level chip. Courses without grades fall back to the
        // category name so the slot isn't empty.
        val levelLabel = course.grades.firstOrNull()?.let {
            stringResource(R.string.stream_courses_grade_label, it)
        } ?: course.category
        Text(
            text = levelLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))

        Text(
            text = course.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))

        Text(
            text = courseTypeFor(course.lessonCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${course.enrollmentCount}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Matches web's getCourseTypeKey. Uses lessonCount as a proxy for chapter depth
 * since the mobile catalog list doesn't hydrate chapters.
 */
private fun courseTypeFor(lessonCount: Int): String = when {
    lessonCount >= 30 -> "Professional Certificate"
    lessonCount >= 15 -> "Specialization"
    lessonCount >= 9 -> "Course"
    else -> "Short Course"
}
