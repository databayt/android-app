package org.hogwarts.android.feature.subjects.textbook.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.hogwarts.android.feature.subjects.textbook.domain.Book
import org.hogwarts.android.feature.subjects.textbook.engine.BookSearch
import org.hogwarts.android.feature.subjects.textbook.engine.Frag
import org.hogwarts.android.feature.subjects.textbook.engine.Pos
import org.hogwarts.android.feature.subjects.textbook.engine.ReaderType
import org.hogwarts.android.feature.subjects.textbook.engine.Screen
import org.hogwarts.android.feature.subjects.textbook.engine.TextRole
import org.hogwarts.android.feature.subjects.ui.components.elongate
import kotlin.math.roundToInt

/** The search the reader last opened: its query marks its page, its hit is the active mark. */
data class ActiveHit(val query: String, val flow: Int, val pos: Pos)

/** A box exactly [px] wide — the width the paginator measured at, with no dp rounding. */
private fun Modifier.exactWidth(px: Int) = layout { measurable, constraints ->
    val placeable = measurable.measure(
        Constraints(minWidth = px, maxWidth = px, minHeight = constraints.minHeight, maxHeight = constraints.maxHeight),
    )
    layout(px, placeable.height) { placeable.place(0, 0) }
}

@Composable
private fun Float.toDpPx(): Dp = with(LocalDensity.current) { this@toDpPx.toDp() }

/** One screen of a flow or of the contents: its fragments stacked in the page box. */
@Composable
internal fun PageScreen(
    book: Book,
    screen: Screen,
    type: ReaderType,
    palette: ReaderPalette,
    lang: String,
    hit: ActiveHit?,
    markColor: Color,
    onGoToPage: (Int) -> Unit,
) {
    val m = type.metrics
    Column(Modifier.exactWidth(m.pageWidth.toInt()).height(m.pageHeight.toDpPx())) {
        screen.frags.forEach { frag ->
            if (frag.space > 0f) Spacer(Modifier.height(frag.space.toDpPx()))
            val h = frag.height.toDpPx()
            when (frag) {
                is Frag.OpenerFrag -> OpenerView(frag, type, palette, h)
                is Frag.FolioFrag -> Text(
                    formatNumber(frag.printed, lang),
                    style = type.style(TextRole.Folio),
                    color = palette.muted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().height(h),
                )
                is Frag.TextFrag -> TextView(frag, screen.flow, type, palette, hit, markColor, h)
                is Frag.TableFrag -> TableView(frag, type, palette, h)
                is Frag.RuleFrag -> Box(Modifier.fillMaxWidth().height(h).background(palette.rule))
                is Frag.ImageFrag -> AsyncImage(
                    model = frag.url,
                    contentDescription = frag.alt,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(h).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                )
                is Frag.TocFrag -> TocRowView(frag, book, type, palette, lang, h, onGoToPage)
            }
        }
    }
}

@Composable
private fun OpenerView(frag: Frag.OpenerFrag, type: ReaderType, palette: ReaderPalette, h: Dp) {
    val m = type.metrics
    val o = frag.opener
    Column(
        Modifier.fillMaxWidth().height(h),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(((if (frag.toc) m.tocTop else 0f) + m.openerTop(o.chapter)).toDpPx()))
        if (!o.kicker.isNullOrBlank()) {
            Text(o.kicker, style = type.style(TextRole.Kicker), color = palette.muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(m.kickerAfter.toDpPx()))
        }
        Text(
            o.title,
            style = type.style(if (o.chapter) TextRole.ChapterTitle else TextRole.LessonTitle),
            color = palette.fg,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(m.titleAfter(o.chapter).toDpPx()))
        Ornament(palette.fg, Modifier.width(m.ornamentWidth.toDpPx()).height(m.ornamentHeight.toDpPx()).alpha(0.85f))
    }
}

private val ORNAMENT_PATHS = listOf(
    "M4 10h60M136 10h60",
    "M64 10c9-10 18 10 27 0s18-10 27 0 18 10 27 0",
    "M64 10c9 10 18-10 27 0s18 10 27 0 18-10 27 0",
).map { PathParser().parsePathString(it).toPath() }

/** The rope divider under an opener — a rule with a twist (ornament.tsx, 200×20). */
@Composable
private fun Ornament(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.width / 200f
        withTransform({ scale(s, s, pivot = Offset.Zero) }) {
            ORNAMENT_PATHS.forEach { drawPath(it, color, style = Stroke(width = 1.25f, cap = StrokeCap.Round)) }
        }
    }
}

