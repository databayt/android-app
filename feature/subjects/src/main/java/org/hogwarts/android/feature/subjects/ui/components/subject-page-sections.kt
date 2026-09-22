package org.hogwarts.android.feature.subjects.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.AssignmentItem
import org.hogwarts.android.feature.subjects.domain.model.ExamItem
import org.hogwarts.android.feature.subjects.domain.model.SubjectChapter
import org.hogwarts.android.feature.subjects.domain.model.SubjectLevel
import org.hogwarts.android.feature.subjects.domain.model.VideoItem

/**
 * `catalog-hero.tsx` on a phone: the banner art edge to edge at 5.4:1 — held
 * at 160dp at least, so the strip stays readable and the art crops — mirrored
 * in Arabic, under a dark scrim, with the name and the counts set on it.
 */
@Composable
fun SubjectPageHero(
    name: String,
    imageUrl: String?,
    color: String?,
    chapters: Int,
    lessons: Int,
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(5.4f)
            .heightIn(min = 160.dp)
            .background(parseColor(color) ?: Color(0xFF1E40AF)),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().scale(scaleX = if (rtl) -1f else 1f, scaleY = 1f),
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.5f to Color.Black.copy(alpha = 0.30f),
                    1f to Color.Black.copy(alpha = 0.75f),
                ),
            ),
        )
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(name, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = stringResource(R.string.subjects_page_hero_counts, chapters, lessons),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** A section heading with its action at the far end — `ContentSection`. */
@Composable
fun PageSectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    val colors = HogwartsTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.foreground,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colors.mutedForeground,
                modifier = Modifier.clickable(role = Role.Button, onClick = onAction),
            )
        }
    }
}

/**
 * `catalog-detail.tsx`: "Chapters" with "See all", then one sideways row —
 * the explore-all card first, then a 208dp card per chapter, a 56dp picture
 * at the start and the name beside it.
 */
@Composable
fun ChaptersRow(
    chapters: List<SubjectChapter>,
    subjectImageUrl: String?,
    subjectColor: String?,
    onOpenChapters: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageSectionHeader(
            title = stringResource(R.string.subjects_page_chapters),
            actionLabel = stringResource(R.string.subjects_see_all),
            onAction = onOpenChapters,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item(key = "explore") {
                ChapterChip(
                    label = stringResource(R.string.subjects_page_explore_all),
                    imageUrl = subjectImageUrl,
                    color = subjectColor,
                    muted = true,
                    onClick = onOpenChapters,
                )
            }
            items(chapters, key = { it.id }) { chapter ->
                ChapterChip(
                    label = chapter.name,
                    imageUrl = chapter.thumbnailUrl,
                    color = chapter.color ?: subjectColor,
                    muted = false,
                    onClick = onOpenChapters,
                )
            }
        }
    }
}

@Composable
private fun ChapterChip(label: String, imageUrl: String?, color: String?, muted: Boolean, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .width(208.dp)
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(56.dp).background(parseColor(color) ?: Color(0xFF6B7280))) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize())
            }
        }
        Text(
            label,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (muted) colors.mutedForeground else colors.foreground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 12.dp),
        )
    }
}

/** One book on the materials board, and where it goes. */
data class BookTile(val badge: String?, val badgeColor: Color, val onClick: (() -> Unit)?)

/**
 * `MaterialTypePipeline`: five copies of the subject's own textbook board —
 * the book, then its summary, question bank, exams and references — each
 * 180x260, the cover image under a white veil that carries the stage, the
 * title and the grade, the way the printed book sets its head. The four
 * derived books wear a coloured word in the corner.
 */
@Composable
fun MaterialsBoard(
    tiles: List<BookTile>,
    coverUrl: String?,
    accentColor: Color,
    stage: String?,
    title: String,
    grade: String?,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(tiles) { tile ->
            Box(
                Modifier
                    .size(width = 180.dp, height = 260.dp)
                    .then(if (tile.onClick != null) Modifier.clickable(role = Role.Button, onClick = tile.onClick) else Modifier),
            ) {
                if (!coverUrl.isNullOrBlank()) {
                    AsyncImage(model = coverUrl, contentDescription = title, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(accentColor))
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.White.copy(alpha = 0.97f),
                                0.58f to Color.White.copy(alpha = 0.95f),
                                0.82f to Color.White.copy(alpha = 0.72f),
                                1f to Color.White.copy(alpha = 0f),
                            ),
                        )
                        .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val ink = Color(0xFF1C1C1E)
                    if (stage != null) {
                        Text(stage, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold, color = ink,
                            textAlign = TextAlign.Center)
                    }
                    Text(elongate(title), fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, color = ink,
                        maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    if (grade != null) {
                        Text(grade, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold, color = ink,
                            textAlign = TextAlign.Center)
                    }
                }
                if (tile.badge != null) {
                    Text(
                        tile.badge,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.TopStart).background(tile.badgeColor),
                    )
                }
            }
        }
    }
}

