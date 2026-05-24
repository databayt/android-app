package org.hogwarts.android.feature.students.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.students.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.students.domain.model.StudentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudentFormViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) stringResource(R.string.students_form_title_edit) else stringResource(R.string.students_form_title_new)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.students_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AppleInsetGroupedList(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                state = listState
            ) {
                // Personal Information
                item {
                    AppleListSection(header = stringResource(R.string.students_detail_personal_info)) {
                        Column(
                            modifier = Modifier.padding(AppleSpacing.Standard),
                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
                            ) {
                                OutlinedTextField(
                                    value = uiState.firstName,
                                    onValueChange = viewModel::onFirstNameChanged,
                                    label = { Text(stringResource(R.string.students_form_label_first_name)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = uiState.lastName,
                                    onValueChange = viewModel::onLastNameChanged,
                                    label = { Text(stringResource(R.string.students_form_label_last_name)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = viewModel::onEmailChanged,
                                label = { Text(stringResource(R.string.students_form_label_email)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.phone,
                                onValueChange = viewModel::onPhoneChanged,
                                label = { Text(stringResource(R.string.students_form_label_phone)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.gender,
                                onValueChange = viewModel::onGenderChanged,
                                label = { Text(stringResource(R.string.students_form_label_gender)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // Academic Information
                item {
                    AppleListSection(header = stringResource(R.string.students_detail_academic_info)) {
                        Column(
                            modifier = Modifier.padding(AppleSpacing.Standard),
                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
                        ) {
                            OutlinedTextField(
                                value = uiState.classId,
                                onValueChange = viewModel::onClassIdChanged,
                                label = { Text(stringResource(R.string.students_form_label_class_id)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.section,
                                onValueChange = viewModel::onSectionChanged,
                                label = { Text(stringResource(R.string.students_form_label_section)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            if (uiState.isEditMode) {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = uiState.status,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(R.string.students_form_label_status)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        StudentStatus.entries.forEach { status ->
                                            DropdownMenuItem(
                                                text = { Text(status.name) },
                                                onClick = {
                                                    viewModel.onStatusChanged(status.name)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Guardian Information
                item {
                    AppleListSection(header = stringResource(R.string.students_detail_guardian_info)) {
                        Column(
                            modifier = Modifier.padding(AppleSpacing.Standard),
                            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
                        ) {
                            OutlinedTextField(
                                value = uiState.guardianName,
                                onValueChange = viewModel::onGuardianNameChanged,
                                label = { Text(stringResource(R.string.students_form_label_guardian_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.guardianPhone,
                                onValueChange = viewModel::onGuardianPhoneChanged,
                                label = { Text(stringResource(R.string.students_form_label_guardian_phone)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // Save button
                item {
                    Button(
                        onClick = viewModel::onSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppleSpacing.Standard),
                        enabled = uiState.firstName.isNotBlank() && uiState.lastName.isNotBlank()
                    ) {
                        Text(if (uiState.isEditMode) stringResource(R.string.students_form_button_update) else stringResource(R.string.students_form_button_create))
                    }
                }

                if (uiState.error != null) {
                    item {
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(AppleSpacing.Standard)
                        )
                    }
                }
            }
        }
    }
}
