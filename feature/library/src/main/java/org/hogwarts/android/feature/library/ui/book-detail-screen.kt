package org.hogwarts.android.feature.library.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.library.R

/**
 * Book detail screen showing cover, title, author, description,
 * availability, shelf location, and borrow action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success snackbar when borrow succeeds
    val borrowSuccessMsg = stringResource(R.string.library_borrow_success)
    LaunchedEffect(uiState.borrowSuccess) {
        if (uiState.borrowSuccess) {
            snackbarHostState.showSnackbar(borrowSuccessMsg)
            viewModel.clearBorrowSuccess()
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library_book_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.library_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.book == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.book?.let { book ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(AppleSpacing.Standard)
                ) {
                    // Cover image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (book.coverImageUrl != null) {
                            AsyncImage(
                                model = book.coverImageUrl,
                                contentDescription = stringResource(R.string.library_book_cover_description, book.title),
                                modifier = Modifier
                                    .size(width = 140.dp, height = 200.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                    // Title
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(AppleSpacing.Tiny))

                    // Author
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(AppleSpacing.Compact))

                    // Category and ISBN badges
                    Row(horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)) {
                        StatusBadge(
                            text = book.category.displayName(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatusBadge(
                            text = "ISBN: ${book.isbn}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                    // Info card: availability and shelf location
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(AppleSpacing.Standard)) {
                            // Availability
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = if (book.isAvailable)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.padding(AppleSpacing.Tiny))
                                Text(
                                    text = "${book.availableCopies} of ${book.totalCopies} copies available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (book.isAvailable)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(AppleSpacing.Compact))

                            // Shelf location
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.padding(AppleSpacing.Tiny))
                                Text(
                                    text = "${book.sectionName} - ${book.shelfLocation}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                    // Description
                    if (book.description.isNotBlank()) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(AppleSpacing.Tiny))
                        Text(
                            text = book.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(AppleSpacing.Large))

                    // Borrow button
                    HogwartsButton(
                        text = if (book.isAvailable) "Request Borrow" else "Not Available",
                        onClick = { viewModel.borrowBook() },
                        enabled = book.isAvailable && !uiState.isBorrowing,
                        isLoading = uiState.isBorrowing,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