/** The board's stage line — "المرحلة الثانوية" — or the generic word. */
@Composable
fun stageLine(level: SubjectLevel?): String = stringResource(
    when (level) {
        SubjectLevel.ELEMENTARY -> R.string.subjects_page_stage_elementary
        SubjectLevel.MIDDLE -> R.string.subjects_page_stage_middle
        SubjectLevel.HIGH -> R.string.subjects_page_stage_high
        null -> R.string.subjects_page_textbook
    },
)

/**
 * The grade a book prints is its place inside its stage — grade 12 is the
 * third secondary year: "الصف الثالث ثانوي". `gradeLine` in textbook/format.ts.
 */
@Composable
fun gradeLine(grade: Int?, level: SubjectLevel?): String? {
    if (grade == null) return null
    val start = when (level) {
        SubjectLevel.MIDDLE -> 7
        SubjectLevel.HIGH -> 10
        else -> 1
    }
    val nth = grade - start + 1
    val ordinals = stringArrayResource(R.array.subjects_page_ordinals)
    val ordinal = ordinals.getOrNull(nth - 1).orEmpty()
    val suffix = stringResource(
        when (level) {
            SubjectLevel.MIDDLE -> R.string.subjects_page_stage_suffix_middle
            SubjectLevel.HIGH -> R.string.subjects_page_stage_suffix_high
            else -> R.string.subjects_page_stage_suffix_elementary
        },
    )
    return stringResource(R.string.subjects_page_grade_ordinal, ordinal, suffix, grade)
        .replace(Regex("\\s+"), " ").trim()
}

/** Letters that join to the one after them — copied from textbook/format.ts. */
private val JOINS_FORWARD = Regex(
    "[\\u0628\\u062A-\\u062C\\u062D\\u062E\\u0633-\\u0636\\u0637\\u0638\\u0639\\u063A\\u0641-\\u0643" +
        "\\u0644\\u0645\\u0646\\u0647\\u064A\\u0626\\u0649\\u06A9\\u06CC\\u067E\\u0686\\u0698\\u06AF]",
)
/** Letters that join to the one before them. */
private val JOINS_BACK = Regex("[\\u0621-\\u064A\\u0626\\u0649\\u06A9\\u06CC\\u067E\\u0686\\u0698\\u06AF]")

/**
 * `elongate` in textbook/format.ts: two kashida strokes after every letter
 * that joins forward into one that joins back, never inside lam-alef — the
 * stretched setting the printed boards use for a title.
 */
fun elongate(text: String, times: Int = 2): String {
    if (times < 1) return text
    val stroke = "ـ".repeat(times)
    val out = StringBuilder()
    for (i in text.indices) {
        val ch = text[i]
        val next = text.getOrNull(i + 1)
        out.append(ch)
        val lamAlef = ch == 'ل' && next != null && next in "آأإا"
        if (next != null && !lamAlef && JOINS_FORWARD.matches(ch.toString()) && JOINS_BACK.matches(next.toString())) {
            out.append(stroke)
        }
    }
    return out.toString()
}

/**
 * The videos shelf — `ShelfCard` with the title below: a 240dp card, 3:2 art,
 * then the play mark, the length and the views as the eyebrow over the name.
 */
