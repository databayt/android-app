package org.hogwarts.android.feature.fees.ui

import android.content.Intent
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.fees.R
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.fees.domain.model.PaymentMethod
import org.hogwarts.android.feature.fees.domain.model.PaymentStatus
import org.hogwarts.android.feature.fees.domain.model.PaymentTransaction
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceiptScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentReceiptViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fees_receipt_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.fees_back))
                    }
                },
                actions = {
                    uiState.transaction?.let { transaction ->
                        IconButton(onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    buildReceiptShareText(transaction, context)
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.fees_receipt_share_chooser)))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.fees_receipt_share))
                        }
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
            uiState.error != null && uiState.transaction == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: stringResource(R.string.fees_error_occurred),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.transaction != null -> {
                val transaction = uiState.transaction!!
                val dateFormatter = DateTimeFormatter
                    .ofPattern("MMM d, yyyy 'at' h:mm a")
                    .withZone(ZoneId.systemDefault())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // School branding header
                    Text(
                        text = stringResource(R.string.fees_receipt_school_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.fees_receipt_payment_receipt),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Success icon
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.fees_receipt_completed_icon),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount
                    Text(
                        text = stringResource(R.string.fees_amount_dollar, transaction.amount),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = transaction.currency,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Receipt details card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ReceiptRow(
                                label = stringResource(R.string.fees_receipt_transaction_id),
                                value = transaction.transactionRef ?: transaction.id
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ReceiptRow(
                                label = stringResource(R.string.fees_receipt_amount),
                                value = stringResource(R.string.fees_receipt_amount_value, transaction.amount, transaction.currency)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ReceiptRow(
                                label = stringResource(R.string.fees_receipt_date),
                                value = transaction.processedAt?.let {
                                    dateFormatter.format(it)
                                } ?: stringResource(R.string.fees_pending)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ReceiptRow(
                                label = stringResource(R.string.fees_receipt_payment_method),
                                value = paymentMethodLabel(transaction.paymentMethod)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.fees_receipt_status),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                StatusBadge(
                                    text = paymentStatusLabel(transaction.status),
                                    color = paymentStatusColor(transaction.status)
                                )
                            }

                            transaction.description?.let { desc ->
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                ReceiptRow(label = stringResource(R.string.fees_receipt_description), value = desc)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        buildReceiptShareText(transaction, context)
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(
                                    Intent.createChooser(sendIntent, context.getString(R.string.fees_receipt_share_chooser))
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.fees_receipt_share),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.downloadPdf() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.fees_receipt_download_pdf),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun paymentStatusColor(status: PaymentStatus) = when (status) {
    PaymentStatus.PENDING -> MaterialTheme.colorScheme.secondary
    PaymentStatus.PROCESSING -> MaterialTheme.colorScheme.primary
    PaymentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
    PaymentStatus.FAILED -> MaterialTheme.colorScheme.error
    PaymentStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun paymentStatusLabel(status: PaymentStatus): String = when (status) {
    PaymentStatus.PENDING -> stringResource(R.string.fees_payment_status_pending)
    PaymentStatus.PROCESSING -> stringResource(R.string.fees_payment_status_processing)
    PaymentStatus.COMPLETED -> stringResource(R.string.fees_payment_status_completed)
    PaymentStatus.FAILED -> stringResource(R.string.fees_payment_status_failed)
    PaymentStatus.CANCELLED -> stringResource(R.string.fees_payment_status_cancelled)
}

@Composable
fun paymentMethodLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.CARD -> stringResource(R.string.fees_payment_method_card)
    PaymentMethod.BANK_TRANSFER -> stringResource(R.string.fees_payment_method_bank)
    PaymentMethod.WALLET -> stringResource(R.string.fees_payment_method_wallet)
}

private fun buildReceiptShareText(
    transaction: PaymentTransaction,
    context: android.content.Context
): String {
    val dateFormatter = DateTimeFormatter
        .ofPattern("MMM d, yyyy 'at' h:mm a")
        .withZone(ZoneId.systemDefault())

    val methodLabel = when (transaction.paymentMethod) {
        PaymentMethod.CARD -> context.getString(R.string.fees_payment_method_card)
        PaymentMethod.BANK_TRANSFER -> context.getString(R.string.fees_payment_method_bank)
        PaymentMethod.WALLET -> context.getString(R.string.fees_payment_method_wallet)
    }
    val statusLabel = when (transaction.status) {
        PaymentStatus.PENDING -> context.getString(R.string.fees_payment_status_pending)
        PaymentStatus.PROCESSING -> context.getString(R.string.fees_payment_status_processing)
        PaymentStatus.COMPLETED -> context.getString(R.string.fees_payment_status_completed)
        PaymentStatus.FAILED -> context.getString(R.string.fees_payment_status_failed)
        PaymentStatus.CANCELLED -> context.getString(R.string.fees_payment_status_cancelled)
    }

    return buildString {
        appendLine(context.getString(R.string.fees_receipt_share_header))
        appendLine(context.getString(R.string.fees_receipt_share_separator))
        appendLine(context.getString(R.string.fees_receipt_share_transaction, transaction.transactionRef ?: transaction.id))
        appendLine(context.getString(R.string.fees_receipt_share_amount, transaction.amount, transaction.currency))
        transaction.processedAt?.let { appendLine(context.getString(R.string.fees_receipt_share_date, dateFormatter.format(it))) }
        appendLine(context.getString(R.string.fees_receipt_share_method, methodLabel))
        appendLine(context.getString(R.string.fees_receipt_share_status, statusLabel))
        transaction.description?.let { appendLine(context.getString(R.string.fees_receipt_share_description, it)) }
    }
}
