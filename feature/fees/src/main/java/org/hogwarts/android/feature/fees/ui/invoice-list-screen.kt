package org.hogwarts.android.feature.fees.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.fees.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.feature.fees.domain.model.Invoice
import org.hogwarts.android.feature.fees.domain.model.InvoiceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoice: (invoiceId: String) -> Unit,
    onNavigateToTransactions: () -> Unit,
    viewModel: InvoiceListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val formatter = viewModel.localeFormatter

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fees_invoices_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.fees_back))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToTransactions,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(HogwartsIcons.History, contentDescription = null)
                Text(
                    text = stringResource(R.string.fees_invoices_transactions),
                    modifier = Modifier.padding(start = AppleSpacing.Compact)
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.invoices.isEmpty()) {
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
                    // Status filter chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppleSpacing.Compact),
                            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                        ) {
                            val filters = listOf(
                                null to stringResource(R.string.fees_invoices_filter_all),
                                InvoiceStatus.SENT to stringResource(R.string.fees_invoices_filter_sent),
                                InvoiceStatus.PAID to stringResource(R.string.fees_invoices_filter_paid),
                                InvoiceStatus.OVERDUE to stringResource(R.string.fees_invoices_filter_overdue),
                                InvoiceStatus.PARTIALLY_PAID to stringResource(R.string.fees_invoices_filter_partial)
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

                    if (uiState.invoices.isEmpty()) {
                        item {
                            EmptyState(
                                icon = HogwartsIcons.Fees,
                                title = stringResource(R.string.fees_invoices_empty_title),
                                subtitle = stringResource(R.string.fees_invoices_empty_subtitle)
                            )
                        }
                    } else {
                        item {
                            AppleListSection(header = stringResource(R.string.fees_invoices_count, uiState.invoices.size)) {
                                uiState.invoices.forEachIndexed { index, invoice ->
                                    AppleListRow(
                                        showDivider = index < uiState.invoices.size - 1
                                    ) {
                                        InvoiceCard(
                                            invoice = invoice,
                                            formatter = formatter,
                                            onClick = { onNavigateToInvoice(invoice.id) }
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
private fun InvoiceCard(
    invoice: Invoice,
    formatter: LocaleFormatter,
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
        Column(
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = invoice.invoiceNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = invoice.studentName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.fees_due_date, formatter.formatDate(invoice.dueDate)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Text(
                text = stringResource(R.string.fees_amount_dollar, invoice.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (invoice.balanceDue > java.math.BigDecimal.ZERO) {
                Text(
                    text = stringResource(R.string.fees_invoices_balance_due, invoice.balanceDue),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            StatusBadge(
                text = invoiceStatusLabel(invoice.status),
                color = invoiceStatusColor(invoice.status)
            )
        }
    }
}

@Composable
fun invoiceStatusColor(status: InvoiceStatus) = when (status) {
    InvoiceStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
    InvoiceStatus.SENT -> MaterialTheme.colorScheme.secondary
    InvoiceStatus.PAID -> MaterialTheme.colorScheme.tertiary
    InvoiceStatus.PARTIALLY_PAID -> MaterialTheme.colorScheme.primary
    InvoiceStatus.OVERDUE -> MaterialTheme.colorScheme.error
    InvoiceStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun invoiceStatusLabel(status: InvoiceStatus): String = when (status) {
    InvoiceStatus.DRAFT -> stringResource(R.string.fees_invoice_status_draft)
    InvoiceStatus.SENT -> stringResource(R.string.fees_invoice_status_sent)
    InvoiceStatus.PAID -> stringResource(R.string.fees_invoice_status_paid)
    InvoiceStatus.PARTIALLY_PAID -> stringResource(R.string.fees_invoice_status_partial)
    InvoiceStatus.OVERDUE -> stringResource(R.string.fees_invoice_status_overdue)
    InvoiceStatus.CANCELLED -> stringResource(R.string.fees_invoice_status_cancelled)
}
