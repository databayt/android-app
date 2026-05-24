package org.hogwarts.android.feature.attendance.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.attendance.domain.model.DestinationType
import org.hogwarts.android.feature.attendance.domain.model.HallPass
import org.hogwarts.android.feature.attendance.domain.model.HallPassStatus

/**
 * Hall Pass screen.
 *
 * Students see a request form and their pass history.
 * Teachers see pending requests with approve/deny actions and active passes with countdown timers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HallPassScreen(
    onNavigateBack: () -> Unit,
    viewModel: HallPassViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.attendance_hall_pass_title)) },
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
        floatingActionButton = {
            if (!uiState.isTeacher) {
                FloatingActionButton(
                    onClick = { viewModel.showRequestForm() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.attendance_request_hall_pass)
                    )
                }
            }
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

            uiState.error != null && uiState.hallPasses.isEmpty() -> {
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
                ) {
                    // Filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppleSpacing.Standard, vertical = AppleSpacing.Compact),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                    ) {
                        HallPassFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = uiState.selectedFilter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = {
                                    Text(
                                        text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }

                    val filtered = uiState.filteredPasses

                    if (filtered.isEmpty()) {
                        EmptyState(
                            icon = HogwartsIcons.Attendance,
                            title = stringResource(R.string.attendance_no_passes_title),
                            subtitle = stringResource(R.string.attendance_no_passes_subtitle),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = AppleSpacing.Standard,
                                vertical = AppleSpacing.Compact
                            )
                        ) {
                            items(
                                items = filtered,
                                key = { it.id }
                            ) { pass ->
                                HallPassCard(
                                    pass = pass,
                                    isTeacher = uiState.isTeacher,
                                    onApprove = { viewModel.approvePass(pass.id) },
                                    onDeny = { viewModel.denyPass(pass.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Request form dialog
        if (uiState.showRequestForm) {
            HallPassRequestDialog(
                selectedDestination = uiState.selectedDestination,
                reason = uiState.reason,
                isSubmitting = uiState.isSubmitting,
                error = uiState.submitError,
                onDestinationChange = { viewModel.updateDestination(it) },
                onReasonChange = { viewModel.updateReason(it) },
                onSubmit = { viewModel.submitRequest() },
                onDismiss = { viewModel.hideRequestForm() }
            )
        }
    }
}

@Composable
private fun HallPassCard(
    pass: HallPass,
    isTeacher: Boolean,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppleSpacing.Standard),
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
        ) {
            // Header row: student name + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pass.studentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(
                    text = pass.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = pass.status.toColor()
                )
            }

            // Destination
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
            ) {
                Text(
                    text = stringResource(R.string.attendance_destination_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pass.destination.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Reason
            Text(
                text = pass.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Countdown timer for active passes
            if (pass.isActive) {
                val remaining = pass.remainingSeconds
                if (remaining != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.attendance_time_remaining),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        val minutes = remaining / 60
                        val seconds = remaining % 60
                        Text(
                            text = stringResource(R.string.attendance_time_remaining_format, minutes, seconds),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Teacher approve/deny actions
            if (isTeacher && pass.status == HallPassStatus.REQUESTED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDeny
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.attendance_deny_icon_desc),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.attendance_deny))
                    }
                    Spacer(modifier = Modifier.width(AppleSpacing.Compact))
                    Button(
                        onClick = onApprove
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.attendance_approve_icon_desc),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.attendance_approve))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HallPassRequestDialog(
    selectedDestination: DestinationType,
    reason: String,
    isSubmitting: Boolean,
    error: String?,
    onDestinationChange: (DestinationType) -> Unit,
    onReasonChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.attendance_request_hall_pass)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
            ) {
                // Destination dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDestination.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.attendance_destination_field_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DestinationType.entries.forEach { destination ->
                            DropdownMenuItem(
                                text = {
                                    Text(destination.name.lowercase().replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    onDestinationChange(destination)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Reason field
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(R.string.attendance_reason_field_label)) },
                    placeholder = { Text(stringResource(R.string.attendance_reason_leave_placeholder)) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.attendance_submit))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text(stringResource(R.string.attendance_cancel))
            }
        }
    )
}

@Composable
private fun HallPassStatus.toColor() = when (this) {
    HallPassStatus.REQUESTED -> MaterialTheme.colorScheme.tertiary
    HallPassStatus.APPROVED -> MaterialTheme.colorScheme.primary
    HallPassStatus.ACTIVE -> MaterialTheme.colorScheme.secondary
    HallPassStatus.EXPIRED -> MaterialTheme.colorScheme.outline
    HallPassStatus.DENIED -> MaterialTheme.colorScheme.error
}