@Composable
private fun TextView(
    frag: Frag.TextFrag, flow: Int, type: ReaderType, palette: ReaderPalette,
    hit: ActiveHit?, markColor: Color, h: Dp,
) {
    val m = type.metrics
    val text = remember(frag, hit) {
        val slice = frag.text.substring(frag.start, frag.end)
        if (hit == null || hit.flow != flow || hit.pos.page != frag.pos.page) AnnotatedString(slice)
        else buildAnnotatedString {
            append(slice)
            BookSearch.ranges(frag.text, hit.query).forEach { r ->
                val s = r.first - frag.start
                val e = r.last + 1 - frag.start
                if (e <= 0 || s >= slice.length) return@forEach
                val active = hit.pos.block == frag.pos.block && hit.pos.item == frag.pos.item && hit.pos.char == r.first
                addStyle(
                    SpanStyle(background = markColor.copy(alpha = if (active) 0.55f else 0.25f)),
                    s.coerceAtLeast(0), e.coerceAtMost(slice.length),
                )
            }
        }
    }
    Row(Modifier.fillMaxWidth().height(h)) {
        if (frag.inset > 0f) {
            Box(Modifier.width(frag.inset.toDpPx()).fillMaxHeight(), contentAlignment = Alignment.TopEnd) {
                if (frag.marker != null) {
                    Text(
                        frag.marker,
                        style = type.style(frag.role),
                        color = palette.fg,
                        modifier = Modifier.padding(end = (m.base * 0.4f).toDpPx()),
                    )
                }
            }
        }
        Text(
            text,
            style = type.indented(frag.role, frag.indent),
            color = palette.fg,
            softWrap = true,
            modifier = Modifier.exactWidth((m.pageWidth - frag.inset).toInt()),
        )
    }
}

