package org.hogwarts.android.feature.teacher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.teacher.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAttendance: (String) -> Unit,
    onNavigateToGrades: (String) -> Unit,
    onNavigateToStudentRoster: (String) -> Unit,
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.teacher_tab_students),
        stringResource(R.string.teacher_tab_attendance),
        stringResource(R.string.teacher_tab_grades)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.teacherClass?.displayName ?: stringResource(R.string.teacher_class_detail_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.teacher_back))
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
            // Class info header
            uiState.teacherClass?.let { tc ->
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = tc.subjectName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.teacher_students_count_label, tc.studentCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Students tab - show roster
                    StudentRosterContent(
                        students = uiState.students,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    // Attendance tab - navigate to batch attendance
                    uiState.teacherClass?.id?.let { classId ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            org.hogwarts.android.core.designsystem.atom.HogwartsButton(
                                text = stringResource(R.string.teacher_mark_attendance),
                                onClick = { onNavigateToAttendance(classId) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                2 -> {
                    // Grades tab - navigate to grade entry
                    uiState.teacherClass?.id?.let { classId ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            org.hogwarts.android.core.designsystem.atom.HogwartsButton(
                                text = stringResource(R.string.teacher_enter_grades),
                                onClick = { onNavigateToGrades(classId) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentRosterContent(
    students: List<org.hogwarts.android.feature.teacher.domain.model.ClassStudent>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (students.isEmpty() && !isLoading) {
        org.hogwarts.android.core.designsystem.atom.EmptyState(
            icon = Icons.Default.People,
            title = stringResource(R.string.teacher_no_students_title),
            subtitle = stringResource(R.string.teacher_no_students_subtitle),
            modifier = modifier
        )
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(students.size) { index ->
                val student = students[index]
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        org.hogwarts.android.core.designsystem.atom.UserAvatar(
                            name = student.displayName,
                            imageUrl = student.avatarUrl,
                            size = 40.dp
                        )
                        androidx.compose.foundation.layout.Spacer(
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            student.studentNumber?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        org.hogwarts.android.core.designsystem.atom.StatusBadge(
                            text = "${(student.attendanceRate * 100).toInt()}%",
                            color = when {
                                student.attendanceRate >= 0.9f -> MaterialTheme.colorScheme.primary
                                student.attendanceRate >= 0.75f -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
}

