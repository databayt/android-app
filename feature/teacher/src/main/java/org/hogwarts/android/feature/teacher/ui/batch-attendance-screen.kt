package org.hogwarts.android.feature.teacher.ui

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.teacher.R
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.teacher.domain.model.AttendanceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchAttendanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: BatchAttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.teacher_attendance_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.teacher_back))
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllPresent() }) {
                        Text(stringResource(R.string.teacher_all_present))
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                HogwartsButton(
                    text = stringResource(R.string.teacher_submit_attendance),
                    onClick = { showConfirmDialog = true },
                    isLoading = uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(uiState.studentMarks, key = { _, pair -> pair.first.id }) { _, (student, mark) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        name = student.displayName,
                        imageUrl = student.avatarUrl,
                        size = 36.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = student.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AttendanceStatus.entries.forEach { status ->
                            FilterChip(
                                selected = mark.status == status,
                                onClick = { viewModel.setMark(student.id, status) },
                                label = {
                                    Text(
                                        text = when (status) {
                                            AttendanceStatus.PRESENT -> stringResource(R.string.teacher_attendance_present)
                                            AttendanceStatus.ABSENT -> stringResource(R.string.teacher_attendance_absent)
                                            AttendanceStatus.LATE -> stringResource(R.string.teacher_attendance_late)
                                            AttendanceStatus.EXCUSED -> stringResource(R.string.teacher_attendance_excused)
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (status) {
                                        AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.primary
                                        AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
                                        AttendanceStatus.LATE -> MaterialTheme.colorScheme.tertiary
                                        AttendanceStatus.EXCUSED -> MaterialTheme.colorScheme.secondary
                                    },
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        val presentCount = uiState.studentMarks.count { it.second.status == AttendanceStatus.PRESENT }
        val absentCount = uiState.studentMarks.count { it.second.status == AttendanceStatus.ABSENT }
        val lateCount = uiState.studentMarks.count { it.second.status == AttendanceStatus.LATE }
        val excusedCount = uiState.studentMarks.count { it.second.status == AttendanceStatus.EXCUSED }

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.teacher_confirm_attendance_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.teacher_attendance_present_count, presentCount))
                    Text(stringResource(R.string.teacher_attendance_absent_count, absentCount))
                    Text(stringResource(R.string.teacher_attendance_late_count, lateCount))
                    Text(stringResource(R.string.teacher_attendance_excused_count, excusedCount))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.teacher_attendance_total, uiState.studentMarks.size))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.submit(onNavigateBack)
                }) {
                    Text(stringResource(R.string.teacher_submit))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.teacher_cancel))
                }
            }
        )
    }
}
