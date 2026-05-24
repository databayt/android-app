package org.hogwarts.android.feature.fees.ui

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.fees.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.fees.domain.model.PaymentMethod
import org.hogwarts.android.feature.fees.domain.model.PaymentStatus
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (transactionId: String) -> Unit,
    viewModel: TransactionHistoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    val dateFormatter = DateTimeFormatter
        .ofPattern("MMM d, yyyy")
        .withZone(ZoneId.systemDefault())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis
                        if (startMillis != null && endMillis != null) {
                            viewModel.onDateRangeChanged(
                                start = Instant.ofEpochMilli(startMillis),
                                end = Instant.ofEpochMilli(endMillis)
                            )
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.fees_transactions_date_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onDateRangeChanged(start = null, end = null)
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.fees_transactions_date_clear))
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.height(500.dp)
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fees_transactions_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.fees_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.fees_transactions_filter_date))
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AppleInsetGroupedList(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    // Date range indicator
                    if (uiState.startDate != null && uiState.endDate != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppleSpacing.Compact),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${dateFormatter.format(uiState.startDate)} - ${dateFormatter.format(uiState.endDate)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Status filter chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppleSpacing.Compact),
                            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                        ) {
                            val filters = listOf(
                                null to stringResource(R.string.fees_transactions_filter_all),
                                PaymentStatus.COMPLETED to stringResource(R.string.fees_transactions_filter_completed),
                                PaymentStatus.PENDING to stringResource(R.string.fees_transactions_filter_pending),
                                PaymentStatus.FAILED to stringResource(R.string.fees_transactions_filter_failed)
                            )
                            filters.forEach { (status, label) ->
                                FilterChip(
                                    selected = uiState.selectedStatus == status,
                                    onClick = { viewModel.onStatusFilterChanged(status) },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.transactions.isEmpty()) {
                        item {
                            EmptyState(
                                icon = HogwartsIcons.History,
                                title = stringResource(R.string.fees_transactions_empty_title),
                                subtitle = stringResource(R.string.fees_transactions_empty_subtitle)
                            )
                        }
                    } else {
                        item {
                            AppleListSection(header = stringResource(R.string.fees_transactions_count, uiState.transactions.size)) {
                                uiState.transactions.forEachIndexed { index, transaction ->
                                    AppleListRow(
                                        showDivider = index < uiState.transactions.size - 1
                                    ) {
                                        TransactionRow(
                                            transaction = transaction,
                                            dateFormatter = dateFormatter,
                                            onClick = { onNavigateToReceipt(transaction.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.error != null) {
                        item {
                            Text(
                                text = stringResource(R.string.fees_showing_cached_data, uiState.error ?: ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(AppleSpacing.Standard)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: PaymentTransaction,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Method icon
        Icon(
            imageVector = when (transaction.paymentMethod) {
                PaymentMethod.CARD -> Icons.Default.CreditCard
                PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                PaymentMethod.WALLET -> Icons.Default.Wallet
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp)
        )

        // Description and date
        Column(
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = transaction.description
                    ?: paymentMethodLabel(transaction.paymentMethod),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = transaction.processedAt?.let { dateFormatter.format(it) } ?: stringResource(R.string.fees_pending),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount and status
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Text(
                text = stringResource(R.string.fees_amount_dollar, transaction.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatusBadge(
                text = paymentStatusLabel(transaction.status),
                color = paymentStatusColor(transaction.status)
            )
        }
    }
}
