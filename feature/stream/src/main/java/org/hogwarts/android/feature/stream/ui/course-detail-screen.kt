package org.hogwarts.android.feature.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.hogwarts.android.feature.stream.R
import org.hogwarts.android.feature.stream.domain.model.Chapter
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChapters: (String) -> Unit,
    onNavigateToVideoLesson: (String, String) -> Unit,
    onNavigateToTextLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit,
    onNavigateToProgress: (String) -> Unit,
    onNavigateToCertificate: (String) -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Web parity: enroll → auto-open first lesson. The ViewModel emits OpenLesson
    // right after a successful enrollment.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CourseDetailEvent.OpenLesson -> routeToLesson(
                    courseId = event.courseId,
                    lesson = event.lesson,
                    onVideo = onNavigateToVideoLesson,
                    onText = onNavigateToTextLesson,
                    onQuiz = onNavigateToQuiz
                )
            }
        }
    }

    val course = uiState.course

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
        bottomBar = {
            // Web's sticky bottom CTA (lg:hidden) — always visible on mobile.
            if (course != null) {
                CourseDetailBottomCta(
                    isEnrolled = uiState.isEnrolled,
                    isEnrolling = uiState.isEnrolling,
                    onEnroll = viewModel::enroll,
                    onContinue = viewModel::continueLearning
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (course == null) return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Hero ---------------------------------------------------------
            item {
                CourseDetailHero(
                    course = course,
                    isEnrolled = uiState.isEnrolled,
                    totalLessons = uiState.totalLessons,
                    totalDuration = course.totalDuration
                )
            }

            // --- About card ---------------------------------------------------
            item {
                CourseAboutCard(course = course, chapters = uiState.chapters)
            }

            // --- Chapters ----------------------------------------------------
            item {
                Text(
                    text = stringResource(R.string.stream_detail_chapters_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            val nonEmptyChapters = uiState.chapters.filter { it.lessons?.isNotEmpty() == true }
            items(items = nonEmptyChapters, key = { it.id }) { chapter ->
                ChapterSection(
                    chapter = chapter,
                    onLessonClick = { lesson ->
                        routeToLesson(
                            courseId = viewModel.courseId,
                            lesson = lesson,
                            onVideo = onNavigateToVideoLesson,
                            onText = onNavigateToTextLesson,
                            onQuiz = onNavigateToQuiz
                        )
                    }
                )
            }

            // Fallback when chapter lessons aren't hydrated — keep the old
            // "tap to open chapter list" affordance so the screen is never
            // a dead-end.
            if (nonEmptyChapters.isEmpty() && uiState.chapters.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(
                            R.string.stream_view_all_chapters_count,
                            uiState.chapters.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clickable { onNavigateToChapters(viewModel.courseId) }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Hero
// ---------------------------------------------------------------------------

@Composable
private fun CourseDetailHero(
    course: Course,
    isEnrolled: Boolean,
    totalLessons: Int,
    totalDuration: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Breadcrumb — Stream / Courses
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.stream_detail_breadcrumb_stream),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "/",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.stream_detail_breadcrumb_courses),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Thumbnail with play overlay (web's "click to preview" affordance)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (!course.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = course.thumbnailUrl,
                    contentDescription = course.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Title
        Text(
            text = course.title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).dp.let { androidx.compose.ui.unit.TextUnit(-0.5f, androidx.compose.ui.unit.TextUnitType.Sp) }
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Description
        Text(
            text = course.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Progress bar when enrolled (web parity)
        if (isEnrolled && course.progress > 0f) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { course.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.stream_percent_complete,
                        (course.progress * 100).toInt()
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Stats row — lectures · duration · quiz · certificate
        HeroStatsRow(
            totalLessons = totalLessons,
            totalDuration = totalDuration
        )
    }
}

@Composable
private fun HeroStatsRow(totalLessons: Int, totalDuration: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip(
            value = totalLessons.toString(),
            label = stringResource(R.string.stream_detail_stat_lectures)
        )
        StatChip(
            value = totalDuration.ifBlank { "—" },
            label = stringResource(R.string.stream_detail_stat_min_video)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.stream_detail_stat_certificate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// About card
// ---------------------------------------------------------------------------

@Composable
private fun CourseAboutCard(course: Course, chapters: List<Chapter>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.stream_detail_about_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = course.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (chapters.isNotEmpty()) {
            AboutFieldBlock(
                title = stringResource(R.string.stream_detail_learning_objectives),
                lead = stringResource(R.string.stream_detail_by_the_end),
                bullets = chapters.map { it.title }
            )
        }

        AboutFieldBlock(
            title = stringResource(R.string.stream_detail_prerequisites),
            lead = null,
            bullets = listOf(stringResource(R.string.stream_detail_no_prerequisites))
        )

        AboutFieldBlock(
            title = stringResource(R.string.stream_detail_who_is_for),
            lead = null,
            bullets = listOf(stringResource(R.string.stream_detail_default_audience))
        )
    }
}

@Composable
private fun AboutFieldBlock(title: String, lead: String?, bullets: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (lead != null) {
            Text(
                text = lead,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        bullets.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Chapter section (web: h3 + grid of CourseLessonCard)
// ---------------------------------------------------------------------------

@Composable
private fun ChapterSection(
    chapter: Chapter,
    onLessonClick: (Lesson) -> Unit
) {
    val lessons = chapter.lessons ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        lessons.forEach { lesson ->
            LessonRow(lesson = lesson, onClick = { onLessonClick(lesson) })
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !lesson.isLocked, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Square thumbnail — web's `h-18 w-18 rounded-sm` lesson tile. The
        // image fills the tile; if the lesson has no thumbnail we keep a
        // surface-tinted square with a small type icon so the row shape stays
        // identical across rows.
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!lesson.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = lesson.thumbnailUrl,
                    contentDescription = lesson.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = when (lesson.type) {
                        LessonType.VIDEO -> Icons.Default.PlayArrow
                        LessonType.TEXT -> Icons.Default.Check
                        LessonType.QUIZ -> Icons.Default.Check
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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

// ---------------------------------------------------------------------------
// Sticky bottom CTA — mirrors web's lg:hidden bottom bar
// ---------------------------------------------------------------------------

@Composable
private fun CourseDetailBottomCta(
    isEnrolled: Boolean,
    isEnrolling: Boolean,
    onEnroll: () -> Unit,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isEnrolled) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.stream_detail_free_badge),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .clickable(enabled = !isEnrolling) {
                    if (isEnrolled) onContinue() else onEnroll()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isEnrolling) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.stream_detail_processing),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = stringResource(
                        if (isEnrolled) R.string.stream_detail_continue_learning
                        else R.string.stream_detail_enroll_free
                    ),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun routeToLesson(
    courseId: String,
    lesson: Lesson,
    onVideo: (String, String) -> Unit,
    onText: (String, String) -> Unit,
    onQuiz: (String, String) -> Unit
) {
    when (lesson.type) {
        LessonType.VIDEO -> onVideo(courseId, lesson.id)
        LessonType.TEXT -> onText(courseId, lesson.id)
        LessonType.QUIZ -> onQuiz(courseId, lesson.id)
    }
}

