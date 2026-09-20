package org.hogwarts.android.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.library.domain.model.Book

/**
 * A shelf — a heading over a row of covers, as `book-list/` draws one.
 *
 * The heading is 24sp bold and the covers are 128x192, both measured off the
 * live page. The row scrolls sideways and the covers keep their 2:3 shape:
 * a book jacket cropped to a square stops being a book jacket.
 */
@Composable
fun BookShelf(
    title: String,
    books: List<Book>,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (books.isEmpty()) return
    Column(modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = HogwartsTheme.colors.foreground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(books, key = { it.id }) { book ->
                BookCover(book = book, onClick = { onOpenBook(book.id) })
            }
        }
    }
}

/** One jacket on a shelf: the cover, the title under it, the author under that. */
@Composable
private fun BookCover(book: Book, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    Column(
        modifier = Modifier
            .width(128.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(width = 128.dp, height = 192.dp)
                .clip(HogwartsShapes.Sm)
                .background(colors.muted),
            contentAlignment = Alignment.Center,
        ) {
            if (!book.coverImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            text = book.title,
            style = HogwartsTheme.type.bodyMedium,
            color = colors.foreground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = book.author,
            style = HogwartsTheme.type.caption,
            color = colors.mutedForeground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The featured book — `collaborate-section.tsx`: one cover, the title at
 * 30sp semibold, the author under it, the blurb under that, and the way in.
 */
@Composable
fun FeaturedBook(
    book: Book,
    onOpenBook: (String) -> Unit,
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onOpenBook(book.id) },
    ) {
        if (!book.coverImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = book.coverImageUrl,
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(HogwartsShapes.Card),
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = book.title,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.foreground,
            )
            Text(
                text = book.author,
                style = HogwartsTheme.type.body,
                color = colors.mutedForeground,
            )
            if (book.description.isNotBlank()) {
                Text(
                    text = book.description,
                    style = HogwartsTheme.type.body,
                    color = colors.mutedForeground,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(Modifier.padding(top = 8.dp)) { action() }
        }
    }
}
