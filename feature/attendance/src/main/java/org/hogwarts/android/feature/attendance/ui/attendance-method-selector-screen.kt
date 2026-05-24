package org.hogwarts.android.feature.attendance.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.attendance.domain.model.AttendanceMethod
import org.hogwarts.android.feature.attendance.domain.model.MethodType

/**
 * Attendance Method Selector screen.
 *
 * Allows admins/teachers to configure which attendance capture methods are enabled
 * (Manual, Geofence, NFC, Bluetooth, Barcode, QR) and set the default method.
 * Shows permission status indicators for each method.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceMethodSelectorScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttendanceMethodSelectorViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val settingsSavedMessage = stringResource(R.string.attendance_settings_saved)
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(settingsSavedMessage)
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.attendance_methods_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.attendance_back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            uiState.error != null && uiState.methods.isEmpty() -> {
                EmptyState(
                    icon = HogwartsIcons.Attendance,
                    title = stringResource(R.string.attendance_error_title),
                    subtitle = uiState.error ?: stringResource(R.string.attendance_something_went_wrong),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(AppleSpacing.Standard),
                    verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
                ) {
                    // Description
                    Text(
                        text = stringResource(R.string.attendance_methods_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Method cards
                    uiState.methods.forEach { method ->
                        MethodCard(
                            method = method,
                            permissionStatus = uiState.permissionStatuses[method.type]
                                ?: PermissionStatus.NOT_REQUESTED,
                            onToggle = { viewModel.toggleMethod(method.type) },
                            onSetDefault = { viewModel.setDefaultMethod(method.type) }
                        )
                    }

                    Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                    // Save button
                    Button(
                        onClick = { viewModel.saveMethods() },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(AppleSpacing.Compact))
                        }
                        Text(stringResource(R.string.attendance_save_settings))
                    }

                    // Error message
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodCard(
    method: AttendanceMethod,
    permissionStatus: PermissionStatus,
    onToggle: () -> Unit,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (method.enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppleSpacing.Standard),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            val displayName = method.type.toDisplayName(
                manual = stringResource(R.string.attendance_method_manual),
                geofence = stringResource(R.string.attendance_method_geofence),
                nfc = stringResource(R.string.attendance_method_nfc),
                bluetooth = stringResource(R.string.attendance_method_bluetooth),
                barcode = stringResource(R.string.attendance_method_barcode),
                qr = stringResource(R.string.attendance_method_qr)
            )
            val description = method.type.toDescription(
                manual = stringResource(R.string.attendance_method_manual_desc),
                geofence = stringResource(R.string.attendance_method_geofence_desc),
                nfc = stringResource(R.string.attendance_method_nfc_desc),
                bluetooth = stringResource(R.string.attendance_method_bluetooth_desc),
                barcode = stringResource(R.string.attendance_method_barcode_desc),
                qr = stringResource(R.string.attendance_method_qr_desc)
            )

            // Method icon
            Icon(
                imageVector = method.type.toIcon(),
                contentDescription = displayName,
                tint = if (method.enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(28.dp)
            )

            // Method info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (method.isDefault) {
                        Text(
                            text = stringResource(R.string.attendance_default_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Permission status
                if (method.type != MethodType.MANUAL) {
                    PermissionStatusIndicator(status = permissionStatus)
                }
            }

            // Default radio button
            if (method.enabled) {
                RadioButton(
                    selected = method.isDefault,
                    onClick = onSetDefault
                )
            }

            // Enable/disable toggle
            Switch(
                checked = method.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun PermissionStatusIndicator(
    status: PermissionStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
    ) {
        val (color, text) = when (status) {
            PermissionStatus.GRANTED -> MaterialTheme.colorScheme.primary to stringResource(R.string.attendance_permission_granted)
            PermissionStatus.DENIED -> MaterialTheme.colorScheme.error to stringResource(R.string.attendance_permission_denied)
            PermissionStatus.NOT_REQUESTED -> MaterialTheme.colorScheme.outline to stringResource(R.string.attendance_permission_not_requested)
            PermissionStatus.NOT_AVAILABLE -> MaterialTheme.colorScheme.error to stringResource(R.string.attendance_permission_not_available)
        }

        Icon(
            imageVector = if (status == PermissionStatus.GRANTED) Icons.Default.Check else Icons.Default.Edit,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun MethodType.toIcon(): ImageVector = when (this) {
    MethodType.MANUAL -> Icons.Default.Edit
    MethodType.GEOFENCE -> Icons.Default.LocationOn
    MethodType.NFC -> Icons.Default.Nfc
    MethodType.BLUETOOTH -> Icons.Default.Bluetooth
    MethodType.BARCODE -> Icons.Default.CameraAlt
    MethodType.QR -> Icons.Default.QrCode
}

private fun MethodType.toDisplayName(
    manual: String,
    geofence: String,
    nfc: String,
    bluetooth: String,
    barcode: String,
    qr: String
): String = when (this) {
    MethodType.MANUAL -> manual
    MethodType.GEOFENCE -> geofence
    MethodType.NFC -> nfc
    MethodType.BLUETOOTH -> bluetooth
    MethodType.BARCODE -> barcode
    MethodType.QR -> qr
}

private fun MethodType.toDescription(
    manual: String,
    geofence: String,
    nfc: String,
    bluetooth: String,
    barcode: String,
    qr: String
): String = when (this) {
    MethodType.MANUAL -> manual
    MethodType.GEOFENCE -> geofence
    MethodType.NFC -> nfc
    MethodType.BLUETOOTH -> bluetooth
    MethodType.BARCODE -> barcode
    MethodType.QR -> qr
}
