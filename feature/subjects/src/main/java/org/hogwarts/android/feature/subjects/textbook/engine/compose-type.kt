package org.hogwarts.android.feature.subjects.textbook.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em

/**
 * The book's type as Compose styles — one builder for the paginator's
 * measurements and the page's drawing, so a counted line is a drawn line.
 * Line boxes are exactly `line-height` tall (no font padding, no trim), as
 * CSS sets them; breaking is greedy so the rest of a split paragraph breaks
 * where it did whole.
 */
class ReaderType(
    val metrics: ReaderMetrics,
    private val density: Density,
    private val bookFont: FontFamily,
    private val sansFont: FontFamily,
    /** `--book-weight`: 600 on the Bold theme. */
    private val bodyWeight: FontWeight,
    val rtl: Boolean,
) {
    private val cache = HashMap<TextRole, TextStyle>()

    fun style(role: TextRole): TextStyle = cache.getOrPut(role) {
        val size = metrics.size(role)
        with(density) {
            TextStyle(
                fontFamily = when (role) {
                    TextRole.Folio -> sansFont
                    else -> bookFont
                },
                fontWeight = when (role) {
                    TextRole.Heading1, TextRole.Heading2, TextRole.Heading3,
                    TextRole.ChapterTitle, TextRole.LessonTitle, TextRole.TableHead, TextRole.TocChapter,
                    -> FontWeight.Bold
                    else -> bodyWeight
                },
                fontSize = size.toSp(),
                lineHeight = metrics.lineHeight(role).toSp(),
                letterSpacing = when (role) {
                    TextRole.Kicker -> 0.12.em
                    TextRole.Folio -> 0.02.em
                    else -> 0.em
                },
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineBreak = LineBreak.Simple,
                hyphens = Hyphens.None,
                textDirection = if (rtl) TextDirection.Rtl else TextDirection.Ltr,
            )
        }
    }

    fun indented(role: TextRole, indentPx: Float): TextStyle =
        if (indentPx <= 0f) style(role) else with(density) {
            style(role).copy(textIndent = TextIndent(firstLine = indentPx.toSp()))
        }
}

/** [LineBreaker] over Compose's own text layout. Not thread-safe: one pagination at a time. */
class ComposeLineBreaker(
    private val measurer: TextMeasurer,
    private val type: ReaderType,
    private val density: Density,
) : LineBreaker {
    override fun lines(text: String, role: TextRole, width: Float, firstIndent: Float): IntArray {
        if (text.isEmpty()) return intArrayOf(0)
        val result = measurer.measure(
            text = AnnotatedString(text),
            style = type.indented(role, firstIndent),
            constraints = Constraints(maxWidth = width.toInt().coerceAtLeast(1)),
            layoutDirection = if (type.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            density = density,
            skipCache = true,
        )
        return IntArray(result.lineCount + 1) { i -> if (i < result.lineCount) result.getLineStart(i) else text.length }
    }
}
