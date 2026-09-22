package org.hogwarts.android.feature.subjects.textbook.domain

/**
 * A textbook as the web reader holds it (`textbook/load.ts`): the book's
 * pages of blocks grouped into flows — the front matter, then one per
 * chapter — with the contents resolved to PDF pages and the openers placed
 * on the pages chapters and lessons start. The phone only typesets it.
 */
data class Book(
    val slug: String,
    val pdfUrl: String,
    /** The book's folder on the CDN; figure `src`s are relative to it. */
    val assetBaseUrl: String,
    val title: String,
    val edition: String?,
    val lang: String,
    val rtl: Boolean,
    /** PDF index − printed page number; null hides the printed folios. */
    val offset: Int?,
    val cover: BookCover,
    val toc: List<TocChapter>,
    val sections: List<BookSection>,
) {
    /** The number printed on PDF page [page], as the web's `printed()`. */
    fun printed(page: Int): Int = if (offset != null && page - offset > 0) page - offset else page
}

data class BookCover(val url: String?, val stage: String?, val gradeLine: String?)

data class TocChapter(val id: String, val name: String, val page: Int?, val lessons: List<TocLesson>)

data class TocLesson(val id: String, val name: String, val page: Int?)

data class BookSection(
    /** "front" · "chapter" · "chunk". */
    val kind: String,
    /** The running head over its pages. */
    val title: String,
    val kicker: String?,
    val pages: List<BookPage>,
)

data class BookPage(
    /** PDF page number; null for text before the first page marker. */
    val number: Int?,
    val empty: Boolean,
    val opener: Opener?,
    val blocks: List<Block>,
)

/** The designed heading on the page a chapter or lesson opens. */
data class Opener(val kicker: String?, val title: String, val chapter: Boolean)

sealed interface Block {
    data class Heading(val level: Int, val text: String) : Block
    data class Paragraph(val text: String) : Block
    data class ListBlock(val ordered: Boolean, val items: List<String>) : Block
    data class Table(val rows: List<List<String>>) : Block
    data object Rule : Block
    /** A figure: the scanned page it sits on, `pages/N.webp`. */
    data class Image(val alt: String, val src: String) : Block
}

/** What the reader opens on: the book, or the web's fallback with its PDF. */
sealed interface TextbookLoad {
    data class Ready(val book: Book) : TextbookLoad
    /** The text could not be loaded right now (`unavailable`). */
    data class Unavailable(val pdfUrl: String?) : TextbookLoad
    /** The book has no transcribed text yet (`noText`). */
    data class NoText(val pdfUrl: String?) : TextbookLoad
}
