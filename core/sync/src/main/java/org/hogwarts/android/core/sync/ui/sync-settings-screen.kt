package org.hogwarts.android.core.sync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.sync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sync_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sync_back)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sync frequency
            item {
                Text(stringResource(R.string.sync_frequency_heading), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            item {
                val options = listOf(
                    "Every 15 min" to stringResource(R.string.sync_frequency_15min),
                    "Every 30 min" to stringResource(R.string.sync_frequency_30min),
                    "Every hour" to stringResource(R.string.sync_frequency_hour),
                    "Manual only" to stringResource(R.string.sync_frequency_manual)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        options.forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                                RadioButton(
                                    selected = key == uiState.syncFrequency,
                                    onClick = { viewModel.setSyncFrequency(key) }
                                )
                            }
                        }
                    }
                }
            }

            // Network preference
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.sync_network_heading), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            item {
                SyncToggle(stringResource(R.string.sync_wifi_only), uiState.wifiOnly) { viewModel.setWifiOnly(it) }
            }

            // Entity toggles
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.sync_data_heading), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            item { SyncToggle(stringResource(R.string.sync_entity_attendance), uiState.syncAttendance) { viewModel.toggleEntity("attendance", it) } }
            item { SyncToggle(stringResource(R.string.sync_entity_grades), uiState.syncGrades) { viewModel.toggleEntity("grades", it) } }
            item { SyncToggle(stringResource(R.string.sync_entity_timetable), uiState.syncTimetable) { viewModel.toggleEntity("timetable", it) } }
            item { SyncToggle(stringResource(R.string.sync_entity_messages), uiState.syncMessages) { viewModel.toggleEntity("messages", it) } }
            item { SyncToggle(stringResource(R.string.sync_entity_notifications), uiState.syncNotifications) { viewModel.toggleEntity("notifications", it) } }
            item { SyncToggle(stringResource(R.string.sync_entity_fees), uiState.syncFees) { viewModel.toggleEntity("fees", it) } }
            item { SyncToggle(stringResource(R.string.sync_entity_library), uiState.syncLibrary) { viewModel.toggleEntity("library", it) } }

            // Data usage
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.sync_data_usage_heading), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.sync_total_cached), style = MaterialTheme.typography.bodyMedium)
                            Text(uiState.totalCacheSize, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.sync_last_sync), style = MaterialTheme.typography.bodyMedium)
                            Text(uiState.lastSyncTime, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Actions
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.syncNow() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sync, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.sync_now_button))
                }
            }
            item {
                OutlinedButton(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.sync_clear_cache))
                }
            }
        }
    }
}

@Composable
private fun SyncToggle(
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
