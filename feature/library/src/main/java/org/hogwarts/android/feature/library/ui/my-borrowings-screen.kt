package org.hogwarts.android.feature.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.library.R
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.feature.library.domain.model.Borrowing
import org.hogwarts.android.feature.library.domain.model.BorrowingStatus

/**
 * My Borrowings screen showing active borrowings with due dates,
 * overdue items in red, renewal button, and history section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBorrowingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyBorrowingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val formatter = viewModel.localeFormatter

    // Show success snackbar
    val renewSuccessMsg = stringResource(R.string.library_renew_success)
    LaunchedEffect(uiState.renewSuccess) {
        if (uiState.renewSuccess) {
            snackbarHostState.showSnackbar(renewSuccessMsg)
            viewModel.clearRenewSuccess()
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
                title = { Text(stringResource(R.string.library_my_borrowings_title)) },
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
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.activeBorrowings.isEmpty() && uiState.historyBorrowings.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    icon = Icons.Default.MenuBook,
                    title = "No Borrowings",
                    subtitle = "You haven't borrowed any books yet. Visit the library catalog to find books.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppleSpacing.Small),
                    contentPadding = PaddingValues(
                        horizontal = AppleSpacing.Standard,
                        vertical = AppleSpacing.Compact
                    )
                ) {
                    // Active borrowings section
                    if (uiState.activeBorrowings.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.library_active_section),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = AppleSpacing.Tiny)
                            )
                        }
                        items(uiState.activeBorrowings, key = { it.id }) { borrowing ->
                            BorrowingCard(
                                borrowing = borrowing,
                                formatter = formatter,
                                isRenewing = uiState.renewingId == borrowing.id,
                                onRenew = { viewModel.renewBorrowing(borrowing.id) }
                            )
                        }
                    }

                    // History section
                    if (uiState.historyBorrowings.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                            Text(
                                text = stringResource(R.string.library_history_section),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = AppleSpacing.Tiny)
                            )
                        }
                        items(uiState.historyBorrowings, key = { it.id }) { borrowing ->
                            BorrowingCard(
                                borrowing = borrowing,
                                formatter = formatter,
                                isRenewing = false,
                                onRenew = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BorrowingCard(
    borrowing: Borrowing,
    formatter: LocaleFormatter,
    isRenewing: Boolean,
    onRenew: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppleSpacing.Standard)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = borrowing.bookTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(AppleSpacing.Compact))
                StatusBadge(
                    text = borrowing.status.displayName(),
                    color = when (borrowing.status) {
                        BorrowingStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        BorrowingStatus.RENEWED -> MaterialTheme.colorScheme.tertiary
                        BorrowingStatus.OVERDUE -> MaterialTheme.colorScheme.error
                        BorrowingStatus.RETURNED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(AppleSpacing.Compact))

            // Borrowed date
            Text(
                text = stringResource(R.string.library_borrowed_date, formatter.formatDate(borrowing.borrowedDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Due date - highlighted red if overdue
            Text(
                text = stringResource(R.string.library_due_date, formatter.formatDate(borrowing.dueDate)),
                style = MaterialTheme.typography.bodySmall,
                color = if (borrowing.isOverdue)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Returned date
            borrowing.returnedDate?.let { returnedDate ->
                Text(
                    text = stringResource(R.string.library_returned_date, formatter.formatDate(returnedDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Fine
            borrowing.fine?.let { fine ->
                if (fine > 0) {
                    Text(
                        text = "Fine: $${"%.2f".format(fine)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Renew button for active/overdue borrowings
            if (onRenew != null && borrowing.isActive) {
                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                Button(
                    onClick = onRenew,
                    enabled = !isRenewing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    if (isRenewing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(AppleSpacing.Compact))
                    }
                    Text(
                        text = "Renew",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
