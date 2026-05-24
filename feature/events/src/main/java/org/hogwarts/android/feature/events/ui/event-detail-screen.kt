package org.hogwarts.android.feature.events.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.events.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.events_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.events_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        uiState.event?.let { event ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${event.title}\n${event.startDate}\n${event.location ?: ""}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.events_share_event)))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.events_share_content_desc))
                    }
                }
            )
        }
    ) { innerPadding ->
        uiState.event?.let { event ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status and type badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(
                        text = event.status.name,
                        color = when (event.status) {
                            org.hogwarts.android.feature.events.domain.model.EventStatus.UPCOMING -> MaterialTheme.colorScheme.primary
                            org.hogwarts.android.feature.events.domain.model.EventStatus.ONGOING -> MaterialTheme.colorScheme.tertiary
                            org.hogwarts.android.feature.events.domain.model.EventStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
                            org.hogwarts.android.feature.events.domain.model.EventStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Date
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = event.startDate + (event.endDate?.let { " - $it" } ?: ""),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Time
                        event.startTime?.let { time ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    text = time + (event.endTime?.let { " - $it" } ?: ""),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Location
                        event.location?.let { loc ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(text = loc, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // Organizer
                        event.organizerName?.let { org ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(text = org, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // Attendees
                        if (event.registrationRequired) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.events_attending_info, event.currentAttendees, event.maxAttendees?.toString() ?: "unlimited"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                if (event.description.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.events_about_section),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add to Calendar button
                HogwartsButton(
                    text = stringResource(R.string.events_add_to_calendar),
                    onClick = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, event.title)
                            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                            event.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
                            putExtra(CalendarContract.Events.ALL_DAY, event.isAllDay)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Register/Unregister button
                if (event.registrationRequired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (event.isRegistered) {
                        HogwartsButton(
                            text = stringResource(R.string.events_unregister),
                            onClick = { viewModel.unregister() },
                            isLoading = uiState.isRegistering,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        HogwartsButton(
                            text = stringResource(R.string.events_register),
                            onClick = { viewModel.register() },
                            isLoading = uiState.isRegistering,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
