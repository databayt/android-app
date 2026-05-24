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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.core.designsystem.atom.HogwartsTextField
import org.hogwarts.android.core.designsystem.atom.UserAvatar
import org.hogwarts.android.feature.teacher.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: GradeEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var assessmentDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.teacher_grade_entry_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.teacher_back))
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                // Summary footer
                if (uiState.studentGrades.isNotEmpty()) {
                    val entered = uiState.studentGrades.count { it.second.marks != null }
                    val total = uiState.studentGrades.size
                    val avg = uiState.studentGrades
                        .mapNotNull { it.second.marks }
                        .takeIf { it.isNotEmpty() }
                        ?.average()

                    Text(
                        text = avg?.let {
                            stringResource(
                                R.string.teacher_grade_entered_with_avg,
                                entered,
                                total,
                                String.format("%.1f", it)
                            )
                        } ?: stringResource(R.string.teacher_grade_entered_summary, entered, total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                HogwartsButton(
                    text = "Submit Grades",
                    onClick = { viewModel.submit(onNavigateBack) },
                    isLoading = uiState.isSubmitting,
                    enabled = uiState.selectedAssessmentId != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Assessment selector
            ExposedDropdownMenuBox(
                expanded = assessmentDropdownExpanded,
                onExpandedChange = { assessmentDropdownExpanded = it }
            ) {
                val selectedName = uiState.assessments.find { it.id == uiState.selectedAssessmentId }?.name ?: ""

                HogwartsTextField(
                    value = selectedName,
                    onValueChange = {},
                    label = stringResource(R.string.teacher_assessment_label),
                    placeholder = stringResource(R.string.teacher_assessment_placeholder),
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = assessmentDropdownExpanded,
                    onDismissRequest = { assessmentDropdownExpanded = false }
                ) {
                    uiState.assessments.forEach { assessment ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        R.string.teacher_assessment_item,
                                        assessment.name,
                                        assessment.maxMarks.toInt()
                                    )
                                )
                            },
                            onClick = {
                                viewModel.selectAssessment(assessment.id)
                                assessmentDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(uiState.studentGrades, key = { _, pair -> pair.first.id }) { _, (student, grade) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                        OutlinedTextField(
                            value = grade.marks?.toString() ?: "",
                            onValueChange = { value ->
                                val marks = value.toFloatOrNull()
                                viewModel.setGrade(student.id, marks)
                            },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.teacher_grade_placeholder)) },
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
