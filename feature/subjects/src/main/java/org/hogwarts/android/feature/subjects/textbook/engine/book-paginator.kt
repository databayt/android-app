package org.hogwarts.android.feature.subjects.textbook.engine

import org.hogwarts.android.feature.subjects.textbook.data.ReaderAnchor
import org.hogwarts.android.feature.subjects.textbook.domain.Block
import org.hogwarts.android.feature.subjects.textbook.domain.Book
import org.hogwarts.android.feature.subjects.textbook.domain.Opener
import org.hogwarts.android.feature.subjects.textbook.domain.TocChapter
import kotlin.math.floor
import kotlin.math.max

/** Where each line of a text starts, for one role and width; the last entry is the text's length. */
fun interface LineBreaker {
    fun lines(text: String, role: TextRole, width: Float, firstIndent: Float): IntArray
}

/**
 * A place in a flow: the page (index into the section's pages), the block on
 * it (−2 the opener, −1 the folio), the list item and the character. Screens
 * start at one, so position memory and search jumps compare them.
 */
data class Pos(val page: Int, val block: Int, val item: Int = 0, val char: Int = 0) : Comparable<Pos> {
    override fun compareTo(other: Pos): Int =
        compareValuesBy(this, other, Pos::page, Pos::block, Pos::item, Pos::char)
}

/** One piece of a screen, stacked top to bottom with [space] above it. */
sealed interface Frag {
    val pos: Pos
    val space: Float
    val height: Float

    data class OpenerFrag(
        override val pos: Pos, override val space: Float, override val height: Float,
        val opener: Opener, val toc: Boolean = false,
    ) : Frag

    data class FolioFrag(override val pos: Pos, override val space: Float, override val height: Float, val printed: Int) : Frag

    /** Lines [start, end) of a text; the indent and the list marker only ride its first line. */
    data class TextFrag(
        override val pos: Pos, override val space: Float, override val height: Float,
        val role: TextRole, val text: String, val start: Int, val end: Int,
        val indent: Float, val inset: Float, val marker: String?,
    ) : Frag

    data class TableFrag(override val pos: Pos, override val space: Float, override val height: Float, val rows: List<List<String>>) : Frag

    data class RuleFrag(override val pos: Pos, override val space: Float, override val height: Float) : Frag

    data class ImageFrag(override val pos: Pos, override val space: Float, override val height: Float, val url: String, val alt: String) : Frag

    /** A contents row: chapter [number] (null for a lesson), title, PDF page. */
    data class TocFrag(
        override val pos: Pos, override val space: Float, override val height: Float,
        val title: String, val page: Int?, val number: Int?, val chapter: Int,
    ) : Frag
}

enum class ScreenKind { Cover, Contents, Flow }

data class Screen(
    val kind: ScreenKind,
    /** Index into [Book.sections]; −1 for the cover and the contents. */
    val flow: Int,
    val frags: List<Frag>,
    /** PDF page at the top of the screen. */
    val page: Int?,
    val start: Pos,
)

/** The whole book laid out for one screen size and one setting of type. */
class Pagination(val screens: List<Screen>, private val book: Book) {
    private val flowStart = IntArray(book.sections.size) { flow -> screens.indexOfFirst { it.flow == flow } }

    val total get() = screens.size

    /**
     * The screen that shows [pos] in [flow]: the one holding the fragment of
     * that very block (and list item) that starts at or before it, else the
     * first fragment after it — a page's opener or a skipped block may not
     * exist as a fragment, and the reader should land where the page does.
     */
    fun screenFor(flow: Int, pos: Pos): Int {
        val first = flowStart.getOrNull(flow)?.takeIf { it >= 0 } ?: return 0
        var last: Pair<Int, Frag>? = null
        var i = first
        while (i < screens.size && screens[i].flow == flow) {
            for (f in screens[i].frags) {
                if (f.pos <= pos) {
                    last = i to f
                } else {
                    val (at, frag) = last ?: return i
                    val same = frag.pos.page == pos.page && frag.pos.block == pos.block && frag.pos.item == pos.item
                    return if (same) at else i
                }
            }
            i++
        }
        return last?.first ?: first
    }

