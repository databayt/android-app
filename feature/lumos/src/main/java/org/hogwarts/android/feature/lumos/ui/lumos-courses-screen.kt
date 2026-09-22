package org.hogwarts.android.feature.lumos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import org.hogwarts.android.core.designsystem.icon.ToolbarIcons
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.lumos.R
import org.hogwarts.android.feature.lumos.domain.model.CatalogCourse
import org.hogwarts.android.feature.lumos.domain.model.CourseTypeKey
import org.hogwarts.android.feature.lumos.domain.model.LessonInstructor
import org.hogwarts.android.feature.lumos.ui.components.LumosAssets

/**
 * `/lumos/courses` — where a student lands when they open Lumos, on the web
 * and now here (`lumos/page.tsx` redirects students past the marketing home).
 *
 * Section for section `courses/content.tsx`: the two-line hero beside its
 * terracotta tile, the search pill, the twelve grade chips, then — while
 * browsing — the lead card (what they were watching, or where to start),
 * the Recommended shelf of six, and every other course of the grade as a
 * two-column grid, twelve at a time. A submitted search replaces the browse
 * view with the paginated results grid until it is cleared.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LumosCoursesScreen(
    onNavigateToCourse: (String) -> Unit,
    viewModel: LumosCoursesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HogwartsTheme.colors
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    val page = state.page
    val shownGrade = state.level ?: page?.effectiveGrade
    val isSearching = state.searchQuery.isNotBlank()

    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            item(key = "hero") { CoursesHero() }

            item(key = "search") {
                SearchPill(
                    query = state.searchQuery,
                    onOpen = { searchOpen = true },
                )
            }

            item(key = "grades") {
                GradeChips(shownGrade = shownGrade, onSelect = viewModel::onGradeSelected)
            }

            when {
                page == null && state.isLoading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                isSearching -> {
                    if (state.searchResults.isEmpty() && !state.isLoading) {
                        item(key = "empty") { EmptyCourses() }
                    }
                    items(state.searchResults, key = { "result-${it.id}" }) { course ->
                        CourseCard(
                            course = course,
                            showGrade = true,
                            compact = false,
                            onClick = { onNavigateToCourse(course.id) },
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    if (state.searchResults.size < state.searchTotal) {
                        item(key = "results-more") { SeeMore(onClick = viewModel::loadMoreResults) }
                    }
                }

                page != null && page.shelves.isNotEmpty() -> {
                    val gradeCourses = page.shelves.find { it.grade == shownGrade }?.courses.orEmpty()
                    val resume = page.continueWatching.firstOrNull()
                    val start = page.startHere
                    val leadSlugs = setOfNotNull(resume?.courseSlug, start?.courseSlug)
                    val available = gradeCourses.filterNot { it.slug in leadSlugs }
                    val recommended = available.take(RECOMMENDED_COUNT)
                    val others = available.drop(RECOMMENDED_COUNT)
                    val visible = others.take(state.visibleOthers)

                    if (resume != null || start != null) {
                        item(key = "lead") {
                            if (resume != null) {
                                LeadCard(
                                    kicker = stringResource(R.string.lumos_browse_continue_learning),
                                    courseTitle = resume.courseTitle,
                                    grade = resume.grade,
                                    chapterTitle = resume.chapterTitle,
                                    lessonTitle = resume.lessonTitle,
                                    thumbnailUrl = resume.thumbnailUrl,
                                    color = resume.color,
                                    instructor = resume.instructor,
                                    statusLabel = stringResource(R.string.lumos_browse_resume),
                                    statusDetail = stringResource(
                                        R.string.lumos_browse_progress,
                                        (resume.watchedSeconds / 60).toString(),
                                        maxOf(1, (resume.totalSeconds + 59) / 60).toString(),
                                    ),
                                    onClick = { onNavigateToCourse(resume.courseSlug) },
                                )
                            } else if (start != null) {
                                LeadCard(
                                    kicker = stringResource(R.string.lumos_browse_start_here),
                                    courseTitle = start.courseTitle,
                                    grade = start.grade,
                                    chapterTitle = start.chapterTitle,
                                    lessonTitle = start.lessonTitle,
                                    thumbnailUrl = start.thumbnailUrl,
                                    color = start.color,
                                    instructor = start.instructor,
                                    statusLabel = stringResource(R.string.lumos_browse_start),
                                    statusDetail = if (start.totalLessons > 0) {
                                        stringResource(R.string.lumos_browse_lessons, start.totalLessons.toString())
                                    } else null,
                                    onClick = { onNavigateToCourse(start.courseId) },
                                )
                            }
                        }
                    }

                    if (recommended.isNotEmpty()) {
                        item(key = "recommended") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ShelfTitle(stringResource(R.string.lumos_browse_recommended))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(recommended, key = { "rec-${it.id}" }) { course ->
                                        CourseCard(
                                            course = course,
                                            showGrade = false,
                                            compact = false,
                                            onClick = { onNavigateToCourse(course.id) },
                                            modifier = Modifier.width(160.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (visible.isNotEmpty()) {
                        item(key = "more") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ShelfTitle(stringResource(R.string.lumos_browse_more))
                                // Two across, 12dp apart — a lazy grid inside a lazy list
                                // cannot measure, so the rows are laid out by hand.
                                visible.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        pair.forEach { course ->
                                            CourseCard(
                                                course = course,
                                                showGrade = false,
                                                compact = true,
                                                onClick = { onNavigateToCourse(course.id) },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        if (pair.size == 1) Box(Modifier.weight(1f))
                                    }
                                }
                                if (visible.size < others.size) {
                                    SeeMore(onClick = viewModel::onSeeMoreOthers)
                                }
                            }
                        }
                    }
                }

                else -> item(key = "empty") { EmptyCourses() }
            }

            state.error?.let { message ->
                item(key = "error") {
                    Text(message, style = HogwartsTheme.type.caption, color = colors.destructive)
                }
            }
        }
    }

    if (searchOpen) {
        SearchSheet(
            initialQuery = state.searchQuery,
            typeahead = state.typeahead,
            onTypeahead = viewModel::onTypeahead,
            onSubmit = { query ->
                searchOpen = false
                viewModel.submitSearch(query)
            },
            onOpenCourse = { id ->
                searchOpen = false
                onNavigateToCourse(id)
            },
            onDismiss = { searchOpen = false },
        )
    }
}

/**
 * The hero: an 80dp terracotta tile carrying the glyph, beside two lines set
 * to the same width — the title bold, the tagline light at the ratio of the
 * two lines' measured widths, so they end together as on the web.
 */
