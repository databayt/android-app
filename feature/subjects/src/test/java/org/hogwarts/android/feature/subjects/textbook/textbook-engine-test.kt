package org.hogwarts.android.feature.subjects.textbook

import org.hogwarts.android.feature.subjects.textbook.data.ReaderAnchor
import org.hogwarts.android.feature.subjects.textbook.data.parseAnchor
import org.hogwarts.android.feature.subjects.textbook.data.parseBookmarks
import org.hogwarts.android.feature.subjects.textbook.domain.Block
import org.hogwarts.android.feature.subjects.textbook.domain.Book
import org.hogwarts.android.feature.subjects.textbook.domain.BookCover
import org.hogwarts.android.feature.subjects.textbook.domain.BookPage
import org.hogwarts.android.feature.subjects.textbook.domain.BookSection
import org.hogwarts.android.feature.subjects.textbook.domain.Opener
import org.hogwarts.android.feature.subjects.textbook.domain.TocChapter
import org.hogwarts.android.feature.subjects.textbook.engine.BookPaginator
import org.hogwarts.android.feature.subjects.textbook.engine.BookSearch
import org.hogwarts.android.feature.subjects.textbook.engine.Frag
import org.hogwarts.android.feature.subjects.textbook.engine.LineBreaker
import org.hogwarts.android.feature.subjects.textbook.engine.ReaderMetrics
import org.hogwarts.android.feature.subjects.textbook.engine.ScreenKind
import org.hogwarts.android.feature.subjects.textbook.engine.SearchFold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFoldTest {
    @Test fun `folds diacritics, hamza seats, taa marbuta and alef maqsura`() {
        assertEquals("الاميبا", SearchFold.normalize("الأمِيبَا"))
        assertEquals("مدرسه", SearchFold.normalize("مدرسة"))
        assertEquals("علي", SearchFold.normalize("على"))
        assertEquals("سوال", SearchFold.normalize("سؤال"))
        assertEquals("كتاب", SearchFold.normalize("كـتـاب"))
        assertEquals("dna rna", SearchFold.normalize("  DNA   RNA "))
    }

    @Test fun `map points folded characters back at their source`() {
        val source = "الأَميبا"
        val (norm, map) = SearchFold.withMap(source)
        assertEquals("الاميبا", norm)
        assertEquals(norm.length, map.size)
        assertEquals(source.indexOf('أ'), map[2])
        assertEquals(source.length - 1, map.last())
    }
}

/** Ten characters a line, whatever the role; a page of 100 px holds five 20-px body lines. */
private val fakeBreaker = LineBreaker { text, _, width, _ ->
    val perLine = (width / 10f).toInt().coerceAtLeast(1)
    val starts = (0 until text.length step perLine).toMutableList()
    (starts + text.length).toIntArray()
}

private fun metrics(height: Float = 100f) =
    ReaderMetrics(base = 10f, leading = 2f, dp = 1f, pageWidth = 100f, pageHeight = height)

private fun book(vararg pages: BookPage) = Book(
    slug = "b", pdfUrl = "", assetBaseUrl = "https://cdn/b", title = "Book", edition = null, lang = "ar", rtl = true,
    offset = null, cover = BookCover(null, null, null),
    toc = listOf(TocChapter("c1", "One", 1, emptyList())),
    sections = listOf(BookSection("chapter", "One", null, pages.toList())),
)

class BookPaginatorTest {
    @Test fun `cover, contents, then the flow`() {
        val b = book(BookPage(1, false, null, listOf(Block.Paragraph("x".repeat(10)))))
        val p = BookPaginator(metrics(), fakeBreaker).paginate(b, "Contents")
        assertEquals(ScreenKind.Cover, p.screens[0].kind)
        assertEquals(ScreenKind.Contents, p.screens[1].kind)
        assertEquals(ScreenKind.Flow, p.screens.last().kind)
    }

    @Test fun `a long paragraph splits at lines and keeps two on each side`() {
        // 12 lines of 20 px on 100-px pages: 5 + 5 + 2.
        val b = book(BookPage(1, false, null, listOf(Block.Paragraph("x".repeat(120)))))
        val flow = BookPaginator(metrics(), fakeBreaker).paginate(b, "C").screens.filter { it.kind == ScreenKind.Flow }
        val lines = flow.map { s -> s.frags.filterIsInstance<Frag.TextFrag>().sumOf { (it.end - it.start + 9) / 10 } }
        assertEquals(listOf(5, 5, 2), lines)
        val first = flow[0].frags.first() as Frag.TextFrag
        val second = flow[1].frags.first() as Frag.TextFrag
        assertTrue("indent only on the first line", first.indent > 0f && second.indent == 0f)
        assertEquals(first.end, second.start)
    }