    /** The screen a PDF page begins on (`#p-N`, a contents row, a bookmark). */
    fun screenForPage(page: Int): Int? {
        book.sections.forEachIndexed { flow, section ->
            val idx = section.pages.indexOfFirst { it.number == page }
            if (idx >= 0) return screenFor(flow, Pos(idx, -2))
        }
        return null
    }

    /** The web's `{sec, page, block}`: sec counts the cover and the contents first. */
    fun anchorOf(index: Int): ReaderAnchor {
        val s = screens.getOrNull(index) ?: return ReaderAnchor(0, null, 0)
        return when (s.kind) {
            ScreenKind.Cover -> ReaderAnchor(0, null, 0)
            ScreenKind.Contents -> ReaderAnchor(1, null, index - 1)
            ScreenKind.Flow -> ReaderAnchor(s.flow + 2, s.page, max(0, s.start.block))
        }
    }

    fun screenFor(anchor: ReaderAnchor): Int = when (anchor.sec) {
        0 -> 0
        1 -> (1 + anchor.block).coerceIn(0, total - 1)
        else -> {
            val flow = anchor.sec - 2
            val pages = book.sections.getOrNull(flow)?.pages
            if (pages == null) 0 else {
                val idx = pages.indexOfFirst { it.number == anchor.page }.coerceAtLeast(0)
                screenFor(flow, Pos(idx, anchor.block))
            }
        }
    }
}

/**
 * Lays a [Book] out in screens the way the web's column engine does: the
 * cover, the contents page, then each flow on fresh screens, its blocks
 * stacked with CSS's collapsed margins and split between lines where a
 * paragraph crosses the page foot (two lines at least on either side —
 * CSS's default orphans and widows). Headings and openers never end a
 * screen (`break-after: avoid`); tables, figures and contents rows never
 * split (`break-inside: avoid`).
 */
