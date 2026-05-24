package org.hogwarts.android.feature.students.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.students.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.students.domain.model.StudentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: StudentDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val formatter = viewModel.localeFormatter

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.students_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.students_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        uiState.student?.let { onNavigateToEdit(it.id) }
                    }) {
                        Icon(HogwartsIcons.Edit, contentDescription = stringResource(R.string.students_detail_edit))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.student == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val student = uiState.student
            if (student == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.students_detail_not_found), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                AppleInsetGroupedList(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    state = listState
                ) {
                    // Avatar and name header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppleSpacing.Large),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                        ) {
                            UserAvatar(
                                name = student.fullName,
                                imageUrl = student.avatarUrl,
                                size = 80.dp
                            )
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            StatusBadge(
                                text = student.status.name,
                                color = when (student.status) {
                                    StudentStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                    StudentStatus.INACTIVE -> MaterialTheme.colorScheme.outline
                                    StudentStatus.SUSPENDED -> MaterialTheme.colorScheme.error
                                    StudentStatus.GRADUATED -> MaterialTheme.colorScheme.tertiary
                                    StudentStatus.TRANSFERRED -> MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }

                    // Personal Information
                    item {
                        AppleListSection(header = stringResource(R.string.students_detail_personal_info)) {
                            DetailRow(label = stringResource(R.string.students_detail_label_email), value = student.email ?: "\u2014")
                            DetailRow(label = stringResource(R.string.students_detail_label_phone), value = student.phone ?: "\u2014", showDivider = true)
                            DetailRow(
                                label = stringResource(R.string.students_detail_label_dob),
                                value = student.dateOfBirth?.let { formatter.formatDate(it) } ?: "\u2014",
                                showDivider = true
                            )
                            DetailRow(label = stringResource(R.string.students_detail_label_gender), value = student.gender ?: "\u2014", showDivider = true)
                        }
                    }

                    // Academic Information
                    item {
                        AppleListSection(header = stringResource(R.string.students_detail_academic_info)) {
                            DetailRow(label = stringResource(R.string.students_detail_label_enrollment), value = student.enrollmentNumber ?: "\u2014")
                            DetailRow(label = stringResource(R.string.students_detail_label_class), value = student.className ?: "\u2014", showDivider = true)
                            DetailRow(label = stringResource(R.string.students_detail_label_section), value = student.section ?: "\u2014", showDivider = true)
                        }
                    }

                    // Guardian Information
                    item {
                        AppleListSection(header = stringResource(R.string.students_detail_guardian_info)) {
                            DetailRow(label = stringResource(R.string.students_detail_label_name), value = student.guardianName ?: "\u2014")
                            DetailRow(label = stringResource(R.string.students_detail_label_phone), value = student.guardianPhone ?: "\u2014", showDivider = true)
                        }
                    }

                    if (uiState.error != null) {
                        item {
                            Text(
                                text = stringResource(R.string.students_cached_data, uiState.error ?: ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(AppleSpacing.Standard)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    showDivider: Boolean = false
) {
    AppleListRow(showDivider = showDivider) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