    @Test fun `widows move a line rather than strand one`() {
        // 6 lines: 5 would leave 1 behind, so the first page takes 4.
        val b = book(BookPage(1, false, null, listOf(Block.Paragraph("x".repeat(60)))))
        val flow = BookPaginator(metrics(), fakeBreaker).paginate(b, "C").screens.filter { it.kind == ScreenKind.Flow }
        assertEquals(2, flow.size)
        assertEquals(40, (flow[0].frags.first() as Frag.TextFrag).end)
    }

    @Test fun `an opener never ends a screen`() {
        val filler = Block.Paragraph("x".repeat(40)) // 4 lines = 80 px
        val b = book(
            BookPage(1, false, null, listOf(filler)),
            BookPage(2, false, Opener("Unit 1", "Title", chapter = false), listOf(Block.Paragraph("y".repeat(10)))),
        )
        val flow = BookPaginator(metrics(height = 200f), fakeBreaker).paginate(b, "C").screens.filter { it.kind == ScreenKind.Flow }
        val opener = flow.indexOfFirst { s -> s.frags.any { it is Frag.OpenerFrag } }
        val text = flow.indexOfFirst { s -> s.frags.any { it is Frag.TextFrag && it.text.startsWith("y") } }
        assertEquals(opener, text)
    }

    @Test fun `a PDF page and a saved anchor resolve to the screen that shows them`() {
        val b = book(
            BookPage(7, false, null, listOf(Block.Paragraph("x".repeat(50)))),
            BookPage(8, false, null, listOf(Block.Paragraph("y".repeat(50)))),
        )
        val p = BookPaginator(metrics(), fakeBreaker).paginate(b, "C")
        val at = assertNotNull(p.screenForPage(8)).let { p.screenForPage(8)!! }
        assertEquals(8, p.screens[at].page)
        val anchor = p.anchorOf(at)
        assertEquals(at, p.screenFor(anchor))
    }

    @Test fun `bigger type keeps the reader on the same block`() {
        val blocks = (0 until 30).map { Block.Paragraph("p$it".padEnd(25, 'x')) }
        val b = book(BookPage(3, false, null, blocks))
        val small = BookPaginator(metrics(), fakeBreaker).paginate(b, "C")
        val at = small.screens.indexOfFirst { s -> s.frags.any { it.pos.block == 17 } }
        val anchor = small.anchorOf(at)
        val large = BookPaginator(ReaderMetrics(13f, 2f, 1f, 100f, 100f), fakeBreaker).paginate(b, "C")
        val there = large.screens[large.screenFor(anchor)]
        assertTrue(there.frags.any { it.pos.block == anchor.block })
    }

    @Test fun `figures reserve a page-capped box`() {
        val b = book(BookPage(1, false, null, listOf(Block.Image("fig", "pages/1.webp"))))
        val frag = BookPaginator(metrics(), fakeBreaker).paginate(b, "C").screens.last()
            .frags.filterIsInstance<Frag.ImageFrag>().single()
        assertEquals("https://cdn/b/pages/1.webp", frag.url)
        assertEquals(100f - 48f, frag.height)
    }
}

class BookSearchTest {
    @Test fun `hits carry their block and context`() {
        val b = book(
            BookPage(12, false, null, listOf(Block.Heading(1, "الانشطار الثنائي"), Block.Paragraph("تتكاثر الأميبا بالانشطار."))),
        )
        val hits = BookSearch(b).run("الاميبا")
        assertEquals(1, hits.size)
        assertEquals(12, hits[0].page)
        assertEquals(1, hits[0].pos.block)
        assertEquals("الأميبا", hits[0].hit)
        assertTrue(BookSearch(b).run("ا").isEmpty())
    }
}

class ReaderPrefsParseTest {
    @Test fun `reads the web's stored anchor and bookmarks`() {
        assertEquals(ReaderAnchor(3, 12, 4), parseAnchor("""{"sec":3,"page":12,"block":4}"""))
        assertEquals(ReaderAnchor(1, null, 0), parseAnchor("""{"sec":1,"page":null,"block":0}"""))
        assertEquals(null, parseAnchor("{}"))
        assertEquals(listOf(5, 12, 40), parseBookmarks("[5,12,40]"))
    }
}