class BookPaginator(
    private val metrics: ReaderMetrics,
    private val breaker: LineBreaker,
) {
    fun paginate(book: Book, contentsLabel: String): Pagination {
        val screens = mutableListOf<Screen>()
        screens += Screen(ScreenKind.Cover, -1, emptyList(), null, Pos(0, 0))
        pack(tocUnits(book.toc, contentsLabel)).forEach { frags ->
            screens += Screen(ScreenKind.Contents, -1, frags, null, frags.firstOrNull()?.pos ?: Pos(0, 0))
        }
        book.sections.forEachIndexed { flow, section ->
            val units = mutableListOf<Unit>()
            section.pages.forEachIndexed { pi, page ->
                page.opener?.let { units += openerUnit(Pos(pi, -2), it, toc = false) }
                val n = page.number
                if (n != null && book.offset != null && n - book.offset > 0) {
                    units += Unit.Atom(
                        Pos(pi, -1), 0f, metrics.folioAfter, metrics.lineHeight(TextRole.Folio), false,
                    ) { pos, space, h -> Frag.FolioFrag(pos, space, h, n - book.offset) }
                }
                page.blocks.forEachIndexed { bi, block -> units += blockUnits(Pos(pi, bi), block, book.assetBaseUrl) }
            }
            val pages = pack(units).ifEmpty { listOf(emptyList()) }
            pages.forEach { frags ->
                val start = frags.firstOrNull()?.pos ?: Pos(0, -2)
                screens += Screen(ScreenKind.Flow, flow, frags, section.pages.getOrNull(start.page)?.number, start)
            }
        }
        return Pagination(screens, book)
    }

    // ── Units ──────────────────────────────────────────────────────────

    private sealed interface Unit {
        val pos: Pos
        val top: Float
        val bottom: Float
        val keepWithNext: Boolean

        class Atom(
            override val pos: Pos, override val top: Float, override val bottom: Float,
            val height: Float, override val keepWithNext: Boolean,
            val make: (Pos, Float, Float) -> Frag,
        ) : Unit

        class Text(
            override val pos: Pos, override val top: Float, override val bottom: Float,
            val role: TextRole, val text: String, val lines: IntArray, val lineHeight: Float,
            val indent: Float, val inset: Float, val marker: String?,
            /** A heading moves whole; body text splits. */
            val splittable: Boolean, override val keepWithNext: Boolean,
        ) : Unit {
            val count get() = lines.size - 1
        }
    }

    private fun text(
        pos: Pos, top: Float, bottom: Float, role: TextRole, text: String, width: Float,
        indent: Float = 0f, inset: Float = 0f, marker: String? = null, splittable: Boolean = true,
    ): Unit.Text = Unit.Text(
        pos, top, bottom, role, text, breaker.lines(text, role, width, indent), metrics.lineHeight(role),
        indent, inset, marker, splittable, keepWithNext = !splittable,
    )

    private fun blockUnits(pos: Pos, block: Block, assetBaseUrl: String): List<Unit> {
        val m = metrics
        val w = m.pageWidth
        return when (block) {
            is Block.Paragraph -> if (block.text.isBlank()) emptyList() else
                listOf(text(pos, 0f, m.paragraphAfter, TextRole.Body, block.text, w, indent = m.paragraphIndent))
            is Block.Heading -> {
                val role = m.heading(block.level)
                listOf(text(pos, m.headingBefore(role), m.headingAfter(role), role, block.text, w, splittable = false))
            }
            is Block.ListBlock -> block.items.mapIndexed { i, item ->
                text(
                    pos.copy(item = i),
                    if (i == 0) m.listMargin else m.listItemGap,
                    if (i == block.items.lastIndex) m.listMargin else m.listItemGap,
                    TextRole.Body, item, w - m.listIndent,
                    inset = m.listIndent, marker = if (block.ordered) "${i + 1}." else "•",
                )
            }
            is Block.Table -> listOf(
                Unit.Atom(pos, m.tableMargin, m.tableMargin, tableHeight(block.rows), false) { p, s, h ->
                    Frag.TableFrag(p, s, h, block.rows)
                },
            )
            Block.Rule -> listOf(Unit.Atom(pos, m.ruleMargin, m.ruleMargin, m.dp, false) { p, s, h -> Frag.RuleFrag(p, s, h) })
            is Block.Image -> listOf(
                Unit.Atom(pos, m.figureMargin, m.figureMargin, m.figureHeight, false) { p, s, h ->
                    Frag.ImageFrag(p, s, h, "$assetBaseUrl/${block.src}", block.alt)
                },
            )
        }
    }

    /** Equal columns at the page's width; each row as tall as its tallest cell; capped, then it scrolls. */
    fun tableHeight(rows: List<List<String>>): Float {
        val m = metrics
        val cols = rows.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: return 0f
        val cellWidth = m.pageWidth / cols - 2 * m.cellPadH - m.dp
        var total = m.dp
        rows.forEachIndexed { r, row ->
            val role = if (r == 0) TextRole.TableHead else TextRole.TableCell
            val tallest = row.maxOfOrNull { cell ->
                (breaker.lines(cell, role, cellWidth, 0f).size - 1).coerceAtLeast(1)
            } ?: 1
            total += tallest * m.lineHeight(role) + 2 * m.cellPadV + m.dp
        }
        return minOf(total, m.tableMaxHeight)
    }

    fun openerHeight(opener: Opener, toc: Boolean): Float {
        val m = metrics
        val titleRole = if (opener.chapter) TextRole.ChapterTitle else TextRole.LessonTitle
        val titleLines = (breaker.lines(opener.title, titleRole, m.pageWidth, 0f).size - 1).coerceAtLeast(1)
        var h = m.openerTop(opener.chapter) + titleLines * m.lineHeight(titleRole) + m.titleAfter(opener.chapter) +
            m.ornamentHeight + m.openerBottom(opener.chapter)
        if (!opener.kicker.isNullOrBlank()) {
            val kickerLines = (breaker.lines(opener.kicker, TextRole.Kicker, m.pageWidth, 0f).size - 1).coerceAtLeast(1)
            h += kickerLines * m.lineHeight(TextRole.Kicker) + m.kickerAfter
        }
        if (toc) h += m.tocTop
        return h
    }

    private fun openerUnit(pos: Pos, opener: Opener, toc: Boolean): Unit =
        Unit.Atom(pos, 0f, 0f, openerHeight(opener, toc), keepWithNext = true) { p, s, h ->
            Frag.OpenerFrag(p, s, h, opener, toc)
        }

    private fun tocUnits(toc: List<TocChapter>, label: String): List<Unit> {
        val m = metrics
        val units = mutableListOf(openerUnit(Pos(0, -2), Opener(null, label, chapter = true), toc = true))
        toc.forEachIndexed { ci, ch ->
            units += tocRow(Pos(ci, 0), ch.name, ch.page, ci + 1, ci, lesson = false)
            ch.lessons.forEachIndexed { li, lesson ->
                units += tocRow(Pos(ci, li + 1), lesson.name, lesson.page, null, ci, lesson = true)
            }
        }
        return units
    }

    private fun tocRow(pos: Pos, title: String, page: Int?, number: Int?, chapter: Int, lesson: Boolean): Unit {
        val m = metrics
        val role = if (lesson) TextRole.TocLesson else TextRole.TocChapter
        val lines = (breaker.lines(title, role, m.tocTitleWidth(lesson), 0f).size - 1).coerceAtLeast(1)
        val h = lines * m.lineHeight(role) + 2 * m.tocRowPadV(lesson)
        return Unit.Atom(pos, 0f, 0f, h, false) { p, s, hh -> Frag.TocFrag(p, s, hh, title, page, number, chapter) }
    }

    // ── Packing ────────────────────────────────────────────────────────

    private fun pack(units: List<Unit>): List<List<Frag>> {
        val limit = metrics.pageHeight
        val pages = mutableListOf<List<Frag>>()
        var cur = mutableListOf<Frag>()
        var used = 0f
        var prevBottom = 0f

        fun flush() {
            if (cur.isNotEmpty()) pages += cur
            cur = mutableListOf()
            used = 0f
            prevBottom = 0f
        }

        /** What the unit after [i] needs on the same screen when [i] keeps with it. */
        fun follower(i: Int): Float {
            val next = units.getOrNull(i + 1) ?: return 0f
            val gap = max(0f, next.top - units[i].bottom)
            return gap + when (next) {
                is Unit.Atom -> if (next.keepWithNext) next.height else minOf(next.height, metrics.bodyLine * 2)
                is Unit.Text -> minOf(next.count, 2) * next.lineHeight
            }
        }

        units.forEachIndexed { i, u ->
            when (u) {
                is Unit.Atom -> {
                    var space = if (cur.isEmpty()) 0f else max(prevBottom, u.top)
                    val need = space + u.height + if (u.keepWithNext) follower(i) else 0f
                    if (cur.isNotEmpty() && used + need > limit) {
                        flush()
                        space = 0f
                    }
                    cur += u.make(u.pos, space, u.height)
                    used += space + u.height
                    prevBottom = u.bottom
                }
                is Unit.Text -> {
                    val n = u.count
                    if (n <= 0) return@forEachIndexed
                    if (!u.splittable) {
                        var space = if (cur.isEmpty()) 0f else max(prevBottom, u.top)
                        val h = n * u.lineHeight
                        if (cur.isNotEmpty() && used + space + h + follower(i) > limit) {
                            flush()
                            space = 0f
                        }
                        cur += textFrag(u, 0, n, space)
                        used += space + h
                        prevBottom = u.bottom
                        return@forEachIndexed
                    }
                    var line = 0
                    while (line < n) {
                        val space = if (cur.isEmpty()) 0f else max(prevBottom, u.top)
                        val remaining = n - line
                        var fit = floor((limit - used - space) / u.lineHeight + 1e-3f).toInt().coerceIn(0, remaining)
                        if (fit < remaining && remaining >= 2) {
                            // Orphans and widows: two lines at least on each side of a break.
                            if (fit < 2) fit = 0
                            if (remaining - fit < 2) fit = remaining - 2
                            if (fit < 2) fit = 0
                        }
                        if (fit == 0) {
                            if (cur.isEmpty()) fit = minOf(remaining, max(1, floor(limit / u.lineHeight).toInt()))
                            else {
                                flush()
                                continue
                            }
                        }
                        cur += textFrag(u, line, line + fit, space)
                        used += space + fit * u.lineHeight
                        prevBottom = 0f
                        line += fit
                        if (line < n) flush()
                    }
                    prevBottom = u.bottom
                }
            }
        }
        flush()
        return pages
    }

    private fun textFrag(u: Unit.Text, from: Int, to: Int, space: Float): Frag.TextFrag {
        val start = u.lines[from]
        val end = u.lines[to]
        return Frag.TextFrag(
            pos = u.pos.copy(char = start), space = space, height = (to - from) * u.lineHeight,
            role = u.role, text = u.text, start = start, end = end,
            indent = if (from == 0) u.indent else 0f, inset = u.inset,
            marker = if (from == 0) u.marker else null,
        )
    }
}
