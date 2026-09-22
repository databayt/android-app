package org.hogwarts.android.feature.lumos.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.hogwarts.android.feature.lumos.data.remote.dto.CoursePageDto
import org.hogwarts.android.feature.lumos.data.remote.dto.CoursePageLessonDto
import java.net.URLEncoder

/** The course page's Skilljar palette, as `courses/[slug]/content.tsx` pins it. */
private object Sk {
    val card = Color(0xFFF0EEE6)
    val text = Color(0xFF141413)
    val muted = Color(0xFF5E5B4E)
    val border = Color(0xFFE5E2D9)
    val background = Color(0xFFFAF9F5)
    val accent = Color(0xFFB4C6D4)
}

/**
 * `/lumos/courses/[slug]` (`lumos/courses/[slug]/content.tsx`) on a phone:
 * breadcrumb, title, description, the enrol row with FREE, progress when
 * enrolled, share links, the picture with its stats row, About (objectives,
 * prerequisites, audience), the chapters as rows of lesson cards, and the
 * sticky price-and-enrol foot.
 */
@Composable
fun CoursePageScreen(
    onOpenHref: (String) -> Unit,
    onOpenLesson: (courseId: String, lessonId: String) -> Unit,
    viewModel: CoursePageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val page = state.page
    Box(Modifier.fillMaxSize().background(Color.White)) {
        when {
            page != null -> CoursePage(page, state.enrolling, viewModel::enroll, onOpenHref, onOpenLesson)
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Sk.text)
            else -> Text(state.error.orEmpty(), color = Sk.muted, modifier = Modifier.align(Alignment.Center).padding(24.dp))
        }
    }
}

