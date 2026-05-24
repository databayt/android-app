package org.hogwarts.android.feature.admin.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import org.hogwarts.android.feature.admin.R
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.feature.admin.domain.model.ClassRoster
import org.hogwarts.android.feature.admin.domain.model.RosterStudent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassRosterScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClassRosterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.selectedRoster?.let { "${it.className} - ${it.grade}${it.section}" } ?: stringResource(R.string.admin_class_rosters_title))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.selectedRoster != null) viewModel.clearSelection() else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.admin_back))
                    }
                },
                actions = {
                    if (uiState.selectedRoster != null) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.admin_add_student))
                        }
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
            if (uiState.selectedRoster != null) {
                RosterDetailContent(
                    roster = uiState.selectedRoster!!,
                    onRemoveStudent = viewModel::removeStudent
                )
            } else if (uiState.rosters.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = stringResource(R.string.admin_no_classes_title),
                    subtitle = stringResource(R.string.admin_no_classes_subtitle),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                RosterListContent(
                    rosters = uiState.rosters,
                    onSelectClass = viewModel::selectClass
                )
            }
        }

        if (showAddDialog) {
            AddStudentDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { studentId ->
                    viewModel.addStudent(studentId)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun RosterListContent(
    rosters: List<ClassRoster>,
    onSelectClass: (ClassRoster) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(rosters, key = { it.classId }) { roster ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectClass(roster) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = roster.className,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.admin_grade_section, roster.grade, roster.section),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(
                        text = stringResource(R.string.admin_students_badge, roster.students.size),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterDetailContent(
    roster: ClassRoster,
    onRemoveStudent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.admin_students_count, roster.students.size),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        items(roster.students, key = { it.id }) { student ->
            StudentRow(
                student = student,
                onRemove = { onRemoveStudent(student.id) }
            )
        }
        if (roster.students.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.People,
                    title = stringResource(R.string.admin_no_students_title),
                    subtitle = stringResource(R.string.admin_no_students_subtitle),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StudentRow(
    student: RosterStudent,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.admin_enrolled_prefix, student.enrollmentDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(
                text = student.status,
                color = if (student.status.equals("active", true))
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.PersonRemove,
                    contentDescription = stringResource(R.string.admin_remove_student),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var studentId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.admin_add_student_dialog_title)) },
        text = {
            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text(stringResource(R.string.admin_student_id_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(studentId) },
                enabled = studentId.isNotBlank()
            ) {
                Text(stringResource(R.string.admin_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.admin_cancel))
            }
        }
    )
}
