package org.hogwarts.android.feature.subjects.textbook.engine

import org.hogwarts.android.feature.subjects.textbook.domain.Block
import org.hogwarts.android.feature.subjects.textbook.domain.Book

/** A search hit: where it sits in the book and its ±70 characters of context. */
data class SearchHit(
    val flow: Int,
    /** PDF page, for the result's "صفحة N". */
    val page: Int?,
    val pos: Pos,
    val before: String,
    val hit: String,
    val after: String,
)

/**
 * Book search — the web's `search.ts` over the book's text rather than the
 * DOM: each page's blocks folded once into an index, a query folded the same
 * way, at most 80 hits, each placed at its block and character so the reader
 * can open the screen that shows it.
 */
class BookSearch(book: Book) {
    private class Segment(val start: Int, val length: Int, val block: Int, val item: Int)

    private class Entry(
        val flow: Int, val pageIdx: Int, val page: Int?,
        val text: String, val norm: String, val map: IntArray, val segments: List<Segment>,
    )

    private val index: List<Entry> = buildList {
        book.sections.forEachIndexed { flow, section ->
            section.pages.forEachIndexed { pi, page ->
                val sb = StringBuilder()
                val segments = mutableListOf<Segment>()
                fun add(text: String, block: Int, item: Int = 0) {
                    val t = text.trim()
                    if (t.isEmpty()) return
                    if (sb.isNotEmpty()) sb.append('\n')
                    segments += Segment(sb.length, text.length, block, item)
                    sb.append(text)
                }
                page.blocks.forEachIndexed { bi, block ->
                    when (block) {
                        is Block.Paragraph -> add(block.text, bi)
                        is Block.Heading -> add(block.text, bi)
                        is Block.ListBlock -> block.items.forEachIndexed { i, item -> add(item, bi, i) }
                        is Block.Table -> add(block.rows.joinToString(" ") { it.joinToString(" ") }, bi)
                        else -> Unit
                    }
                }
                if (sb.isEmpty()) return@forEachIndexed
                val text = sb.toString()
                val (norm, map) = SearchFold.withMap(text)
                add(Entry(flow, pi, page.number, text, norm, map, segments))
            }
        }
    }

    fun run(query: String, max: Int = 80): List<SearchHit> {
        val needle = SearchFold.normalize(query)
        if (needle.length < 2) return emptyList()
        val results = mutableListOf<SearchHit>()
        for (entry in index) {
            var from = 0
            while (true) {
                val at = entry.norm.indexOf(needle, from)
                if (at < 0) break
                val s = entry.map[at]
                val e = entry.map[at + needle.length - 1] + 1
                val seg = entry.segments.lastOrNull { it.start <= s } ?: entry.segments.first()
                results += SearchHit(
                    flow = entry.flow,
                    page = entry.page,
                    pos = Pos(entry.pageIdx, seg.block, seg.item, (s - seg.start).coerceAtLeast(0)),
                    before = entry.text.substring(maxOf(0, s - 70), s).replace('\n', ' '),
                    hit = entry.text.substring(s, e),
                    after = entry.text.substring(e, minOf(entry.text.length, e + 70)).replace('\n', ' '),
                )
                if (results.size >= max) return results
                from = at + needle.length
            }
        }
        return results
    }

    companion object {
        /** Every occurrence of [query] in [text], as source ranges — the page's `<mark>`s. */
        fun ranges(text: String, query: String): List<IntRange> {
            val needle = SearchFold.normalize(query)
            if (needle.length < 2) return emptyList()
            val (norm, map) = SearchFold.withMap(text)
            val out = mutableListOf<IntRange>()
            var from = 0
            while (true) {
                val at = norm.indexOf(needle, from)
                if (at < 0) break
                out += map[at]..map[at + needle.length - 1]
                from = at + needle.length
            }
            return out
        }
    }
}
