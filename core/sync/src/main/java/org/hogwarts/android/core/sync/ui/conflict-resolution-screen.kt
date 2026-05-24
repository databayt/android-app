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

data class SyncConflict(
    val id: String,
    val entityType: String,
    val entityId: String,
    val localValue: String,
    val serverValue: String,
    val fieldName: String,
    val localTimestamp: String,
    val serverTimestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictResolutionScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConflictResolutionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sync_conflicts_title, uiState.conflicts.size)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sync_back)
                        )
                    }
                },
                actions = {
                    if (uiState.conflicts.isNotEmpty()) {
                        TextButton(onClick = { viewModel.resolveAllWithServer() }) {
                            Text(stringResource(R.string.sync_conflict_accept_all_server))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.conflicts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.sync_conflict_none_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.sync_conflict_none_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.conflicts) { conflict ->
                    ConflictCard(
                        conflict = conflict,
                        onKeepLocal = { viewModel.resolveWithLocal(conflict.id) },
                        onKeepServer = { viewModel.resolveWithServer(conflict.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConflictCard(
    conflict: SyncConflict,
    onKeepLocal: () -> Unit,
    onKeepServer: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${conflict.entityType} - ${conflict.fieldName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Side by side comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Local
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.sync_conflict_local_label), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(conflict.localValue, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(conflict.localTimestamp, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Server
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.sync_conflict_server_label), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(conflict.serverValue, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(conflict.serverTimestamp, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onKeepLocal,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.sync_conflict_keep_local)) }
                Button(
                    onClick = onKeepServer,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.sync_conflict_keep_server)) }
            }
        }
    }
}