@Composable
fun VideosShelf(videos: List<VideoItem>, accentColor: Color, onOpen: (VideoItem) -> Unit) {
    val colors = HogwartsTheme.colors
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(videos, key = { it.id }) { video ->
            Column(Modifier.width(240.dp).clickable(role = Role.Button) { onOpen(video) }) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(parseColor(video.color) ?: accentColor),
                ) {
                    if (!video.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(model = video.thumbnailUrl, contentDescription = video.title,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
                Text(
                    text = "▶ ${formatVideoDuration(video.durationSeconds)} · ${video.viewCount} ${stringResource(R.string.subjects_views)}",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.mutedForeground,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    video.title,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.foreground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun formatVideoDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (s > 0) "%d:%02d".format(m, s) else "$m:00"
}

private data class ExamTypeSpec(val key: String, val color: Color, val label: Int)

private val EXAM_TYPE_PIPELINE = listOf(
    ExamTypeSpec("final", Color(0xFF2C70B2), R.string.subjects_exam_type_final),
    ExamTypeSpec("midterm", Color(0xFF825BA3), R.string.subjects_exam_type_midterm),
    ExamTypeSpec("chapter_test", Color(0xFF4B976A), R.string.subjects_exam_type_chapter_test),
    ExamTypeSpec("quiz", Color(0xFFCF6E30), R.string.subjects_exam_type_quiz),
    ExamTypeSpec("practice", Color(0xFF57908C), R.string.subjects_exam_type_practice),
    ExamTypeSpec("diagnostic", Color(0xFFD25E8C), R.string.subjects_exam_type_diagnostic),
)
private val TEST_TYPES = setOf("chapter_test", "quiz", "practice")

/**
 * `ExamTypePipeline`: one 240dp tile per exam type the subject has, in the
 * web's order — the cream head with the type and how many, then the three
 * averages set large on the type's colour.
 */
@Composable
fun ExamTiles(exams: List<ExamItem>, onOpen: () -> Unit) {
    val groups = EXAM_TYPE_PIPELINE.mapNotNull { spec ->
        val rows = exams.filter { it.examType == spec.key }
        if (rows.isEmpty()) null else spec to rows
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(groups, key = { it.first.key }) { (spec, rows) ->
            fun avg(values: List<Int>) = if (values.isEmpty()) null else Math.round(values.average()).toInt()
            val count = rows.size
            val unit = stringResource(
                if (spec.key in TEST_TYPES) {
                    if (count == 1) R.string.subjects_page_unit_test else R.string.subjects_page_unit_tests
                } else {
                    if (count == 1) R.string.subjects_page_unit_exam else R.string.subjects_page_unit_exams
                },
            )
            val stats = listOf(
                avg(rows.mapNotNull { it.durationMinutes }) to stringResource(R.string.subjects_page_min),
                avg(rows.mapNotNull { it.totalQuestions }) to stringResource(R.string.subjects_page_questions),
                avg(rows.mapNotNull { it.totalMarks }) to stringResource(R.string.subjects_page_marks),
            )
            Column(Modifier.width(240.dp).clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onOpen)) {
                Row(
                    Modifier.fillMaxWidth().background(Color(0xFFF4F1D0)).padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(spec.label), fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF212222), modifier = Modifier.weight(1f))
                    Text("$count $unit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212222).copy(alpha = 0.7f))
                }
                Row(
                    Modifier.fillMaxWidth().background(spec.color).padding(horizontal = 4.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    stats.forEachIndexed { index, (value, label) ->
                        if (index > 0) VerticalDivider(color = Color.White.copy(alpha = 0.25f), modifier = Modifier.height(44.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value?.toString() ?: "–", fontSize = 24.sp, lineHeight = 24.sp,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * The assignments shelf: 224dp raised tiles — `ASSIGNMENT_TILE`, a light
 * slab with a soft 24dp shadow — the type as a tinted badge, the title, then
 * minutes and points.
 */
@Composable
fun AssignmentTiles(assignments: List<AssignmentItem>, accentColor: Color) {
    val colors = HogwartsTheme.colors
    val shape = RoundedCornerShape(34.dp)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 4.dp),
    ) {
        items(assignments, key = { it.id }) { assignment ->
            Column(
                Modifier
                    .width(224.dp)
                    .shadow(12.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.08f))
                    .clip(shape)
                    .background(if (colors.isDark) colors.card else Color(0xFFF8F8F8))
                    .padding(20.dp),
            ) {
                assignment.assignmentType?.let { type ->
                    Text(
                        text = assignmentTypeLabel(type),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(accentColor.copy(alpha = 0.125f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Text(assignment.title, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
                    color = colors.foreground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    assignment.estimatedTime?.let {
                        Text("◷ $it ${stringResource(R.string.subjects_page_min)}", fontSize = 12.sp,
                            color = colors.mutedForeground)
                    }
                    assignment.totalPoints?.let {
                        Text("${it.toInt()} ${stringResource(R.string.subjects_page_pts)}", fontSize = 12.sp,
                            color = colors.mutedForeground)
                    }
                }
            }
        }
    }
}

@Composable
private fun assignmentTypeLabel(type: String): String = when (type) {
    "homework" -> stringResource(R.string.subjects_assignment_type_homework)
    "project" -> stringResource(R.string.subjects_assignment_type_project)
    "lab" -> stringResource(R.string.subjects_assignment_type_lab)
    "essay" -> stringResource(R.string.subjects_assignment_type_essay)
    "presentation" -> stringResource(R.string.subjects_assignment_type_presentation)
    else -> type
}

/** The hairline under the hero — `<hr className="border-border" />`. */
@Composable
fun PageRule(modifier: Modifier = Modifier) {
    HorizontalDivider(color = HogwartsTheme.colors.border, modifier = modifier)
}
