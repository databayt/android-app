package org.hogwarts.android.feature.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.kit.BrandBanner
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.library.R
import org.hogwarts.android.feature.library.ui.components.BookShelf
import org.hogwarts.android.feature.library.ui.components.FeaturedBook

/**
 * `/library` — the school's shelves, as the site lays them out: the green
 * brand banner, one featured book, then rows of jackets.
 *
 * The server cuts the rows with the page's own loader (`library/load.ts`), so
 * the phone and the site put the same book in the same row, in the reader's
 * language. Explore opens `/library/books`, as the banner's pill does.
 *
 * No top app bar and no filter chips. The platform header is the chrome on
 * every phone screen here, and the site has neither.
 */
@Composable
fun BookCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBook: (String) -> Unit,
    onNavigateToMyBorrowings: () -> Unit,
    onNavigateToAllBooks: () -> Unit = {},
    viewModel: BookCatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = HogwartsTheme.colors
    val home = uiState.home
    val featured = home?.featured

    LazyColumn(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item(key = "hero") {
            BrandBanner(
                headline = libraryHeadline(),
                modifier = Modifier.padding(horizontal = 16.dp),
                actions = {
                    PillButton(
                        label = stringResource(R.string.library_explore),
                        onClick = onNavigateToAllBooks,
                        variant = PillVariant.BrandWhite,
                    )
                    PillButton(
                        label = stringResource(R.string.library_my_borrowings),
                        onClick = onNavigateToMyBorrowings,
                        variant = PillVariant.BrandGhost,
                    )
                },
            )
        }

        if (uiState.isLoading && home == null) {
            item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        featured?.let { book ->
            item(key = "featured") {
                FeaturedBook(
                    book = book,
                    onOpenBook = onNavigateToBook,
                    action = {
                        PillButton(
                            label = stringResource(R.string.library_view_book),
                            onClick = { onNavigateToBook(book.id) },
                            variant = PillVariant.Muted,
                        )
                    },
                )
            }
        }

        val shelves = listOf(
            R.string.library_latest_books to home?.latest.orEmpty(),
            R.string.library_featured_books to home?.featuredShelf.orEmpty(),
            R.string.library_literature_books to home?.literature.orEmpty(),
            R.string.library_science_books to home?.science.orEmpty(),
        )
        shelves.forEach { (title, shelf) ->
            if (shelf.isNotEmpty()) {
                item(key = "shelf-$title") {
                    BookShelf(
                        title = stringResource(title),
                        books = shelf,
                        onOpenBook = onNavigateToBook,
                    )
                }
            }
        }

        if (!uiState.isLoading && home != null && home.total == 0) {
            item(key = "empty") {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) {
                    Text(
                        text = stringResource(R.string.library_no_books_title),
                        style = HogwartsTheme.type.body,
                        color = colors.mutedForeground,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        uiState.error?.let { message ->
            item(key = "error") {
                Text(
                    text = message,
                    style = HogwartsTheme.type.caption,
                    color = colors.destructive,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

/**
 * "مكتبة المدرسة تجمع كل ما يستحق أن تقرأه" — the site's headline, with its
 * first phrase carrying the weight, the way every brand banner in this app
 * sets the phrase the sentence turns on.
 */
@Composable
private fun libraryHeadline(): AnnotatedString {
    val lead = stringResource(R.string.library_headline_lead)
    val rest = stringResource(R.string.library_headline_rest)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(lead) }
        append(" ")
        append(rest)
    }
}
