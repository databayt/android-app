package org.hogwarts.android.feature.messaging.ui.whatsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.feature.messaging.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppSettingsDialog(
    onDismiss: () -> Unit,
    onNavigateToQR: () -> Unit,
    viewModel: WhatsAppViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val waColors = LocalWhatsAppColors.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.whatsapp_connection_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))

            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (uiState.isConnected) waColors.surfaceProduct else Color.Gray,
                            CircleShape,
                        ),
                )
                Text(
                    text = when (uiState.status) {
                        "connected" -> stringResource(R.string.whatsapp_status_connected)
                        "connecting" -> stringResource(R.string.whatsapp_status_connecting)
                        "qr_pending" -> stringResource(R.string.whatsapp_status_qr_pending)
                        else -> stringResource(R.string.whatsapp_status_disconnected)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            uiState.phone?.let { phone ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else if (uiState.isConnected) {
                OutlinedButton(
                    onClick = { viewModel.disconnect() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.whatsapp_disconnect))
                }
            } else {
                Button(
                    onClick = {
                        viewModel.connect()
                        onNavigateToQR()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = waColors.surfaceProduct,
                    ),
                ) {
                    Text(stringResource(R.string.whatsapp_connect_button), color = Color.White)
                }
            }

            uiState.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