@Composable
private fun CoursesHero() {
    val colors = HogwartsTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD97757))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(LumosAssets.COURSES_HERO_ICON_URL)
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
        }
        Column {
            Text(
                text = stringResource(R.string.lumos_browse_hero_title),
                fontSize = 30.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground,
            )
            Text(
                text = stringResource(R.string.lumos_browse_hero_tagline),
                // 714.0 / 833.2 of the title's size in Arabic — `HEADLINE_WIDTHS`.
                fontSize = 25.7.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Light,
                color = colors.foreground,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * The search pill as the phone draws it: the black search disc, the prompt,
 * and "Explore" behind a rule. Tapping anywhere opens the sheet — the web's
 * phone layout opens its search as a sheet too, rather than typing in place.
 */
@Composable
private fun SearchPill(query: String, onOpen: () -> Unit) {
    val colors = HogwartsTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(HogwartsShapes.Pill)
            .border(1.dp, colors.border, HogwartsShapes.Pill)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(colors.foreground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(ToolbarIcons.Search, contentDescription = null, tint = colors.background, modifier = Modifier.size(18.dp))
        }
        Text(
            text = query.ifBlank { stringResource(R.string.lumos_browse_search_placeholder) },
            fontSize = 14.sp,
            color = if (query.isBlank()) colors.mutedForeground else colors.foreground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
        )
        VerticalDivider(color = colors.border, modifier = Modifier.height(28.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.lumos_browse_explore),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.foreground,
            )
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = colors.foreground, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Twelve grade chips. The shown grade is filled and reads as its ordinal
 * ("الثاني عشر"); the rest are muted numbers. Every role gets them — they are
 * how anyone leaves the grade the page opened on.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GradeChips(shownGrade: Int?, onSelect: (Int) -> Unit) {
    val colors = HogwartsTheme.colors
    val ordinals = stringArrayResource(R.array.lumos_grade_ordinals)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        (1..12).forEach { grade ->
            val active = grade == shownGrade
            Text(
                text = if (active) ordinals.getOrElse(grade - 1) { grade.toString() } else grade.toString(),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (active) colors.primaryForeground else colors.mutedForeground,
                modifier = Modifier
                    .clip(HogwartsShapes.Pill)
                    .background(if (active) colors.primary else colors.muted)
                    .clickable(role = Role.Button) { onSelect(grade) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ShelfTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = HogwartsTheme.colors.foreground,
    )
}

/**
 * `continue-learning-card.tsx`: the one card about the reader rather than the
 * catalog — an 80dp square beside the kicker, the course with its grade, the
 * chapter, the lesson, the instructor, and where they are in it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeadCard(
    kicker: String,
    courseTitle: String,
    grade: Int?,
    chapterTitle: String?,
    lessonTitle: String?,
    thumbnailUrl: String?,
    color: String?,
    instructor: LessonInstructor?,
    statusLabel: String,
    statusDetail: String?,
    onClick: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // 8dp in from the gutter and 12dp before the copy — the web's 104px
        // art column (12px padding) on a row pulled out by 8px and padded 4px.
        Box(Modifier.padding(start = 8.dp, end = 12.dp)) {
            Artwork(thumbnailUrl, color, Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)))
        }
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(kicker, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = colors.mutedForeground,
                modifier = Modifier.padding(bottom = 4.dp))
            FlowRow(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(courseTitle, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold,
                    color = colors.foreground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (grade != null) {
                    Text(
                        text = stringResource(R.string.lumos_browse_grade_label, grade.toString()),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colors.foreground,
                        modifier = Modifier.clip(HogwartsShapes.Pill).background(colors.muted)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            if (!chapterTitle.isNullOrBlank()) {
                Text(chapterTitle, fontSize = 14.sp, lineHeight = 20.sp, color = colors.foreground, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 4.dp))
            }
            if (!lessonTitle.isNullOrBlank()) {
                Text(lessonTitle, fontSize = 14.sp, lineHeight = 20.sp, color = colors.mutedForeground, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 8.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val name = instructor?.name
                if (!name.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Portrait(name, instructor.image)
                        Text(name, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(statusLabel, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    if (statusDetail != null) {
                        Text(statusDetail, fontSize = 12.sp, lineHeight = 16.sp, color = colors.mutedForeground)
                    }
                }
            }
        }
    }
}

/**
 * `course-card.tsx`: 16:9 artwork, then the grade (on lists that span
 * grades), the title on one line, and the course's kind. `compact` is the
 * grid's smaller type.
 */
@Composable
private fun CourseCard(
    course: CatalogCourse,
    showGrade: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    Column(modifier.clickable(role = Role.Button, onClick = onClick)) {
        Artwork(
            course.imageUrl,
            course.color,
            Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(HogwartsShapes.Card),
        )
        Column(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = if (compact) 8.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
        ) {
            val grade = course.grades.firstOrNull()
            if (showGrade && grade != null) {
                Text(stringResource(R.string.lumos_browse_grade_label, grade.toString()), fontSize = 12.sp, lineHeight = 16.sp,
                    color = colors.mutedForeground)
            }
            Text(
                text = course.title,
                fontSize = if (compact) 12.sp else 14.sp,
                lineHeight = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.foreground,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = stringResource(courseTypeLabel(course.typeKey)),
                fontSize = if (compact) 11.sp else 12.sp,
                lineHeight = 16.sp,
                color = colors.mutedForeground,
            )
            if (course.averageRating > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("%.1f".format(course.averageRating), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.foreground)
                    Text("★", fontSize = 12.sp, color = Color(0xFFFACC15))
                    Text("(${course.enrollments})", fontSize = 12.sp, color = colors.mutedForeground)
                }
            }
        }
    }
}

private fun courseTypeLabel(key: CourseTypeKey): Int = when (key) {
    CourseTypeKey.PROFESSIONAL_CERTIFICATE -> R.string.lumos_course_type_professional_certificate
    CourseTypeKey.SPECIALIZATION -> R.string.lumos_course_type_specialization
    CourseTypeKey.COURSE -> R.string.lumos_course_type_course
    CourseTypeKey.SHORT_COURSE -> R.string.lumos_course_type_short_course
}

/** The artwork, or the course's own colour when there is none — a normal state. */
@Composable
private fun Artwork(url: String?, color: String?, modifier: Modifier) {
    val fallback = color?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
        ?: Color(0xFFE5E7EB)
    Box(modifier.background(fallback)) {
        if (!url.isNullOrBlank()) {
            AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun Portrait(name: String, photoUrl: String?) {
    val colors = HogwartsTheme.colors
    Box(Modifier.size(24.dp).clip(CircleShape).background(colors.muted), contentAlignment = Alignment.Center) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(model = photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text(
                text = name.split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) },
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mutedForeground,
            )
        }
    }
}

/** `SeeMore`: a quiet centred text button. */
@Composable
private fun SeeMore(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.lumos_browse_see_more),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HogwartsTheme.colors.foreground,
            modifier = Modifier.clip(HogwartsShapes.Pill).clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun EmptyCourses() {
    val colors = HogwartsTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(HogwartsShapes.Card).border(1.dp, colors.border, HogwartsShapes.Card)
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = colors.mutedForeground,
            modifier = Modifier.size(64.dp).padding(bottom = 16.dp))
        Text(stringResource(R.string.lumos_browse_empty_title), style = HogwartsTheme.type.rowTitle, color = colors.foreground)
        Text(stringResource(R.string.lumos_browse_empty_description), style = HogwartsTheme.type.body,
            color = colors.mutedForeground, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
    }
}

/**
 * The search sheet: the box, the popular terms on an empty box, and the
 * matching courses as the reader types. Submitting searches the whole
 * catalog and shows the results grid on the page behind.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SearchSheet(
    initialQuery: String,
    typeahead: List<CatalogCourse>,
    onTypeahead: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    var query by remember { mutableStateOf(initialQuery) }
    val terms = stringArrayResource(R.array.lumos_search_terms)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(46.dp).clip(HogwartsShapes.Pill)
                    .border(1.dp, colors.border, HogwartsShapes.Pill).padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(stringResource(R.string.lumos_browse_search_placeholder), fontSize = 14.sp, color = colors.mutedForeground)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it; onTypeahead(it) },
                        singleLine = true,
                        textStyle = HogwartsTheme.type.body.copy(color = colors.foreground),
                        cursorBrush = SolidColor(colors.foreground),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSubmit(query) }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(colors.foreground)
                        .clickable(role = Role.Button) { onSubmit(query) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(ToolbarIcons.Search, contentDescription = null, tint = colors.background, modifier = Modifier.size(18.dp))
                }
            }

            if (query.isBlank()) {
                Text(stringResource(R.string.lumos_browse_search_popular), fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = colors.mutedForeground, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    terms.forEach { term ->
                        Text(
                            term,
                            fontSize = 14.sp,
                            color = colors.foreground,
                            modifier = Modifier.clip(HogwartsShapes.Pill).border(1.dp, colors.border, HogwartsShapes.Pill)
                                .clickable(role = Role.Button) { onSubmit(term) }.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            } else {
                Text(stringResource(R.string.lumos_browse_search_results), fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = colors.mutedForeground, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
                if (typeahead.isEmpty()) {
                    Text(stringResource(R.string.lumos_browse_search_no_results), style = HogwartsTheme.type.body,
                        color = colors.mutedForeground)
                } else {
                    typeahead.take(6).forEach { course ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .clickable(role = Role.Button) { onOpenCourse(course.id) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Artwork(course.imageUrl, course.color, Modifier.size(width = 64.dp, height = 36.dp).clip(RoundedCornerShape(6.dp)))
                            Column(Modifier.weight(1f)) {
                                Text(course.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.foreground,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                course.grades.firstOrNull()?.let {
                                    Text(stringResource(R.string.lumos_browse_grade_label, it.toString()), fontSize = 12.sp,
                                        color = colors.mutedForeground)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
