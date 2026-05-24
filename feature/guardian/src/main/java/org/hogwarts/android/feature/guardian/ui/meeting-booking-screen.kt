package org.hogwarts.android.feature.guardian.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingBookingScreen(
    onNavigateBack: () -> Unit,
    viewModel: MeetingBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guardian_meeting_title)) },
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
            // Teacher selector
            item {
                Text(stringResource(R.string.guardian_meeting_select_teacher), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.teachers) { teacher ->
                        FilterChip(
                            selected = teacher.id == uiState.selectedTeacherId,
                            onClick = { viewModel.selectTeacher(teacher.id) },
                            label = { Text(teacher.name) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            // Date selector
            item {
                Text(stringResource(R.string.guardian_meeting_select_date), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.availableDates) { date ->
                        FilterChip(
                            selected = date == uiState.selectedDate,
                            onClick = { viewModel.selectDate(date) },
                            label = { Text(date) }
                        )
                    }
                }
            }

            // Time slots
            item {
                Text(stringResource(R.string.guardian_meeting_time_slots), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(uiState.timeSlots) { slot ->
                Surface(
                    onClick = { viewModel.selectTimeSlot(slot.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (slot.id == uiState.selectedTimeSlotId)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(slot.time, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(slot.duration, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (slot.isAvailable) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.primary)
                        } else {
                            Text(stringResource(R.string.guardian_meeting_booked), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Agenda field
            item {
                OutlinedTextField(
                    value = uiState.agenda,
                    onValueChange = { viewModel.updateAgenda(it) },
                    label = { Text(stringResource(R.string.guardian_meeting_agenda_label)) },
                    placeholder = { Text(stringResource(R.string.guardian_meeting_agenda_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            // Book button
            item {
                Button(
                    onClick = { viewModel.bookMeeting() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedTeacherId != null &&
                            uiState.selectedDate != null &&
                            uiState.selectedTimeSlotId != null
                ) {
                    Text(stringResource(R.string.guardian_meeting_book_button))
                }
            }
        }
    }
}
