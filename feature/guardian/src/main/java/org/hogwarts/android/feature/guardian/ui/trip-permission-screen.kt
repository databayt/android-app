package org.hogwarts.android.feature.guardian.ui

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
import org.hogwarts.android.feature.guardian.R

data class TripPermission(
    val id: String,
    val tripName: String,
    val destination: String,
    val date: String,
    val childName: String,
    val description: String,
    val emergencyContact: String,
    val status: String // PENDING, GRANTED, DENIED
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPermissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: TripPermissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guardian_trip_title)) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.permissions) { permission ->
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
                            Text(permission.tripName, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            val (statusColor, statusText) = when (permission.status) {
                                "GRANTED" -> MaterialTheme.colorScheme.primary to stringResource(R.string.guardian_trip_status_granted)
                                "DENIED" -> MaterialTheme.colorScheme.error to stringResource(R.string.guardian_trip_status_denied)
                                else -> MaterialTheme.colorScheme.tertiary to stringResource(R.string.guardian_trip_status_pending)
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = statusColor.copy(alpha = 0.1f)
                            ) {
                                Text(statusText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall, color = statusColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(permission.childName, style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(permission.destination, style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(permission.date, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(permission.description, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (permission.status == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.denyPermission(permission.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text(stringResource(R.string.guardian_trip_deny)) }
                                Button(
                                    onClick = { viewModel.grantPermission(permission.id) },
                                    modifier = Modifier.weight(1f)
                                ) { Text(stringResource(R.string.guardian_trip_grant)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
