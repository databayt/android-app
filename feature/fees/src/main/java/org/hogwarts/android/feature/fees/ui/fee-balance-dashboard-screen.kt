package org.hogwarts.android.feature.fees.ui

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.fees.R
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import java.math.BigDecimal
import java.time.LocalDate
import org.hogwarts.android.core.common.utils.LocaleFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeBalanceDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToPayment: () -> Unit,
    viewModel: FeeBalanceDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fees_balance_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.fees_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Outstanding balance card (large, prominent)
                    OutstandingBalanceCard(
                        outstanding = uiState.outstandingBalance,
                        currency = uiState.currency
                    )

                    // Overdue amount card
                    if (uiState.overdueAmount > BigDecimal.ZERO) {
                        OverdueCard(
                            overdueAmount = uiState.overdueAmount,
                            currency = uiState.currency
                        )
                    }

                    // Next due date with countdown
                    uiState.nextDueDate?.let { dueDate ->
                        NextDueDateCard(
                            dueDate = dueDate,
                            nextDueAmount = uiState.nextDueAmount,
                            currency = uiState.currency,
                            formatter = viewModel.localeFormatter
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick action buttons
                    Text(
                        text = stringResource(R.string.fees_balance_quick_actions),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    QuickActionButton(
                        icon = Icons.Default.Payment,
                        label = stringResource(R.string.fees_balance_action_pay_now),
                        description = stringResource(R.string.fees_balance_action_pay_description),
                        onClick = onNavigateToPayment
                    )

                    QuickActionButton(
                        icon = Icons.Default.Receipt,
                        label = stringResource(R.string.fees_balance_action_invoices),
                        description = stringResource(R.string.fees_balance_action_invoices_description),
                        onClick = onNavigateToInvoices
                    )

                    QuickActionButton(
                        icon = Icons.Default.Refresh,
                        label = stringResource(R.string.fees_balance_action_transactions),
                        description = stringResource(R.string.fees_balance_action_transactions_description),
                        onClick = onNavigateToTransactions
                    )

                    // Error message
                    uiState.error?.let { error ->
                        Text(
                            text = stringResource(R.string.fees_balance_cached_data, error),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutstandingBalanceCard(
    outstanding: BigDecimal,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.fees_balance_outstanding),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.fees_amount_dollar, outstanding),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currency,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun OverdueCard(
    overdueAmount: BigDecimal,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Overdue Amount",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Please pay immediately",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "$${overdueAmount}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NextDueDateCard(
    dueDate: LocalDate,
    nextDueAmount: BigDecimal,
    currency: String,
    formatter: LocaleFormatter,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate)

    val countdownText = when {
        daysUntilDue < 0 -> "${-daysUntilDue} days overdue"
        daysUntilDue == 0L -> "Due today"
        daysUntilDue == 1L -> "Due tomorrow"
        else -> "$daysUntilDue days remaining"
    }

    val containerColor = when {
        daysUntilDue < 0 -> MaterialTheme.colorScheme.errorContainer
        daysUntilDue <= 7 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        daysUntilDue < 0 -> MaterialTheme.colorScheme.onErrorContainer
        daysUntilDue <= 7 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Next Due Date",
                        style = MaterialTheme.typography.titleSmall,
                        color = contentColor
                    )
                    Text(
                        text = formatter.formatDate(dueDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${nextDueAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = countdownText,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = HogwartsIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
