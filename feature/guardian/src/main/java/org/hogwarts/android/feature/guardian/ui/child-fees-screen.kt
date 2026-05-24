package org.hogwarts.android.feature.guardian.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.designsystem.atom.ChildSelector
import org.hogwarts.android.core.designsystem.atom.ChildSelectorItem
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.guardian.R
import org.hogwarts.android.feature.guardian.data.repository.ChildFeeRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildFeesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChildFeesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guardian_fees_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.guardian_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.children.size > 1) {
                ChildSelector(
                    children = uiState.children.map { ChildSelectorItem(it.id, it.displayName, it.avatarUrl) },
                    selectedChildId = uiState.selectedChildId,
                    onChildSelected = viewModel::selectChild
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Balance summary
            FeeSummaryCard(
                totalOutstanding = uiState.totalOutstanding,
                overdueAmount = uiState.overdueAmount,
                formatter = viewModel.localeFormatter
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "All" to stringResource(R.string.guardian_fees_filter_all),
                    "Pending" to stringResource(R.string.guardian_fees_filter_pending),
                    "Paid" to stringResource(R.string.guardian_fees_filter_paid),
                    "Overdue" to stringResource(R.string.guardian_fees_filter_overdue)
                )
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.statusFilter == key,
                        onClick = { viewModel.setStatusFilter(key) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(uiState.filteredFees, key = { it.id }) { fee ->
                    FeeRecordRow(fee = fee, formatter = viewModel.localeFormatter)
                }
            }
        }
    }
}

@Composable
private fun FeeSummaryCard(
    totalOutstanding: Double,
    overdueAmount: Double,
    formatter: LocaleFormatter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(R.string.guardian_fees_outstanding), style = MaterialTheme.typography.labelMedium)
                Text(
                    text = formatter.formatCurrency(totalOutstanding),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (overdueAmount > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.guardian_fees_overdue), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = formatter.formatCurrency(overdueAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FeeRecordRow(
    fee: ChildFeeRecord,
    formatter: LocaleFormatter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fee.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.guardian_fees_due, fee.dueDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatter.formatCurrency(fee.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val localizedStatus = when (fee.status.lowercase()) {
                    "paid" -> stringResource(R.string.guardian_fees_filter_paid)
                    "pending" -> stringResource(R.string.guardian_fees_filter_pending)
                    "overdue" -> stringResource(R.string.guardian_fees_filter_overdue)
                    else -> fee.status
                }
                StatusBadge(
                    text = localizedStatus,
                    color = when (fee.status.lowercase()) {
                        "paid" -> MaterialTheme.colorScheme.primary
                        "pending" -> MaterialTheme.colorScheme.tertiary
                        "overdue" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
    }
}
