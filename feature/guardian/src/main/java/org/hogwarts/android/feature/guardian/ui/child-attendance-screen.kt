package org.hogwarts.android.feature.guardian.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.ChildSelector
import org.hogwarts.android.core.designsystem.atom.ChildSelectorItem
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.guardian.R
import org.hogwarts.android.feature.guardian.data.repository.ChildAttendanceRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildAttendanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChildAttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guardian_attendance_title)) },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.children.size > 1) {
                ChildSelector(
                    children = uiState.children.map { ChildSelectorItem(it.id, it.displayName, it.avatarUrl) },
                    selectedChildId = uiState.selectedChildId,
                    onChildSelected = viewModel::selectChild
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "All" to stringResource(R.string.guardian_attendance_filter_all),
                    "Present" to stringResource(R.string.guardian_attendance_filter_present),
                    "Absent" to stringResource(R.string.guardian_attendance_filter_absent),
                    "Late" to stringResource(R.string.guardian_attendance_filter_late),
                    "Excused" to stringResource(R.string.guardian_attendance_filter_excused)
                )
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.statusFilter == key,
                        onClick = { viewModel.setStatusFilter(key) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attendance summary card
            AttendanceSummaryCard(
                presentCount = uiState.presentCount,
                absentCount = uiState.absentCount,
                lateCount = uiState.lateCount,
                excusedCount = uiState.excusedCount,
                attendanceRate = uiState.attendanceRate
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Attendance records list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(uiState.filteredRecords, key = { it.id }) { record ->
                    AttendanceRecordRow(record = record)
                }
            }
        }
    }
}

@Composable
private fun AttendanceSummaryCard(
    presentCount: Int,
    absentCount: Int,
    lateCount: Int,
    excusedCount: Int,
    attendanceRate: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(
                    R.string.guardian_attendance_rate,
                    (attendanceRate * 100).toInt()
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$presentCount", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.guardian_attendance_present), style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$absentCount", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.guardian_attendance_absent), style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$lateCount", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                    Text(stringResource(R.string.guardian_attendance_late), style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$excusedCount", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                    Text(stringResource(R.string.guardian_attendance_excused), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AttendanceRecordRow(
    record: ChildAttendanceRecord,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                record.subject?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val localizedStatus = when (record.status.lowercase()) {
                "present" -> stringResource(R.string.guardian_attendance_present)
                "absent" -> stringResource(R.string.guardian_attendance_absent)
                "late" -> stringResource(R.string.guardian_attendance_late)
                "excused" -> stringResource(R.string.guardian_attendance_excused)
                else -> record.status
            }
            StatusBadge(
                text = localizedStatus,
                color = when (record.status.lowercase()) {
                    "present" -> MaterialTheme.colorScheme.primary
                    "absent" -> MaterialTheme.colorScheme.error
                    "late" -> MaterialTheme.colorScheme.tertiary
                    "excused" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}
