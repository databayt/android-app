package org.hogwarts.android.feature.attendance.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.feature.attendance.R

@Composable
fun ExcuseRequestDialog(
    date: String,
    onSubmit: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.attendance_submit_excuse_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.attendance_request_excuse_for, date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = AppleSpacing.Compact)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.attendance_reason_label)) },
                    placeholder = { Text(stringResource(R.string.attendance_reason_placeholder)) },
                    isError = isError,
                    supportingText = if (isError) {{ Text(stringResource(R.string.attendance_reason_required)) }} else null,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (reason.isBlank()) {
                        isError = true
                    } else {
                        onSubmit(reason)
                    }
                }
            ) {
                Text(stringResource(R.string.attendance_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.attendance_cancel))
            }
        }
    )
}