@Composable
private fun CoursePage(
    p: CoursePageDto,
    enrolling: Boolean,
    onEnroll: () -> Unit,
    onOpenHref: (String) -> Unit,
    onOpenLesson: (String, String) -> Unit,
) {
    val l = p.labels
    val firstLesson = p.chapters.firstOrNull()?.lessons?.firstOrNull()?.id
    val enroll: () -> Unit = {
        if (p.isEnrolled) {
            if (firstLesson != null) onOpenLesson(p.id, firstLesson) else Unit
        } else onEnroll()
    }
    val totalLessons = p.chapters.sumOf { it.lessons.size }
    val totalMinutes = p.chapters.sumOf { ch -> ch.lessons.sumOf { it.duration ?: 0 } }
    val free = p.price == null || p.price == 0.0
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // ── Hero ──────────────────────────────────────────────────
            Column(Modifier.padding(horizontal = 16.dp, vertical = 48.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(p.schoolName ?: l["lumos"].orEmpty(), color = Sk.muted, fontSize = 14.sp, modifier = Modifier.clickable { onOpenHref("/lumos") })
                    Text("/", color = Sk.muted, fontSize = 14.sp)
                    Text(l["courses"].orEmpty(), color = Sk.muted, fontSize = 14.sp, modifier = Modifier.clickable { onOpenHref("/lumos/courses") })
                }
                Text(p.title, color = Sk.text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.025).em, maxLines = 1, overflow = TextOverflow.Clip)
                Text(p.description ?: l["default_description"].orEmpty(), color = Sk.muted, fontSize = 16.sp, lineHeight = 1.625.em)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    EnrollButton(p, enrolling, outline = p.isEnrolled, onClick = enroll)
                    if (free) {
                        Box(Modifier.height(36.dp).clip(RoundedCornerShape(6.dp)).background(Sk.card).padding(horizontal = 12.dp), Alignment.Center) {
                            Text(l["free"].orEmpty(), color = Sk.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                p.progress?.takeIf { p.isEnrolled }?.let { pr -> ProgressCard(p, pr.percent, pr.completedLessons, pr.totalLessons, pr.remainingMinutes, onOpenHref) }
                if (!p.isEnrolled) {
                    Text(
                        buildAnnotatedString {
                            append(l["already_registered"].orEmpty() + " ")
                            withStyle(SpanStyle(color = Sk.text, textDecoration = TextDecoration.Underline)) { append(l["sign_in"].orEmpty()) }
                        },
                        color = Sk.muted, fontSize = 14.sp,
                    )
                }
                ShareRow(p)
                // The picture and its stats.
                Column {
                    Box(
                        Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(p.color?.let(::parse) ?: Sk.card),
                        Alignment.Center,
                    ) {
                        if (p.imageUrl != null) {
                            AsyncImage(p.imageUrl, p.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                        }
                        Icon(PlayTriangle, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Box(Modifier.padding(top = 32.dp).fillMaxWidth().height(1.dp).background(Sk.border))
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Stat(totalLessons.toString(), l["lectures"].orEmpty())
                        val hours = totalMinutes / 60
                        Stat(
                            if (hours > 0) hours.toString() else totalMinutes.toString(),
                            if (hours > 0) (if (hours != 1) l["hours_of_video"] else l["hour_of_video"]).orEmpty() else l["min_of_video"].orEmpty(),
                        )
                        Stat(p.quizCount.toString(), l["quiz"].orEmpty())
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("✓", color = Sk.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(l["certificate"].orEmpty(), color = Sk.muted, fontSize = 11.sp)
                        }
                    }
                }
            }

            // ── About ─────────────────────────────────────────────────
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 48.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF4F4F5).copy(alpha = 0.5f)).padding(24.dp),
            ) {
                Text(l["about"].orEmpty(), color = Sk.text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(p.description ?: l["default_description"].orEmpty(), color = Sk.text, fontSize = 16.sp, lineHeight = 1.625.em, modifier = Modifier.padding(top = 16.dp))
                val objectives = p.objectives.ifEmpty { p.chapters.map { it.title } }
                if (objectives.isNotEmpty()) {
                    SubHeading(l["objectives"].orEmpty())
                    Text(l["by_the_end"].orEmpty(), color = Sk.muted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    Bullets(objectives, Sk.text, Modifier.padding(top = 12.dp))
                }
                SubHeading(l["prerequisites"].orEmpty())
                Bullets(listOf(p.prerequisites ?: l["no_prerequisites"].orEmpty()), Sk.muted, Modifier.padding(top = 12.dp))
                SubHeading(l["audience"].orEmpty())
                Text(p.targetAudience ?: l["default_audience"].orEmpty(), color = Sk.muted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }

            // ── Chapters ──────────────────────────────────────────────
            Column(Modifier.padding(horizontal = 16.dp, vertical = 48.dp)) {
                Text(l["chapters"].orEmpty(), color = Sk.text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Column(Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                    p.chapters.filter { it.lessons.isNotEmpty() }.forEach { ch ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(ch.title, color = Sk.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ch.lessons.forEach { lesson -> LessonCard(lesson, p.color, l["preview"].orEmpty()) { onOpenLesson(p.id, lesson.id) } }
                            }
                        }
                    }
                }
            }
        }
        // ── The sticky foot ──────────────────────────────────────────
        Box(Modifier.fillMaxWidth().height(1.dp).background(Sk.border))
        Row(
            Modifier.fillMaxWidth().background(Sk.background).navigationBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.weight(1f)) { EnrollButton(p, enrolling, outline = p.isEnrolled, onClick = enroll) }
            if (free) Text(l["free"].orEmpty(), color = Sk.text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EnrollButton(p: CoursePageDto, busy: Boolean, outline: Boolean, onClick: () -> Unit) {
    val l = p.labels
    val label = when {
        p.isEnrolled -> l["continue_learning"]
        busy -> l["processing"]
        p.price != null && p.price > 0 -> l["enroll_price"]?.replace("{price}", "${p.price} ${p.currency.orEmpty()}")
        else -> l["enroll_free"]
    }.orEmpty()
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(6.dp))
            .then(if (outline) Modifier.border(1.dp, Sk.border, RoundedCornerShape(6.dp)).background(Color.White) else Modifier.background(Sk.text))
            .clickable(enabled = !busy, role = Role.Button, onClick = onClick).padding(horizontal = 24.dp),
        Alignment.Center,
    ) {
        Text(label, color = if (outline) Sk.text else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProgressCard(p: CoursePageDto, percent: Int, done: Int, total: Int, remaining: Int, onOpenHref: (String) -> Unit) {
    val l = p.labels
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Sk.card).border(1.dp, Sk.border, RoundedCornerShape(8.dp)).padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(l["percent_complete"].orEmpty().replace("{percent}", percent.toString()), color = Sk.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text("$done/$total ${l["progress_lessons"].orEmpty()}", color = Sk.muted, fontSize = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Sk.text.copy(alpha = 0.2f))) {
            Box(Modifier.fillMaxWidth(percent / 100f).height(8.dp).clip(CircleShape).background(Sk.text))
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("$done ${l["done"].orEmpty()}", color = Sk.muted, fontSize = 12.sp)
            Text("${total - done} ${l["remaining"].orEmpty()}", color = Sk.muted, fontSize = 12.sp)
            if (remaining > 0) {
                val shown = if (remaining >= 60) "${remaining / 60}h ${remaining % 60}m" else "${remaining}m"
                Text("~$shown ${l["left"].orEmpty()}", color = Sk.muted, fontSize = 12.sp)
            }
        }
        if (percent >= 100) {
            Text(
                "${l["certificate_view"].orEmpty()} ${l["certificate_title"].orEmpty().lowercase()}",
                color = Sk.text, fontSize = 12.sp, textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 12.dp).clickable { onOpenHref("/lumos/courses/${p.slug}/certificate") },
            )
        }
    }
}

@Composable
private fun ShareRow(p: CoursePageDto) {
    val uri = LocalUriHandler.current
    val url = URLEncoder.encode("https://balqalam.com/lumos/courses/${p.slug}", "UTF-8")
    val text = URLEncoder.encode(p.title, "UTF-8")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
        ShareLink(XLogo, p.labels["share_on_x"].orEmpty()) { uri.openUri("https://twitter.com/intent/tweet?url=$url&text=$text") }
        ShareLink(LinkedInLogo, p.labels["share_on_linkedin"].orEmpty()) { uri.openUri("https://www.linkedin.com/sharing/share-offsite/?url=$url") }
    }
}

@Composable
private fun ShareLink(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = Sk.text, modifier = Modifier.size(16.dp))
        Text(label, color = Sk.text, fontSize = 14.sp)
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = Sk.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)) { append(value) }
            append(" $label")
        },
        color = Sk.muted, fontSize = 11.sp,
    )
}