@Composable
private fun TableView(frag: Frag.TableFrag, type: ReaderType, palette: ReaderPalette, h: Dp) {
    val m = type.metrics
    val cols = frag.rows.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
    val border = 1.dp
    Column(
        Modifier.fillMaxWidth().height(h).verticalScroll(rememberScrollState())
    ) {
        frag.rows.forEachIndexed { r, row ->
            Row(Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                for (c in 0 until cols) {
                    Box(
                        Modifier.weight(1f).fillMaxHeight()
                            .drawBehind {
                                val w = border.toPx()
                                drawRect(palette.rule, topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(size.width, w))
                                drawRect(palette.rule, topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(w, size.height))
                                drawRect(palette.rule, topLeft = Offset(size.width - w, 0f), size = androidx.compose.ui.geometry.Size(w, size.height))
                                if (r == frag.rows.lastIndex) {
                                    drawRect(palette.rule, topLeft = Offset(0f, size.height - w), size = androidx.compose.ui.geometry.Size(size.width, w))
                                }
                            }
                            .padding(horizontal = m.cellPadH.toDpPx(), vertical = m.cellPadV.toDpPx()),
                    ) {
                        Text(
                            row.getOrNull(c).orEmpty(),
                            style = type.style(if (r == 0) TextRole.TableHead else TextRole.TableCell),
                            color = palette.fg,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TocRowView(
    frag: Frag.TocFrag, book: Book, type: ReaderType, palette: ReaderPalette, lang: String, h: Dp, onGoToPage: (Int) -> Unit,
) {
    val m = type.metrics
    val lesson = frag.number == null
    val role = if (lesson) TextRole.TocLesson else TextRole.TocChapter
    val em = m.size(role)
    val color = if (frag.page == null) palette.muted else palette.fg
    val sans = type.style(role).copy(fontFamily = FontFamily.Default, fontSize = type.style(role).fontSize * 0.85f, fontWeight = FontWeight.Normal)
    Row(
        Modifier.fillMaxWidth().height(h)
            .padding(start = if (lesson) m.tocLessonIndent.toDpPx() else 0.dp)
            .clickable(
                enabled = frag.page != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { frag.page?.let(onGoToPage) }
            .padding(horizontal = (0.25f * em).toDpPx(), vertical = m.tocRowPadV(lesson).toDpPx()),
        horizontalArrangement = Arrangement.spacedBy((0.6f * em).toDpPx()),
    ) {
        if (frag.number != null) {
            Text(
                formatNumber(frag.number, lang), style = sans, color = palette.muted, maxLines = 1,
                modifier = Modifier.widthIn(min = (1.6f * em * 0.85f).toDpPx()).alignByBaseline(),
            )
        }
        Text(
            frag.title, style = type.style(role), color = color,
            modifier = Modifier.widthIn(max = m.tocTitleWidth(lesson).toDpPx()).alignByBaseline(),
        )
        Canvas(Modifier.weight(1f).widthIn(min = em.toDpPx()).fillMaxHeight()) {
            val y = size.height - m.lineHeight(role) * 0.5f + em * 0.1f
            drawLine(
                palette.rule, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.dp.toPx(), 2.dp.toPx())),
            )
        }
        if (frag.page != null) {
            Text(
                formatNumber(book.printed(frag.page), lang), style = sans, color = palette.muted, maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

/**
 * The cover, at the size of the screen: the art over a tint averaged from
 * it, and the board's head carrying stage, title and grade (cover.tsx).
 */
@Composable
internal fun CoverScreen(book: Book, palette: ReaderPalette, dark: Boolean) {
    var tint by remember(book.cover.url) { mutableStateOf(Color(0xFF2B3A4A)) }
    val context = LocalContext.current
    BoxWithConstraints(Modifier.fillMaxSize().background(tint)) {
        val wide = maxWidth / maxHeight >= 5f / 7f
        if (book.cover.url != null) {
            AsyncImage(
                model = remember(book.cover.url) {
                    ImageRequest.Builder(context).data(book.cover.url).allowHardware(false).build()
                },
                contentDescription = book.title,
                contentScale = if (wide) ContentScale.Fit else ContentScale.Crop,
                alignment = if (wide) Alignment.Center else Alignment.TopCenter,
                onSuccess = { state ->
                    (state.result.drawable as? BitmapDrawable)?.bitmap?.let { dominantTint(it) }?.let { tint = it }
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                book.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).padding(16.dp),
            )
        }
        val plateWidth = minOf(maxWidth, 544.dp)
        val cqw = plateWidth.value / 100f
        val ink = if (dark) Color(0xFF1C1C1E) else palette.fg
        Column(
            Modifier.align(Alignment.TopCenter).width(plateWidth).fillMaxHeight(0.5f)
                .background(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.97f),
                        0.58f to Color.White.copy(alpha = 0.95f),
                        0.82f to Color.White.copy(alpha = 0.72f),
                        1f to Color.White.copy(alpha = 0f),
                    ),
                )
                .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            val serif = coverFont()
            book.cover.stage?.let {
                Text(it, color = ink, fontFamily = serif, fontWeight = FontWeight.SemiBold,
                    fontSize = (5 * cqw).coerceIn(16f, 21.6f).sp, textAlign = TextAlign.Center)
            }
            Text(
                elongate(book.title),
                color = ink, fontFamily = serif, fontWeight = FontWeight.Bold,
                fontSize = (21 * cqw).coerceIn(36f, 72f).sp, lineHeight = 1.15.em,
                textAlign = TextAlign.Center,
                style = TextStyle(lineBreak = LineBreak.Heading),
                modifier = Modifier.padding(vertical = 4.dp),
            )
            book.cover.gradeLine?.let {
                Text(it, color = ink, fontFamily = serif, fontWeight = FontWeight.SemiBold,
                    fontSize = (5.6f * cqw).coerceIn(16.8f, 24f).sp, textAlign = TextAlign.Center)
            }
            book.edition?.let {
                Text(it, color = ink.copy(alpha = 0.55f), fontSize = (3.4f * cqw).coerceIn(12.8f, 15.2f).sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun coverFont(): FontFamily = org.hogwarts.android.core.designsystem.theme.BrandFonts.ArabicText

/** Average the cover's pixels into a deep, slightly saturated tint — hsl(h, 28–55%, 30%). */
private fun dominantTint(source: Bitmap): Color? = runCatching {
    val small = Bitmap.createScaledBitmap(source, 12, 12, true)
    var r = 0f
    var g = 0f
    var b = 0f
    for (x in 0 until 12) for (y in 0 until 12) {
        val p = small.getPixel(x, y)
        r += (p shr 16 and 0xff) / 255f
        g += (p shr 8 and 0xff) / 255f
        b += (p and 0xff) / 255f
    }
    r /= 144f; g /= 144f; b /= 144f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2
    var h = 0f
    var s = 0f
    if (max != min) {
        val d = max - min
        s = if (l > 0.5f) d / (2 - max - min) else d / (max + min)
        h = when (max) {
            r -> (g - b) / d + (if (g < b) 6 else 0)
            g -> (b - r) / d + 2
            else -> (r - g) / d + 4
        } * 60f
    }
    Color.hsl(h.roundToInt().toFloat().coerceIn(0f, 359f), s.coerceIn(0.28f, 0.55f), 0.30f)
}.getOrNull()
