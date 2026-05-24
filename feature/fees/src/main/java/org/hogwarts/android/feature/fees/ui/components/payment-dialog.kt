package org.hogwarts.android.feature.fees.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.fees.R

@Composable
fun PaymentDialog(
    feeName: String,
    amount: Double,
    currency: String = "SAR",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.fees_payment_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)) {
                Text(
                    text = feeName,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.fees_payment_dialog_amount_label), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.fees_payment_dialog_amount_value, currency, String.format("%.2f", amount)),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.fees_payment_dialog_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppleSpacing.Compact)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.fees_payment_dialog_pay_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.fees_payment_dialog_cancel))
            }
        }
    )
}
