package org.hogwarts.android.feature.library.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.HogwartsSearchBar
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.library.R
import org.hogwarts.android.feature.library.domain.model.Book
import org.hogwarts.android.feature.library.domain.model.BookCategory

/**
 * Book catalog screen with search bar, category filter chips, and book list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBook: (String) -> Unit,
    onNavigateToMyBorrowings: () -> Unit,
    viewModel: BookCatalogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.library_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMyBorrowings) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = stringResource(R.string.library_my_borrowings_content_desc)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar
                HogwartsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    placeholder = stringResource(R.string.library_search_placeholder),
                    modifier = Modifier.padding(horizontal = AppleSpacing.Standard)
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Compact))

                // Category filter chips
                LazyRow(
                    modifier = Modifier.padding(horizontal = AppleSpacing.Standard),
                    horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text(stringResource(R.string.library_filter_all)) }
                        )
                    }
                    items(BookCategory.entries.toList()) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.displayName()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppleSpacing.Compact))

                if (uiState.filteredBooks.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Default.Book,
                        title = stringResource(R.string.library_no_books_title),
                        subtitle = if (uiState.searchQuery.isNotBlank())
                            stringResource(R.string.library_no_books_search_subtitle)
                        else
                            stringResource(R.string.library_no_books_category_subtitle),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Small),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = AppleSpacing.Standard,
                            vertical = AppleSpacing.Compact
                        )
                    ) {
                        items(uiState.filteredBooks, key = { it.id }) { book ->
                            BookCard(
                                book = book,
                                onClick = { onNavigateToBook(book.id) }
                            )
                        }
                    }
                }

                // Error message
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(AppleSpacing.Standard)
                    )
                }
            }
        }
    }
}

@Composable
private fun BookCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppleSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover image
            if (book.coverImageUrl != null) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = "${book.title} cover",
                    modifier = Modifier
                        .size(width = 60.dp, height = 80.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(width = 60.dp, height = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppleSpacing.Small))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Tiny))

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Tiny))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        text = book.category.displayName(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusBadge(
                        text = if (book.isAvailable)
                            "${book.availableCopies} available"
                        else
                            "Unavailable",
                        color = if (book.isAvailable)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
