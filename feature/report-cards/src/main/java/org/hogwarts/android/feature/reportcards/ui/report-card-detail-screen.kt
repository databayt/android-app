package org.hogwarts.android.feature.reportcards.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.reportcards.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.reportcards.domain.model.AttendanceSummary
import org.hogwarts.android.feature.reportcards.domain.model.ReportCard
import org.hogwarts.android.feature.reportcards.domain.model.ReportCardStatus
import org.hogwarts.android.feature.reportcards.domain.model.SubjectReport

/**
 * Report card detail screen.
 *
 * Shows complete report card with:
 * - Student info header
 * - Subject-wise marks/grade/percentage table
 * - GPA and overall percentage
 * - Attendance summary
 * - Overall remarks
 * - Download PDF and Share buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCardDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportCardDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for PDF download events
    LaunchedEffect(uiState.pdfMessage) {
        uiState.pdfMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearPdfMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_cards_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.report_cards_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.reportCard == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.reportCard == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FormError(message = uiState.error ?: stringResource(R.string.report_cards_detail_failed))
                }
            }
            uiState.reportCard != null -> {
                ReportCardDetailContent(
                    reportCard = uiState.reportCard!!,
                    isDownloading = uiState.isDownloadingPdf,
                    onDownloadPdf = { viewModel.downloadPdf() },
                    onShare = { viewModel.shareReportCard() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ReportCardDetailContent(
    reportCard: ReportCard,
    isDownloading: Boolean,
    onDownloadPdf: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    AppleInsetGroupedList(
        modifier = modifier,
        state = listState
    ) {
        // Student Info Section
        item {
            AppleListSection(header = stringResource(R.string.report_cards_student_info)) {
                AppleListRow(showDivider = true) {
                    DetailRow(label = stringResource(R.string.report_cards_student), value = reportCard.studentName)
                }
                AppleListRow(showDivider = true) {
                    DetailRow(label = stringResource(R.string.report_cards_term), value = reportCard.termName)
                }
                AppleListRow(showDivider = true) {
                    DetailRow(label = stringResource(R.string.report_cards_academic_year), value = reportCard.academicYear)
                }
                AppleListRow(showDivider = false) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.report_cards_status),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusBadge(
                            text = reportCard.status.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            color = when (reportCard.status) {
                                ReportCardStatus.PUBLISHED -> MaterialTheme.colorScheme.primary
                                ReportCardStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        // Overall Performance Section
        item {
            AppleListSection(header = stringResource(R.string.report_cards_overall_performance)) {
                if (reportCard.gpa != null) {
                    AppleListRow(showDivider = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.report_cards_gpa),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%.2f".format(reportCard.gpa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (reportCard.overallPercentage != null) {
                    AppleListRow(showDivider = true) {
                        DetailRow(
                            label = stringResource(R.string.report_cards_overall_percentage),
                            value = "${"%.1f".format(reportCard.overallPercentage)}%"
                        )
                    }
                }
                if (reportCard.rankDisplay != null) {
                    AppleListRow(showDivider = false) {
                        DetailRow(label = stringResource(R.string.report_cards_rank), value = reportCard.rankDisplay!!)
                    }
                }
            }
        }

        // Subject Reports Table
        if (reportCard.subjects.isNotEmpty()) {
            item {
                AppleListSection(header = stringResource(R.string.report_cards_subject_results)) {
                    // Table header
                    AppleListRow(showDivider = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.report_cards_col_subject),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.35f)
                            )
                            Text(
                                text = stringResource(R.string.report_cards_col_marks),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.25f)
                            )
                            Text(
                                text = stringResource(R.string.report_cards_col_grade),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.15f)
                            )
                            Text(
                                text = stringResource(R.string.report_cards_col_percent),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.15f)
                            )
                        }
                    }

                    // Subject rows
                    reportCard.subjects.forEachIndexed { index, subject ->
                        AppleListRow(
                            showDivider = index < reportCard.subjects.size - 1
                        ) {
                            SubjectResultRow(subject = subject)
                        }
                    }
                }
            }
        }

        // Subject details (teacher name, remarks) - expandable section
        if (reportCard.subjects.any { it.remarks != null }) {
            item {
                AppleListSection(header = stringResource(R.string.report_cards_subject_remarks)) {
                    reportCard.subjects
                        .filter { it.remarks != null }
                        .forEachIndexed { index, subject ->
                            val filtered = reportCard.subjects.filter { it.remarks != null }
                            AppleListRow(showDivider = index < filtered.size - 1) {
                                Column {
                                    Text(
                                        text = subject.subjectName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.report_cards_teacher_prefix, subject.teacherName),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (subject.remarks != null) {
                                        Text(
                                            text = subject.remarks,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                }
            }
        }

        // Attendance Summary
        if (reportCard.attendanceSummary != null) {
            item {
                AttendanceSummarySection(summary = reportCard.attendanceSummary!!)
            }
        }

        // Overall Remarks
        if (!reportCard.overallRemarks.isNullOrBlank()) {
            item {
                AppleListSection(header = stringResource(R.string.report_cards_overall_remarks)) {
                    AppleListRow(showDivider = false) {
                        Text(
                            text = reportCard.overallRemarks,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Action Buttons: Download PDF & Share
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppleSpacing.Standard,
                        vertical = AppleSpacing.Comfortable
                    ),
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
            ) {
                Button(
                    onClick = onDownloadPdf,
                    enabled = !isDownloading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = HogwartsIcons.Download,
                            contentDescription = null,
                            modifier = Modifier.padding(end = AppleSpacing.Compact)
                        )
                        Text(stringResource(R.string.report_cards_download_pdf))
                    }
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = HogwartsIcons.Share,
                        contentDescription = null,
                        modifier = Modifier.padding(end = AppleSpacing.Compact)
                    )
                    Text(stringResource(R.string.report_cards_share))
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(AppleSpacing.Comfortable))
        }
    }
}

/**
 * Single row in the subject results table.
 */