@Composable
private fun SubHeading(text: String) {
    Text(text, color = Sk.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 32.dp))
}

@Composable
private fun Bullets(items: List<String>, color: Color, modifier: Modifier) {
    Column(modifier.padding(start = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", color = color, fontSize = 14.sp)
                Text(item, color = color, fontSize = 14.sp)
            }
        }
    }
}

/** `CourseLessonCard`: a 72-dp thumbnail on the course colour, the title (two lines), minutes, Preview. */
@Composable
private fun LessonCard(lesson: CoursePageLessonDto, color: String?, previewLabel: String, onClick: () -> Unit) {
    var failed by remember(lesson.imageUrl) { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(2.dp)).background(color?.let(::parse) ?: Color(0xFF6B7280))) {
            if (lesson.imageUrl != null && !failed) {
                AsyncImage(lesson.imageUrl, lesson.title, contentScale = ContentScale.Crop, onError = { failed = true }, modifier = Modifier.fillMaxSize())
            }
        }
        Column(Modifier.weight(1f).padding(top = 4.dp)) {
            Text(lesson.title, color = Sk.text, fontSize = 14.sp, lineHeight = 1.375.em, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if ((lesson.duration ?: 0) > 0) Text("${lesson.duration} min", color = Sk.muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            if (lesson.isFree) Text(previewLabel, color = Sk.muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

private fun parse(hex: String): Color? = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()

private fun filledIcon(name: String, w: Float, h: Float, path: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, w, h).apply {
        addPath(PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.Black))
    }.build()

private val XLogo = filledIcon(
    "X", 24f, 24f,
    "M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z",
)
private val LinkedInLogo = filledIcon(
    "LinkedIn", 24f, 24f,
    "M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z",
)
private val PlayTriangle = filledIcon("Play", 24f, 24f, "M7 4.5v15a1 1 0 0 0 1.5.86l12-7.5a1 1 0 0 0 0-1.72l-12-7.5A1 1 0 0 0 7 4.5z")

