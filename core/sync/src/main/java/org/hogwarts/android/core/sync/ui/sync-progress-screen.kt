package org.hogwarts.android.core.sync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class EntitySyncStatus(
    val name: String,
    val progress: Float,
    val lastSynced: String,
    val recordCount: Int,
    val status: String // SYNCED, SYNCING, ERROR, PENDING
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncProgressScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sync_progress_title)) },
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
            // Overall progress
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.sync_overall_heading), style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { uiState.overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            trackColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(uiState.overallProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.sync_last_sync_label, uiState.lastOverallSync),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Per-entity status
            item {
                Text(stringResource(R.string.sync_entity_status_heading), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            items(uiState.entities) { entity ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(entity.name, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            val (statusColor, statusIcon) = when (entity.status) {
                                "SYNCED" -> MaterialTheme.colorScheme.primary to Icons.Default.CheckCircle
                                "SYNCING" -> MaterialTheme.colorScheme.tertiary to Icons.Default.Sync
                                "ERROR" -> MaterialTheme.colorScheme.error to Icons.Default.Error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Default.Schedule
                            }
                            Icon(statusIcon, entity.status, tint = statusColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { entity.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.sync_record_count, entity.recordCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(entity.lastSynced,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Sync history
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.sync_history_heading), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            items(uiState.syncHistory) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entry.description, style = MaterialTheme.typography.bodySmall)
                    Text(entry.timestamp, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
