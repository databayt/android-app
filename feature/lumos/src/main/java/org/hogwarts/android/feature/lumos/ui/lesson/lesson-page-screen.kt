package org.hogwarts.android.feature.lumos.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.lumos.data.remote.dto.LessonPageDto
import org.hogwarts.android.feature.lumos.data.remote.dto.QuizQuestionDtoV2
import org.hogwarts.android.feature.lumos.ui.components.LumosVideoPlayer

private val MINT = Color(0xFF9FE5B1)
private val CHIP = Color(0xFF8D8D93)
private val PILL = Color(0xFFF2F2F7)
private val LINK_BLUE = Color(0xFF0A84FF)

/**
 * `/lumos/courses/[slug]/[lessonId]` (`lumos/dashboard/lesson/content.tsx`) on
 * a phone: the title card (art, title, info line, Play, the paragraph with
 * More, the marks) that gives way to the player; "More from" the course; the
 * quiz; the mint lesson card with Mark as Complete; resources; previous/next.
 */
@Composable
fun LessonPageScreen(
    onOpenLesson: (courseId: String, lessonId: String) -> Unit,
    onOpenCourse: (courseId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LessonPageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val page = state.page
    Box(Modifier.fillMaxSize().background(HogwartsTheme.colors.background)) {
        when {
            page != null -> LessonPage(page, state, viewModel, onOpenLesson, onOpenCourse, onNavigateBack)
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            else -> Text(state.error.orEmpty(), color = HogwartsTheme.colors.mutedForeground, modifier = Modifier.align(Alignment.Center).padding(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonPage(
    p: LessonPageDto,
    s: LessonPageState,
    vm: LessonPageViewModel,
    onOpenLesson: (String, String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val c = HogwartsTheme.colors
    val l = p.labels
    val uri = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var showMore by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 24.dp)) {
        // ── Title card, or the player once Play is pressed ─────────────
        if (s.playing && s.playUrl != null) {
            LumosVideoPlayer(
                videoUrl = s.playUrl,
                lessonTitle = p.title,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            )
        } else {
            TitleCard(p, onPlay = vm::play, onMore = { showMore = true })
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // ── More from the course ───────────────────────────────────
            if (p.siblings.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${l["more_from"].orEmpty()} ${p.course.title}", color = c.foreground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(p.siblings, key = { it.id }) { sib ->
                            Column(Modifier.width(240.dp).clickable { onOpenLesson(p.course.slug, sib.id) }) {
                                Box(Modifier.fillMaxWidth().aspectRatio(3f / 2f).clip(RoundedCornerShape(8.dp)).background(sib.color?.let(::hex) ?: Color(0xFF1A1A1A))) {
                                    if (sib.thumbnailUrl != null) AsyncImage(sib.thumbnailUrl, sib.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                                val eyebrow = if ((sib.watchedMinutes ?: 0) > 0) "${sib.watchedMinutes} ${l["min_watched"].orEmpty()}"
                                else "${l["chapter_short"].orEmpty()}${sib.chapterPosition}، ${l["lesson_short"].orEmpty()}${sib.lessonPosition} · ${sib.duration ?: "?"} ${l["min"].orEmpty()}"
                                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(PlayIcon, null, tint = c.mutedForeground, modifier = Modifier.size(12.dp))
                                    Text(eyebrow, color = c.mutedForeground, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(sib.title, color = c.foreground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            // ── Quiz ───────────────────────────────────────────────────
            if (p.quiz.isNotEmpty()) Quiz(p, s, vm)

            // ── The mint lesson card ───────────────────────────────────
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(40.dp)).background(MINT).padding(horizontal = 24.dp, vertical = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${p.chapter.title} • ${p.course.title}", color = Color.Black, fontSize = 24.sp, lineHeight = 36.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
                    Text(p.title, color = Color.Black, fontSize = 30.sp, lineHeight = 45.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
                p.description?.let { Text(it, color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center) }
                if ((p.videoDuration ?: 0) > 0 || (p.duration ?: 0) > 0) {
                    val d = p.videoDuration?.let { "${it / 60}m ${it % 60}s" } ?: "${p.duration} ${l["minutes"].orEmpty()}"
                    Text("${l["duration"].orEmpty()}: $d", color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                Row(
                    Modifier.height(44.dp).clip(CircleShape).background(if (s.completed) Color.White else Color.Black)
                        .clickable(enabled = !s.completing && !s.completed, role = Role.Button, onClick = vm::markComplete)
                        .padding(horizontal = 36.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(if (s.completed) "✓" else "○", color = if (s.completed) Color.Black else Color.White, fontSize = 15.sp)
                    Text(
                        (if (s.completed) l["completed"] else l["mark_complete"]).orEmpty(),
                        color = if (s.completed) Color.Black else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                    )
                }
            }

            // ── Resources ──────────────────────────────────────────────
            if (p.resources.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(l["resources"].orEmpty(), color = c.foreground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    p.resources.forEach { r ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).border(1.dp, c.border, RoundedCornerShape(6.dp))
                                .clickable(enabled = r.url != null) { r.url?.let { url -> scope.launch { uri.openUri(vm.resolve(url)) } } }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("📄", fontSize = 14.sp)
                            Column {
                                Text(r.title, color = c.foreground, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                r.description?.let { Text(it, color = c.mutedForeground, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                    }
                }
            }

            // ── Previous / next ────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                p.previous?.let { prev ->
                    NavButton("‹ ${prev.title}", primary = false) { onOpenLesson(p.course.slug, prev.id) }
                } ?: Box(Modifier)
                p.next?.let { next -> NavButton("${next.title} ›", primary = true) { onOpenLesson(p.course.slug, next.id) } }
                    ?: NavButton("${l["back_to_course"].orEmpty()} ›", primary = true) { onOpenCourse(p.course.slug) }
            }
        }
    }
    if (showMore) {
        AlertDialog(
            onDismissRequest = { showMore = false },
            confirmButton = { TextButton(onClick = { showMore = false }) { Text(l["done"].orEmpty()) } },
            title = { Text(p.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
            text = { Text(p.blurb, fontSize = 13.sp) },
        )
    }
}

/** `TitleCard` on a phone: 4:5 art under a black fade, the title on it, then a black band with Play, the paragraph and the marks. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TitleCard(p: LessonPageDto, onPlay: () -> Unit, onMore: () -> Unit) {
    val l = p.labels
    Column(Modifier.fillMaxWidth().background(Color.Black)) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 5f).background(p.color?.let(::hex) ?: Color(0xFF1A1A1A))) {
            if (p.thumbnailUrl != null) AsyncImage(p.thumbnailUrl, p.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(224.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f), Color.Black))),
            )
            Row(
                Modifier.align(Alignment.TopEnd).padding(16.dp).height(32.dp).clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("+", color = Color.White, fontSize = 15.sp)
                Text(l["add"].orEmpty(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Column(Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    p.title, color = Color.White, fontSize = 52.sp, lineHeight = 58.sp, fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center, style = TextStyle(lineBreak = LineBreak.Heading),
                )
                if (p.meta.isNotBlank()) Text(p.meta, color = Color(0xFF8E8E93), fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            }
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(8.dp)).background(PILL)
                    .clickable(enabled = p.videoUrl != null, role = Role.Button, onClick = onPlay),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Icon(PlayIcon, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Text(p.playLabel, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Box {
                Text(p.blurb, color = Color.White, fontSize = 15.sp, lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Clip)
                Text(
                    "… ${l["more"].orEmpty()}", color = LINK_BLUE, fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black).padding(start = 4.dp).clickable(onClick = onMore),
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("4K", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(CHIP).padding(horizontal = 6.dp, vertical = 2.dp))
                if (p.isFree) Chip(l["free"].orEmpty())
                Chip("CC")
                Chip("AD")
                val resources = p.resources.size
                if (resources > 0) {
                    Text("·  $resources ${(if (resources > 1) l["resource_many"] else l["resource_one"]).orEmpty()}", color = CHIP, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Text(
        text, color = CHIP, fontSize = 13.sp,
        modifier = Modifier.clip(RoundedCornerShape(3.dp)).border(1.dp, CHIP, RoundedCornerShape(3.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** The quiz card: numbered questions, choices that reveal right and wrong after grading, the score. */
@Composable
private fun Quiz(p: LessonPageDto, s: LessonPageState, vm: LessonPageViewModel) {
    val c = HogwartsTheme.colors
    val l = p.labels
    val grade = s.grade
    val verdicts = grade?.verdicts?.associateBy { it.questionId }.orEmpty()
    val allAnswered = p.quiz.all { q -> if (q.choices == null) !s.texts[q.id].isNullOrBlank() else s.choices.containsKey(q.id) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, c.border, RoundedCornerShape(12.dp)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(l["quiz"].orEmpty(), color = c.foreground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        p.quiz.forEachIndexed { i, q -> QuestionView(i, q, s, verdicts[q.id], vm, l) }
        s.quizError?.let { Text(it, color = c.destructive, fontSize = 14.sp) }
        if (grade != null) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(c.muted).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${grade.percentage}%", color = c.foreground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("${grade.score} / ${grade.total}", color = c.mutedForeground, fontSize = 14.sp)
                Text((if (grade.recorded) l["quiz_recorded"] else l["quiz_practice"]).orEmpty(), color = c.mutedForeground, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else {
            Box(
                Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(6.dp)).background(if (allAnswered) c.primary else c.primary.copy(alpha = 0.5f))
                    .clickable(enabled = allAnswered && !s.submitting, role = Role.Button, onClick = vm::submitQuiz),
                Alignment.Center,
            ) {
                Text((if (s.submitting) l["quiz_submitting"] else l["quiz_submit"]).orEmpty(), color = c.primaryForeground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun QuestionView(
    index: Int,
    q: QuizQuestionDtoV2,
    s: LessonPageState,
    verdict: org.hogwarts.android.feature.lumos.data.remote.dto.QuizVerdictDto?,
    vm: LessonPageViewModel,
    l: Map<String, String?>,
) {
    val c = HogwartsTheme.colors
    val submitted = verdict != null
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, c.border, RoundedCornerShape(8.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("${index + 1}. ${q.text}", color = c.foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        val choices = q.choices
        if (choices == null) {
            val green = Color(0xFF22C55E)
            val red = Color(0xFFEF4444)
            BasicTextField(
                value = s.texts[q.id].orEmpty(), onValueChange = { vm.type(q.id, it) }, enabled = !submitted, singleLine = true,
                textStyle = TextStyle(color = c.foreground, fontSize = 14.sp), cursorBrush = SolidColor(c.foreground),
                decorationBox = { inner ->
                    Box(
                        Modifier.fillMaxWidth().height(36.dp).border(1.dp, if (!submitted) c.input else if (verdict?.isCorrect == true) green else red, RoundedCornerShape(6.dp)).padding(horizontal = 12.dp),
                        Alignment.CenterStart,
                    ) {
                        if (s.texts[q.id].isNullOrEmpty()) Text(l["quiz_placeholder"].orEmpty(), color = c.mutedForeground, fontSize = 14.sp)
                        inner()
                    }
                },
            )
        } else {
            choices.forEachIndexed { i, label ->
                val correct = submitted && verdict?.correctIndex == i
                val picked = s.choices[q.id] == i
                val border = when {
                    correct -> Color(0xFF22C55E)
                    picked && submitted -> Color(0xFFEF4444)
                    picked -> c.primary
                    else -> c.border
                }
                val bg = when {
                    correct -> Color(0xFFF0FDF4)
                    picked && submitted -> Color(0xFFFEF2F2)
                    else -> Color.Transparent
                }
                Text(
                    label, color = c.foreground, fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(bg).border(1.dp, border, RoundedCornerShape(6.dp))
                        .clickable(enabled = !submitted) { vm.choose(q.id, i) }.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
        if (submitted && verdict?.correctAnswers != null && !verdict.isCorrect) {
            Text("${l["answer"].orEmpty()}: ${verdict.correctAnswers.joinToString(" / ")}", color = c.mutedForeground, fontSize = 12.sp)
        }
        verdict?.sampleAnswer?.let { Text("${l["answer"].orEmpty()}: $it", color = c.mutedForeground, fontSize = 12.sp) }
        verdict?.explanation?.let { Text(it, color = c.mutedForeground, fontSize = 12.sp) }
    }
}

@Composable
private fun NavButton(label: String, primary: Boolean, onClick: () -> Unit) {
    val c = HogwartsTheme.colors
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(6.dp))
            .then(if (primary) Modifier.background(c.primary) else Modifier.border(1.dp, c.border, RoundedCornerShape(6.dp)))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
        Alignment.Center,
    ) {
        Text(
            label, color = if (primary) c.primaryForeground else c.foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(150.dp),
        )
    }
}

private fun hex(v: String): Color? = runCatching { Color(android.graphics.Color.parseColor(v)) }.getOrNull()

private val PlayIcon: ImageVector = ImageVector.Builder("Play", 24.dp, 24.dp, 24f, 24f).apply {
    addPath(PathParser().parsePathString("M7 4.5v15a1 1 0 0 0 1.5.86l12-7.5a1 1 0 0 0 0-1.72l-12-7.5A1 1 0 0 0 7 4.5z").toNodes(), fill = SolidColor(Color.Black))
}.build()
