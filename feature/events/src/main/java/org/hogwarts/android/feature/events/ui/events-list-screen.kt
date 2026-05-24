package org.hogwarts.android.feature.events.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.events.R
import org.hogwarts.android.feature.events.domain.model.Event
import org.hogwarts.android.feature.events.domain.model.EventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: EventsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.events_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.events_back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.events_calendar_content_desc))
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Type filter chips
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedType == null,
                            onClick = { viewModel.selectType(null) },
                            label = { Text(stringResource(R.string.events_filter_all)) }
                        )
                    }
                    items(EventType.entries.toList()) { type ->
                        FilterChip(
                            selected = uiState.selectedType == type,
                            onClick = { viewModel.selectType(type) },
                            label = { Text(type.displayName()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.filteredEvents.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = stringResource(R.string.events_no_events_title),
                        subtitle = stringResource(R.string.events_no_events_subtitle),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        // Upcoming section
                        val upcoming = uiState.filteredEvents.filter {
                            it.status == org.hogwarts.android.feature.events.domain.model.EventStatus.UPCOMING ||
                                it.status == org.hogwarts.android.feature.events.domain.model.EventStatus.ONGOING
                        }
                        if (upcoming.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.events_upcoming_section),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(upcoming, key = { it.id }) { event ->
                                EventCard(event = event, onClick = { onNavigateToEvent(event.id) })
                            }
                        }

                        // Past section
                        val past = uiState.filteredEvents.filter {
                            it.status == org.hogwarts.android.feature.events.domain.model.EventStatus.COMPLETED ||
                                it.status == org.hogwarts.android.feature.events.domain.model.EventStatus.CANCELLED
                        }
                        if (past.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.events_past_section),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(past, key = { it.id }) { event ->
                                EventCard(event = event, onClick = { onNavigateToEvent(event.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    text = event.type.displayName(),
                    color = event.type.color()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.startDate + (event.startTime?.let { " " + stringResource(R.string.events_at_time, it) } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            event.location?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (event.registrationRequired) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (event.isRegistered) stringResource(R.string.events_registered) else stringResource(R.string.events_attending_count, event.currentAttendees, event.maxAttendees?.toString() ?: "~"),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (event.isRegistered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EventType.displayName(): String = when (this) {
    EventType.ACADEMIC -> stringResource(R.string.events_type_academic)
    EventType.SPORTS -> stringResource(R.string.events_type_sports)
    EventType.CULTURAL -> stringResource(R.string.events_type_cultural)
    EventType.PARENT_MEETING -> stringResource(R.string.events_type_parent_meeting)
    EventType.HOLIDAY -> stringResource(R.string.events_type_holiday)
    EventType.EXAM_SCHEDULE -> stringResource(R.string.events_type_exam)
    EventType.OTHER -> stringResource(R.string.events_type_other)
}

@Composable
private fun EventType.color() = when (this) {
    EventType.ACADEMIC -> MaterialTheme.colorScheme.primary
    EventType.SPORTS -> MaterialTheme.colorScheme.tertiary
    EventType.CULTURAL -> MaterialTheme.colorScheme.secondary
    EventType.PARENT_MEETING -> MaterialTheme.colorScheme.inversePrimary
    EventType.HOLIDAY -> MaterialTheme.colorScheme.error
    EventType.EXAM_SCHEDULE -> MaterialTheme.colorScheme.onSurfaceVariant
    EventType.OTHER -> MaterialTheme.colorScheme.outline
}
