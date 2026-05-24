package org.hogwarts.android.feature.teacher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import org.hogwarts.android.feature.teacher.R
import org.hogwarts.android.core.designsystem.atom.HogwartsSearchBar
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.teacher.domain.model.ClassStudent

enum class StudentSortOption(val labelResId: Int) {
    NAME_AZ(R.string.teacher_sort_name_az),
    NAME_ZA(R.string.teacher_sort_name_za),
    ATTENDANCE(R.string.teacher_sort_attendance),
    GRADE(R.string.teacher_sort_grade)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRosterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStudent: (String) -> Unit,
    viewModel: StudentRosterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.teacher_student_roster_title, uiState.filteredStudents.size)) },
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
            HogwartsSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                placeholder = "Search by name or ID",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            // Sort options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StudentSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = uiState.sortOption == option,
                        onClick = { viewModel.setSortOption(option) },
                        label = { Text(stringResource(option.labelResId), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.filteredStudents, key = { it.id }) { student ->
                    StudentRosterRow(
                        student = student,
                        onClick = { onNavigateToStudent(student.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentRosterRow(
    student: ClassStudent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            name = student.displayName,
            imageUrl = student.avatarUrl,
            size = 40.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = student.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            student.studentNumber?.let {
                Text(
                    text = "ID: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Attendance badge
        val attendanceColor = when {
            student.attendanceRate >= 0.9f -> MaterialTheme.colorScheme.primary
            student.attendanceRate >= 0.75f -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        }
        Text(
            text = "${(student.attendanceRate * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = attendanceColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Latest grade
        student.latestGrade?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
