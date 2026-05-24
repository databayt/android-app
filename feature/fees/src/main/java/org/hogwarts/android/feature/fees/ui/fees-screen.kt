package org.hogwarts.android.feature.fees.ui

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
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.fees.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.fees.domain.model.FeeStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val formatter = viewModel.localeFormatter

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fees_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.fees_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.fees.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AppleInsetGroupedList(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                state = listState
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppleSpacing.Compact),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                    ) {
                        val allLabel = stringResource(R.string.fees_filter_all)
                        val statusLabels = mapOf(
                            FeeStatus.PAID.name to stringResource(R.string.fees_status_paid),
                            FeeStatus.PENDING.name to stringResource(R.string.fees_status_pending),
                            FeeStatus.OVERDUE.name to stringResource(R.string.fees_status_overdue),
                            FeeStatus.PARTIAL.name to stringResource(R.string.fees_status_partial)
                        )
                        val filters = listOf(null to allLabel) + FeeStatus.entries.map {
                            it.name to (statusLabels[it.name] ?: it.name)
                        }
                        filters.forEach { (status, label) ->
                            FilterChip(
                                selected = uiState.selectedStatusFilter == status,
                                onClick = { viewModel.onStatusFilterChanged(status) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                if (uiState.fees.isEmpty()) {
                    item {
                        EmptyState(
                            icon = HogwartsIcons.Fees,
                            title = stringResource(R.string.fees_empty_title),
                            subtitle = stringResource(R.string.fees_empty_subtitle)
                        )
                    }
                } else {
                    item {
                        AppleListSection(header = stringResource(R.string.fees_records_count, uiState.fees.size)) {
                            uiState.fees.forEachIndexed { index, fee ->
                                AppleListRow(
                                    showDivider = index < uiState.fees.size - 1
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = fee.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = stringResource(R.string.fees_due_date, formatter.formatDate(fee.dueDate)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.fees_amount_dollar, fee.amount),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            StatusBadge(
                                                text = when (fee.status) {
                                                    FeeStatus.PAID -> stringResource(R.string.fees_status_paid)
                                                    FeeStatus.PENDING -> stringResource(R.string.fees_status_pending)
                                                    FeeStatus.OVERDUE -> stringResource(R.string.fees_status_overdue)
                                                    FeeStatus.PARTIAL -> stringResource(R.string.fees_status_partial)
                                                },
                                                color = when (fee.status) {
                                                    FeeStatus.PAID -> MaterialTheme.colorScheme.tertiary
                                                    FeeStatus.PENDING -> MaterialTheme.colorScheme.secondary
                                                    FeeStatus.OVERDUE -> MaterialTheme.colorScheme.error
                                                    FeeStatus.PARTIAL -> MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    }
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
