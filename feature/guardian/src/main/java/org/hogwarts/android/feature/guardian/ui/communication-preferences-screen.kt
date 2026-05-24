package org.hogwarts.android.feature.guardian.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.guardian.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationPreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CommunicationPreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guardian_prefs_title)) },
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notification types
            item {
                Text(stringResource(R.string.guardian_prefs_notification_types), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            item {
                PreferenceToggle(stringResource(R.string.guardian_prefs_attendance_alerts), uiState.attendanceEnabled) {
                    viewModel.toggleAttendance(it)
                }
            }
            item {
                PreferenceToggle(stringResource(R.string.guardian_prefs_grade_updates), uiState.gradesEnabled) {
                    viewModel.toggleGrades(it)
                }
            }
            item {
                PreferenceToggle(stringResource(R.string.guardian_prefs_fee_reminders), uiState.feesEnabled) {
                    viewModel.toggleFees(it)
                }
            }
            item {
                PreferenceToggle(stringResource(R.string.guardian_prefs_school_announcements), uiState.announcementsEnabled) {
                    viewModel.toggleAnnouncements(it)
                }
            }
            item {
                PreferenceToggle(stringResource(R.string.guardian_prefs_event_notifications), uiState.eventsEnabled) {
                    viewModel.toggleEvents(it)
                }
            }

            // Quiet hours
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.guardian_prefs_quiet_hours), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            item {
                PreferenceToggle(stringResource(R.string.guardian_prefs_enable_quiet_hours), uiState.quietHoursEnabled) {
                    viewModel.toggleQuietHours(it)
                }
            }
            if (uiState.quietHoursEnabled) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${uiState.quietHoursStart} - ${uiState.quietHoursEnd}",
                                style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = { /* Show time picker */ }) {
                                Text(stringResource(R.string.guardian_prefs_change))
                            }
                        }
                    }
                }
                item {
                    PreferenceToggle(stringResource(R.string.guardian_prefs_emergency_override), uiState.emergencyOverride) {
                        viewModel.toggleEmergencyOverride(it)
                    }
                }
            }

            // Save
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.savePreferences() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.guardian_prefs_save))
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggle(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
