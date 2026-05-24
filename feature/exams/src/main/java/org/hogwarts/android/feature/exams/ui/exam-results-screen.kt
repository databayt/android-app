package org.hogwarts.android.feature.exams.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.FormError
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.data.remote.dto.ExamAnswerDto
import org.hogwarts.android.feature.exams.domain.model.DetailedExamResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCertificate: (String) -> Unit,
    viewModel: ExamResultsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exams_results_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.exams_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.result == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    FormError(message = uiState.error ?: "Failed to load results")
                }
            }
            uiState.result != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Tabs
                    TabRow(
                        selectedTabIndex = uiState.selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        ResultTab.entries.forEach { tab ->
                            Tab(
                                selected = uiState.selectedTab == tab,
                                onClick = { viewModel.onTabSelected(tab) },
                                text = {
                                    Text(
                                        text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }

                    // Tab content
                    when (uiState.selectedTab) {
                        ResultTab.SUMMARY -> ResultSummaryTab(
                            result = uiState.result!!,
                            onViewCertificate = { onNavigateToCertificate(uiState.result!!.examId) }
                        )
                        ResultTab.ANSWERS -> AnswerReviewTab(
                            answerDetails = uiState.answerDetails
                        )
                        ResultTab.DISTRIBUTION -> DistributionTab(
                            result = uiState.result!!,
                            answerDetails = uiState.answerDetails
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSummaryTab(
    result: DetailedExamResult,
    onViewCertificate: () -> Unit
) {
    val listState = rememberLazyListState()
    val isPassed = result.percentage >= 50f

    AppleInsetGroupedList(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        // Score highlight
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppleSpacing.Compact),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPassed) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppleSpacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isPassed) HogwartsIcons.Checkmark else HogwartsIcons.ErrorIcon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isPassed) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Spacer(modifier = Modifier.height(AppleSpacing.Small))
                    Text(
                        text = "${result.obtainedMarks.toInt()} / ${result.totalMarks.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPassed) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = "${result.percentage.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isPassed) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                    Text(
                        text = if (isPassed) "Passed" else "Not Passed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPassed) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
        }

        // Details
        item {
            AppleListSection(header = "Details") {
                AppleListRow(showDivider = true) {
                    ResultDetailRow(label = "Grade", value = result.grade)
                }
                if (result.rank != null) {
                    AppleListRow(showDivider = true) {
                        ResultDetailRow(
                            label = "Rank",
                            value = "${result.rank}${getRankSuffix(result.rank)}" +
                                if (result.totalStudents != null) " of ${result.totalStudents}" else ""
                        )
                    }
                }
                if (result.totalStudents != null && result.rank != null) {
                    val percentile = ((result.totalStudents - result.rank).toFloat() / result.totalStudents * 100).toInt()
                    AppleListRow(showDivider = true) {
                        ResultDetailRow(label = "Percentile", value = "${percentile}th")
                    }
                }
                AppleListRow(showDivider = true) {
                    ResultDetailRow(label = "Total Marks", value = result.totalMarks.toInt().toString())
                }
                AppleListRow(showDivider = true) {
                    ResultDetailRow(label = "Obtained Marks", value = result.obtainedMarks.toInt().toString())
                }
                AppleListRow(showDivider = false) {
                    ResultDetailRow(
                        label = "Correct Answers",
                        value = "${result.answers.count { it.isCorrect == true }} / ${result.answers.size}"
                    )
                }
            }
        }

        // Certificate link
        if (isPassed) {
            item {
                AppleListSection(header = "Certificate") {
                    AppleListRow(
                        showDivider = false,
                        onClick = onViewCertificate
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View Certificate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = HogwartsIcons.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerReviewTab(answerDetails: List<ExamAnswerDto>) {
    val listState = rememberLazyListState()

    AppleInsetGroupedList(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        if (answerDetails.isEmpty()) {
            item {
                Text(
                    text = "No answer details available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(AppleSpacing.Standard),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            item {
                AppleListSection(header = "Answer Review") {
                    answerDetails.forEachIndexed { index, answer ->
                        AppleListRow(showDivider = index < answerDetails.size - 1) {
                            AnswerReviewItem(
                                questionNumber = index + 1,
                                answer = answer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerReviewItem(
    questionNumber: Int,
    answer: ExamAnswerDto
) {
    val isCorrect = answer.isCorrect ?: false

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
    ) {
        // Question
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Q$questionNumber. ${answer.questionText ?: "Question #$questionNumber"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (answer.marksObtained != null) {
                Text(
                    text = "${answer.marksObtained}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        // Student answer
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCorrect) HogwartsIcons.Checkmark else HogwartsIcons.ErrorIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isCorrect) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Text(
                text = "Your answer: ${answer.studentAnswer ?: "Not answered"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isCorrect) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        // Correct answer (if wrong)
        if (!isCorrect && answer.correctAnswer != null) {
            Text(
                text = "Correct: ${answer.correctAnswer}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Explanation
        if (answer.explanation != null) {
            Text(
                text = answer.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DistributionTab(
    result: DetailedExamResult,
    answerDetails: List<ExamAnswerDto>
) {
    val listState = rememberLazyListState()
    val correctCount = answerDetails.count { it.isCorrect == true }
    val incorrectCount = answerDetails.count { it.isCorrect == false }
    val unansweredCount = answerDetails.count { it.studentAnswer == null }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    AppleInsetGroupedList(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        // Bar chart
        item {
            AppleListSection(header = "Score Distribution") {
                AppleListRow(showDivider = false) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppleSpacing.Standard)
                    ) {
                        // Canvas bar chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            val barWidth = size.width / 5
                            val maxValue = maxOf(correctCount, incorrectCount, unansweredCount, 1).toFloat()
                            val chartHeight = size.height - 40f

                            // Correct bar
                            val correctBarHeight = (correctCount / maxValue) * chartHeight
                            drawRect(
                                color = primaryColor,
                                topLeft = Offset(barWidth * 0.5f, chartHeight - correctBarHeight),
                                size = Size(barWidth, correctBarHeight)
                            )

                            // Incorrect bar
                            val incorrectBarHeight = (incorrectCount / maxValue) * chartHeight
                            drawRect(
                                color = errorColor,
                                topLeft = Offset(barWidth * 2f, chartHeight - incorrectBarHeight),
                                size = Size(barWidth, incorrectBarHeight)
                            )

                            // Unanswered bar
                            val unansweredBarHeight = (unansweredCount / maxValue) * chartHeight
                            drawRect(
                                color = surfaceVariantColor,
                                topLeft = Offset(barWidth * 3.5f, chartHeight - unansweredBarHeight),
                                size = Size(barWidth, unansweredBarHeight)
                            )
                        }

                        Spacer(modifier = Modifier.height(AppleSpacing.Compact))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ChartLegendItem(
                                color = primaryColor,
                                label = "Correct ($correctCount)"
                            )
                            ChartLegendItem(
                                color = errorColor,
                                label = "Incorrect ($incorrectCount)"
                            )
                            ChartLegendItem(
                                color = surfaceVariantColor,
                                label = "Unanswered ($unansweredCount)"
                            )
                        }
                    }
                }
            }
        }

        // Performance breakdown
        item {
            AppleListSection(header = "Performance") {
                AppleListRow(showDivider = true) {
                    ResultDetailRow(
                        label = "Accuracy",
                        value = if (answerDetails.isNotEmpty()) {
                            "${(correctCount * 100 / answerDetails.size)}%"
                        } else "N/A"
                    )
                }
                AppleListRow(showDivider = true) {
                    ResultDetailRow(
                        label = "Attempted",
                        value = "${answerDetails.size - unansweredCount} / ${answerDetails.size}"
                    )
                }
                AppleListRow(showDivider = false) {
                    ResultDetailRow(
                        label = "Average Score",
                        value = if (answerDetails.isNotEmpty()) {
                            val avgMarks = answerDetails.mapNotNull { it.marksObtained }.average()
                            String.format("%.1f", avgMarks)
                        } else "N/A"
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawRect(color = color, size = size)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getRankSuffix(rank: Int): String {
    return when {
        rank % 100 in 11..13 -> "th"
        rank % 10 == 1 -> "st"
        rank % 10 == 2 -> "nd"
        rank % 10 == 3 -> "rd"
        else -> "th"
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AnswerReviewItemPreview() {
    HogwartsTheme {
        AnswerReviewItem(
            questionNumber = 1,
            answer = ExamAnswerDto(
                id = "1",
                examId = "exam-1",
                questionId = "q1",
                studentAnswer = "Paris",
                isCorrect = true,
                marksObtained = 1f,
                questionText = "What is the capital of France?",
                correctAnswer = "Paris",
                explanation = "Paris has been the capital of France since the medieval era."
            )
        )
    }
}
