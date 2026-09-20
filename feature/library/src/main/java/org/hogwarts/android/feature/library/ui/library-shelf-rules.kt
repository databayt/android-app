package org.hogwarts.android.feature.library.ui

import org.hogwarts.android.feature.library.domain.model.Book

/**
 * How `/library` fills its four shelves — ported rule for rule from
 * `library/content.tsx`, so the phone and the site put the same book in the
 * same row rather than each inventing an order.
 *
 * The web's featured book is matched on its RAW English title, before
 * translation, because the row's photograph is of that exact edition. The
 * same title is matched here for the same reason.
 */
object LibraryShelves {

    /** `FEATURED_BOOK_TITLE` in content.tsx. */
    const val FEATURED_TITLE = "Harry Potter and the Philosopher's Stone"

    private const val PER_SHELF = 12

    /** Genres the literature shelf accepts, as `content.tsx` lists them. */
    private val LITERATURE = listOf("Fiction", "Classic", "Drama", "أدب", "شعر")

    /** And the science shelf's. */
    private val SCIENCE = listOf("Science", "History", "فلسفة")

    fun featured(books: List<Book>): Book? =
        books.firstOrNull { it.title == FEATURED_TITLE }

    /** Everything the shelves draw from — the list minus the featured book. */
    fun rest(books: List<Book>): List<Book> {
        val featured = featured(books)
        return if (featured == null) books else books.filterNot { it.id == featured.id }
    }

    /** Rows one and two are the first and second dozen, in the list's own order. */
    fun latest(rest: List<Book>): List<Book> = rest.take(PER_SHELF)

    fun featuredShelf(rest: List<Book>): List<Book> =
        rest.drop(PER_SHELF).take(PER_SHELF)

    fun literature(rest: List<Book>): List<Book> = byGenre(rest, LITERATURE)

    fun science(rest: List<Book>): List<Book> = byGenre(rest, SCIENCE)

    private fun byGenre(rest: List<Book>, genres: List<String>): List<Book> =
        rest.filter { book -> genres.any { book.genre.contains(it) } }.take(PER_SHELF)
}