@Composable
private fun SubjectResultRow(
    subject: SubjectReport,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(0.35f)) {
            Text(
                text = subject.subjectName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subject.teacherName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${subject.marks.toInt()}/${subject.maxMarks.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.25f)
        )
        Text(
            text = subject.grade,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (subject.isPassed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.15f)
        )
        Text(
            text = "${"%.0f".format(subject.percentage)}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.15f)
        )
    }
}

/**
 * Attendance summary section with progress indicator.
 */
@Composable
private fun AttendanceSummarySection(
    summary: AttendanceSummary,
    modifier: Modifier = Modifier
) {
    AppleListSection(header = stringResource(R.string.report_cards_attendance_summary)) {
        // Attendance percentage with progress bar
        AppleListRow(showDivider = true) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.report_cards_attendance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.1f".format(summary.attendancePercentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            summary.attendancePercentage >= 90 -> MaterialTheme.colorScheme.primary
                            summary.attendancePercentage >= 75 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                LinearProgressIndicator(
                    progress = { (summary.attendancePercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        summary.attendancePercentage >= 90 -> MaterialTheme.colorScheme.primary
                        summary.attendancePercentage >= 75 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        // Day breakdown
        AppleListRow(showDivider = true) {
            DetailRow(label = stringResource(R.string.report_cards_total_days), value = summary.totalDays.toString())
        }
        AppleListRow(showDivider = true) {
            DetailRow(label = stringResource(R.string.report_cards_present), value = summary.presentDays.toString())
        }
        AppleListRow(showDivider = true) {
            DetailRow(label = stringResource(R.string.report_cards_absent), value = summary.absentDays.toString())
        }
        AppleListRow(showDivider = false) {
            DetailRow(label = stringResource(R.string.report_cards_late), value = summary.lateDays.toString())
        }
    }
}

/**
 * Reusable key-value detail row.
 */
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
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

// --- Previews ---

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun ReportCardDetailPreview() {
    HogwartsTheme {
        ReportCardDetailContent(
            reportCard = ReportCard(
                id = "1",
                studentId = "s1",
                studentName = "Harry Potter",
                termId = "t1",
                termName = "Term 1",
                academicYear = "2025-2026",
                gpa = 3.85f,
                rank = 3,
                totalStudents = 45,
                status = ReportCardStatus.PUBLISHED,
                subjects = listOf(
                    SubjectReport(
                        subjectId = "math",
                        subjectName = "Mathematics",
                        teacherName = "Prof. Vector",
                        marks = 92f,
                        maxMarks = 100f,
                        grade = "A+",
                        percentage = 92f,
                        remarks = "Excellent work in Arithmancy concepts"
                    ),
                    SubjectReport(
                        subjectId = "sci",
                        subjectName = "Potions",
                        teacherName = "Prof. Snape",
                        marks = 78f,
                        maxMarks = 100f,
                        grade = "B+",
                        percentage = 78f,
                        remarks = null
                    ),
                    SubjectReport(
                        subjectId = "eng",
                        subjectName = "Defence Against Dark Arts",
                        teacherName = "Prof. Lupin",
                        marks = 95f,
                        maxMarks = 100f,
                        grade = "A+",
                        percentage = 95f,
                        remarks = "Outstanding Patronus charm"
                    ),
                    SubjectReport(
                        subjectId = "hist",
                        subjectName = "History of Magic",
                        teacherName = "Prof. Binns",
                        marks = 65f,
                        maxMarks = 100f,
                        grade = "C+",
                        percentage = 65f,
                        remarks = null
                    )
                ),
                overallRemarks = "Harry has shown remarkable improvement this term. " +
                        "His practical skills continue to excel, particularly in Defence Against Dark Arts.",
                attendanceSummary = AttendanceSummary(
                    totalDays = 90,
                    presentDays = 85,
                    absentDays = 3,
                    lateDays = 2
                )
            ),
            isDownloading = false,
            onDownloadPdf = {},
            onShare = {}
        )
    }
}
