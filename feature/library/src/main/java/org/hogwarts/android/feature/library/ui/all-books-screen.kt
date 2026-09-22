package org.hogwarts.android.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.library.R
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.ui.components.coverGround

private val GRADE_LEVELS = listOf("GENERAL", "KG", "PRIMARY", "INTERMEDIATE", "SECONDARY")

/**
 * `/library/books` (`book-list/all-books-content.tsx`): the title, a toolbar
 * of search · genre · grade, the count, then a two-column grid of 3:4 covers
 * and See More.
 */
@Composable
fun AllBooksScreen(
    onOpenBook: (String) -> Unit,
    viewModel: AllBooksViewModel = hiltViewModel(),
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val c = HogwartsTheme.colors
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text(stringResource(R.string.library_hero_title), color = c.foreground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.library_hero_subtitle), color = c.mutedForeground, fontSize = 16.sp)
                }
                Toolbar(s, viewModel)
                Text("${s.total} ${stringResource(R.string.library_books_in_library)}", color = c.mutedForeground, fontSize = 16.sp)
            }
        }
        if (s.books.isEmpty() && !s.isLoading) {
            item(span = { GridItemSpan(2) }) {
                Box(Modifier.fillMaxWidth().height(240.dp), Alignment.Center) {
                    Text(stringResource(R.string.library_no_results), color = c.mutedForeground, textAlign = TextAlign.Center)
                }
            }
        }
        items(s.books, key = { it.id }) { book -> BookCard(book) { onOpenBook(book.id) } }
        if (s.page < s.totalPages) {
            item(span = { GridItemSpan(2) }) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    Text(
                        stringResource(R.string.library_see_more), color = c.mutedForeground, fontSize = 16.sp,
                        modifier = Modifier.clickable(role = Role.Button, onClick = viewModel::seeMore).padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Toolbar(s: AllBooksUiState, vm: AllBooksViewModel) {
    val c = HogwartsTheme.colors
    var text by remember(s.search) { mutableStateOf(s.search) }
    val gradeLabels = mapOf(
        "GENERAL" to stringResource(R.string.library_grade_general),
        "KG" to stringResource(R.string.library_grade_kg),
        "PRIMARY" to stringResource(R.string.library_grade_primary),
        "INTERMEDIATE" to stringResource(R.string.library_grade_intermediate),
        "SECONDARY" to stringResource(R.string.library_grade_secondary),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.width(160.dp).height(36.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, c.input, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = text, onValueChange = { text = it }, singleLine = true,
                textStyle = TextStyle(color = c.foreground, fontSize = 14.sp), cursorBrush = SolidColor(c.foreground),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.setSearch(text) }),
                decorationBox = { inner ->
                    if (text.isEmpty()) Text(stringResource(R.string.library_search_books), color = c.mutedForeground, fontSize = 14.sp, maxLines = 1)
                    inner()
                },
            )
        }
        Select(
            label = stringResource(R.string.library_genre),
            value = s.genre.takeIf { it.isNotEmpty() },
            options = s.genres.map { it to it },
            onPick = vm::setGenre,
            modifier = Modifier.weight(1f),
        )
        Select(
            label = stringResource(R.string.library_grade),
            value = gradeLabels[s.gradeLevel],
            options = GRADE_LEVELS.map { it to gradeLabels.getValue(it) },
            onPick = vm::setGradeLevel,
            modifier = Modifier.weight(1f),
        )
    }
}

/** A shadcn select trigger; its first item clears the filter, as the web's `__all__` does. */
@Composable
private fun Select(
    label: String,
    value: String?,
    options: List<Pair<String, String>>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = HogwartsTheme.colors
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, c.input, RoundedCornerShape(8.dp))
                .clickable(role = Role.Button) { open = true }.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                value ?: label, color = if (value == null) c.mutedForeground else c.foreground, fontSize = 14.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            Text("⌄", color = c.mutedForeground, fontSize = 14.sp)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text(label) }, onClick = { open = false; onPick("") })
            options.forEach { (key, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { open = false; onPick(key) })
            }
        }
    }
}

/** `book-card.tsx`: a 3:4 cover on the book's colour; the title and author in white when there is no art. */
@Composable
private fun BookCard(book: Book, onClick: () -> Unit) {
    var failed by remember(book.coverImageUrl) { mutableStateOf(false) }
    Box(
        Modifier.fillMaxWidth().aspectRatio(3f / 4f).clip(RoundedCornerShape(8.dp))
            .background(coverGround(book.coverColor)).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val url = book.coverImageUrl
        if (!url.isNullOrBlank() && !url.contains("placeholder") && !failed) {
            AsyncImage(url, book.title, contentScale = ContentScale.Crop, onError = { failed = true }, modifier = Modifier.fillMaxSize())
        } else {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(book.title, color = androidx.compose.ui.graphics.Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 3)
                Text(book.author, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
