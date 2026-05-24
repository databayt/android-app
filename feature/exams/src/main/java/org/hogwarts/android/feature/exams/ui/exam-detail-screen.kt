package org.hogwarts.android.feature.exams.ui

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.domain.model.ExamResult
import org.hogwarts.android.feature.exams.domain.model.ExamStatus
import org.hogwarts.android.feature.exams.domain.model.ExamType
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExamDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exams_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.exams_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.exam == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.exam == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FormError(message = uiState.error ?: stringResource(R.string.exams_detail_failed_load))
                }
            }
            uiState.exam != null -> {
                ExamDetailContent(
                    exam = uiState.exam!!,
                    formatter = viewModel.localeFormatter,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ExamDetailContent(
    exam: Exam,
    formatter: LocaleFormatter,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    AppleInsetGroupedList(
        modifier = modifier,
        state = listState
    ) {
        // Exam Info Section
        item {
            AppleListSection(header = stringResource(R.string.exams_detail_section_info)) {
                AppleListRow(showDivider = true) {
                    DetailRow(label = stringResource(R.string.exams_detail_label_subject), value = exam.subjectName)
                }
                AppleListRow(showDivider = true) {
                    DetailRow(label = stringResource(R.string.exams_detail_label_type), value = exam.type.name.lowercase().replaceFirstChar { it.uppercase() })
                }
                AppleListRow(showDivider = true) {
                    DetailRow(label = stringResource(R.string.exams_detail_label_date), value = formatter.formatDateLong(exam.date))
                }
                AppleListRow(showDivider = true) {
                    DetailRow(
                        label = stringResource(R.string.exams_detail_label_time),
                        value = "${formatter.formatTime(exam.startTime)} - ${formatter.formatTime(exam.endTime)}"
                    )
                }
                AppleListRow(showDivider = true) {
                    DetailRow(
                        label = stringResource(R.string.exams_detail_label_duration),
                        value = stringResource(R.string.exams_detail_duration_minutes, ChronoUnit.MINUTES.between(exam.startTime, exam.endTime).toInt())
                    )
                }
                if (exam.venue != null) {
                    AppleListRow(showDivider = true) {
                        DetailRow(label = stringResource(R.string.exams_detail_label_venue), value = exam.venue)
                    }
                }
                AppleListRow(showDivider = false) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.exams_detail_label_status),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusBadge(
                            text = exam.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = when (exam.status) {
                                ExamStatus.UPCOMING -> MaterialTheme.colorScheme.primary
                                ExamStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                                ExamStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                                ExamStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }

        // Marks Section
        if (exam.maxMarks != null || exam.passingMarks != null) {
            item {
                AppleListSection(header = stringResource(R.string.exams_detail_section_marks)) {
                    if (exam.maxMarks != null) {
                        AppleListRow(showDivider = exam.passingMarks != null) {
                            DetailRow(label = stringResource(R.string.exams_detail_label_max_marks), value = exam.maxMarks.toString())
                        }
                    }
                    if (exam.passingMarks != null) {
                        AppleListRow(showDivider = false) {
                            DetailRow(label = stringResource(R.string.exams_detail_label_passing_marks), value = exam.passingMarks.toString())
                        }
                    }
                }
            }
        }

        // Instructions Section
        if (!exam.instructions.isNullOrBlank()) {
            item {
                AppleListSection(header = stringResource(R.string.exams_detail_section_instructions)) {
                    AppleListRow(showDivider = false) {
                        Text(
                            text = exam.instructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Result Section (if completed)
        if (exam.result != null) {
            item {
                AppleListSection(header = stringResource(R.string.exams_detail_section_result)) {
                    AppleListRow(showDivider = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.exams_detail_label_marks_obtained),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${exam.result.marksObtained}/${exam.maxMarks ?: "—"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (exam.result.isPassed) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                    if (exam.maxMarks != null && exam.maxMarks > 0) {
                        AppleListRow(showDivider = true) {
                            val percentage = (exam.result.marksObtained / exam.maxMarks * 100).toInt()
                            DetailRow(label = stringResource(R.string.exams_detail_label_percentage), value = "$percentage%")
                        }
                    }
                    if (exam.result.grade != null) {
                        AppleListRow(showDivider = true) {
                            DetailRow(label = stringResource(R.string.exams_detail_label_grade), value = exam.result.grade)
                        }
                    }
                    AppleListRow(showDivider = exam.result.remarks != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.exams_detail_label_result),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatusBadge(
                                text = if (exam.result.isPassed) stringResource(R.string.exams_detail_passed) else stringResource(R.string.exams_detail_failed),
                                color = if (exam.result.isPassed) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                    if (exam.result.remarks != null) {
                        AppleListRow(showDivider = false) {
                            DetailRow(label = stringResource(R.string.exams_detail_label_remarks), value = exam.result.remarks)
                        }
                    }
                }
            }
        }

        // Countdown for upcoming exams
        if (exam.status == ExamStatus.UPCOMING) {
            item {
                val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), exam.date)
                if (daysUntil >= 0) {
                    AppleListSection(header = stringResource(R.string.exams_detail_section_countdown)) {
                        AppleListRow(showDivider = false) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when {
                                        daysUntil == 0L -> stringResource(R.string.exams_detail_countdown_today)
                                        daysUntil == 1L -> stringResource(R.string.exams_detail_countdown_tomorrow)
                                        else -> stringResource(R.string.exams_detail_countdown_days, daysUntil.toInt())
                                    },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun ExamDetailPreview() {
    HogwartsTheme {
        ExamDetailContent(
            exam = Exam(
                id = "1",
                title = "Mathematics Final",
                subjectId = "math-101",
                subjectName = "Mathematics",
                date = LocalDate.now().plusDays(5),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(11, 0),
                venue = "Hall A",
                instructions = "Bring calculator and ruler. No mobile phones allowed.",
                type = ExamType.WRITTEN,
                status = ExamStatus.UPCOMING,
                maxMarks = 100,
                passingMarks = 40
            ),
            formatter = LocaleFormatter(LocalContext.current)
        )
    }
}

@Preview(name = "Completed Exam")
@Composable
private fun ExamDetailCompletedPreview() {
    HogwartsTheme {
        ExamDetailContent(
            exam = Exam(
                id = "2",
                title = "Science Midterm",
                subjectId = "sci-101",
                subjectName = "Science",
                date = LocalDate.now().minusDays(10),
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(12, 0),
                venue = "Room 201",
                type = ExamType.WRITTEN,
                status = ExamStatus.COMPLETED,
                maxMarks = 100,
                passingMarks = 40,
                result = ExamResult(
                    marksObtained = 85.0,
                    grade = "A",
                    remarks = "Excellent performance",
                    isPassed = true
                )
            ),
            formatter = LocaleFormatter(LocalContext.current)
        )
    }
}
