package org.hogwarts.android.feature.exams.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnlineExamScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String) -> Unit,
    viewModel: OnlineExamViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Prevent back navigation during exam
    BackHandler(enabled = !uiState.isSubmitted && uiState.exam != null) {
        // Block back navigation during active exam
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.exam?.title ?: stringResource(R.string.exams_online_default_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!uiState.isSubmitted && uiState.exam != null) {
                            Text(
                                text = stringResource(R.string.exams_online_question_status, uiState.currentQuestionIndex + 1, uiState.totalQuestions, uiState.answeredCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (!uiState.isSubmitted && uiState.exam != null) {
                        // Timer display
                        val timerColor by animateColorAsState(
                            targetValue = if (uiState.isTimerCritical) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            label = "timerColor"
                        )
                        Text(
                            text = uiState.timerFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = timerColor,
                            modifier = Modifier.padding(end = AppleSpacing.Standard)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(AppleSpacing.Standard))
                        Text(
                            text = stringResource(R.string.exams_online_preparing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.error != null && uiState.exam == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = HogwartsIcons.ErrorIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(AppleSpacing.Standard))
                        Text(
                            text = uiState.error ?: stringResource(R.string.exams_online_failed_load),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(AppleSpacing.Standard))
                        Button(onClick = onNavigateBack) {
                            Text(stringResource(R.string.exams_online_go_back))
                        }
                    }
                }
            }
            uiState.isSubmitted || uiState.isExamTerminated -> {
                ExamSubmittedContent(
                    isTerminated = uiState.isExamTerminated,
                    examId = uiState.exam?.examId ?: "",
                    answeredCount = uiState.answeredCount,
                    totalQuestions = uiState.totalQuestions,
                    onViewResults = { onNavigateToResults(uiState.exam?.examId ?: "") },
                    onDone = onNavigateBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            uiState.exam != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Violation banner
                    if (uiState.violationCount > 0) {
                        ViolationBanner(
                            count = uiState.violationCount,
                            max = uiState.maxViolations
                        )
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Question content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(AppleSpacing.Standard)
                    ) {
                        uiState.currentQuestion?.let { question ->
                            // Question number and marks
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.exams_online_question_number, question.questionNumber),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (question.marks > 1f) stringResource(R.string.exams_online_marks_plural, question.marks) else stringResource(R.string.exams_online_marks_singular, question.marks),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                            // Question text
                            Text(
                                text = question.question,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(AppleSpacing.Large))

                            // Answer input based on question type
                            val currentAnswer = uiState.answers[question.id]
                            when (question.type.uppercase()) {
                                "MCQ" -> {
                                    McqAnswerInput(
                                        options = question.options ?: emptyList(),
                                        selectedAnswer = currentAnswer,
                                        onAnswerSelected = { viewModel.selectAnswer(question.id, it) }
                                    )
                                }
                                "TRUE_FALSE" -> {
                                    TrueFalseAnswerInput(
                                        selectedAnswer = currentAnswer,
                                        onAnswerSelected = { viewModel.selectAnswer(question.id, it) }
                                    )
                                }
                                else -> {
                                    TextAnswerInput(
                                        currentAnswer = currentAnswer ?: "",
                                        onAnswerChanged = { viewModel.selectAnswer(question.id, it) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppleSpacing.Large))

                        // Question navigator
                        Text(
                            text = stringResource(R.string.exams_online_question_navigator),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(uiState.totalQuestions) { index ->
                                val question = uiState.questions.getOrNull(index)
                                val isAnswered = question?.let { uiState.answers[it.id] != null } ?: false
                                val isCurrent = index == uiState.currentQuestionIndex

                                QuestionNavChip(
                                    number = index + 1,
                                    isAnswered = isAnswered,
                                    isCurrent = isCurrent,
                                    onClick = { viewModel.jumpToQuestion(index) }
                                )
                            }
                        }
                    }

                    // Bottom navigation bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(AppleSpacing.Standard),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = viewModel::previousQuestion,
                            enabled = !uiState.isFirstQuestion
                        ) {
                            Text(stringResource(R.string.exams_online_previous))
                        }

                        if (uiState.isLastQuestion) {
                            Button(
                                onClick = viewModel::showSubmitConfirmation,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(R.string.exams_online_submit_exam))
                            }
                        } else {
                            Button(onClick = viewModel::nextQuestion) {
                                Text(stringResource(R.string.exams_online_next))
                            }
                        }
                    }
                }
            }
        }
    }

    // Submit confirmation dialog
    if (uiState.showSubmitDialog) {
        SubmitConfirmationDialog(
            answeredCount = uiState.answeredCount,
            totalQuestions = uiState.totalQuestions,
            onConfirm = viewModel::submitExam,
            onDismiss = viewModel::dismissSubmitDialog
        )
    }

    // Violation warning dialog
    if (uiState.showViolationWarning) {
        ViolationWarningDialog(
            violationCount = uiState.violationCount,
            maxViolations = uiState.maxViolations,
            violationType = uiState.lastViolationType,
            onDismiss = viewModel::dismissViolationWarning
        )
    }
}

@Composable
private fun McqAnswerInput(
    options: List<String>,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selectedAnswer == option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onAnswerSelected(option) },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppleSpacing.Standard),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
                ) {
                    RadioButton(selected = isSelected, onClick = null)
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrueFalseAnswerInput(
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
    ) {
        listOf(stringResource(R.string.exams_online_true) to "True", stringResource(R.string.exams_online_false) to "False").forEach { (label, option) ->
            val isSelected = selectedAnswer == option
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAnswerSelected(option) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppleSpacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Switch(
                        checked = option == "True",
                        onCheckedChange = null,
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextAnswerInput(
    currentAnswer: String,
    onAnswerChanged: (String) -> Unit
) {
    var text by remember(currentAnswer) { mutableStateOf(currentAnswer) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onAnswerChanged(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        label = { Text(stringResource(R.string.exams_online_your_answer)) },
        placeholder = { Text(stringResource(R.string.exams_online_answer_placeholder)) },
        maxLines = 8,
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun QuestionNavChip(
    number: Int,
    isAnswered: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isAnswered -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimary
        isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isCurrent) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun ViolationBanner(count: Int, max: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = AppleSpacing.Standard, vertical = AppleSpacing.Compact),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.exams_online_violations, count, max),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        if (count >= max - 1) {
            Text(
                text = stringResource(R.string.exams_online_final_warning),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExamSubmittedContent(
    isTerminated: Boolean,
    examId: String,
    answeredCount: Int,
    totalQuestions: Int,
    onViewResults: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AppleSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isTerminated) HogwartsIcons.ErrorIcon else HogwartsIcons.Checkmark,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = if (isTerminated) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )

        Spacer(modifier = Modifier.height(AppleSpacing.Large))

        Text(
            text = if (isTerminated) stringResource(R.string.exams_online_exam_terminated) else stringResource(R.string.exams_online_exam_submitted),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppleSpacing.Compact))

        Text(
            text = if (isTerminated) {
                stringResource(R.string.exams_online_terminated_reason)
            } else {
                stringResource(R.string.exams_online_answered_count, answeredCount, totalQuestions)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppleSpacing.ExtraLarge))

        Button(
            onClick = onViewResults,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.exams_online_view_results))
        }

        Spacer(modifier = Modifier.height(AppleSpacing.Small))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.exams_online_done))
        }
    }
}

@Composable
private fun SubmitConfirmationDialog(
    answeredCount: Int,
    totalQuestions: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val unanswered = totalQuestions - answeredCount
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exams_online_submit_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.exams_online_submit_answered, answeredCount, totalQuestions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unanswered > 0) {
                    Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                    Text(
                        text = if (unanswered > 1) stringResource(R.string.exams_online_submit_remaining_plural, unanswered) else stringResource(R.string.exams_online_submit_remaining_singular, unanswered),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                Text(
                    text = stringResource(R.string.exams_online_submit_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.exams_online_confirm_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.exams_online_continue_exam))
            }
        }
    )
}

@Composable
private fun ViolationWarningDialog(
    violationCount: Int,
    maxViolations: Int,
    violationType: org.hogwarts.android.feature.exams.domain.model.ViolationType?,
    onDismiss: () -> Unit
) {
    val violationName = when (violationType) {
        org.hogwarts.android.feature.exams.domain.model.ViolationType.APP_SWITCH -> stringResource(R.string.exams_online_violation_app_switch)
        org.hogwarts.android.feature.exams.domain.model.ViolationType.SCREENSHOT_ATTEMPT -> stringResource(R.string.exams_online_violation_screenshot)
        org.hogwarts.android.feature.exams.domain.model.ViolationType.MULTIPLE_FACE -> stringResource(R.string.exams_online_violation_multiple_face)
        org.hogwarts.android.feature.exams.domain.model.ViolationType.NO_FACE -> stringResource(R.string.exams_online_violation_no_face)
        org.hogwarts.android.feature.exams.domain.model.ViolationType.TAB_CHANGE -> stringResource(R.string.exams_online_violation_tab_change)
        null -> stringResource(R.string.exams_online_violation_generic)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.exams_online_warning_title, violationName),
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.exams_online_violation_count, violationCount, maxViolations),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                Text(
                    text = if (violationCount >= maxViolations) {
                        stringResource(R.string.exams_online_violation_max_reached)
                    } else {
                        stringResource(
                            R.string.exams_online_violation_remaining,
                            maxViolations - violationCount,
                            maxViolations
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.exams_online_i_understand))
            }
        }
    )
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExamSubmittedPreview() {
    HogwartsTheme {
        ExamSubmittedContent(
            isTerminated = false,
            examId = "exam-1",
            answeredCount = 18,
            totalQuestions = 20,
            onViewResults = {},
            onDone = {}
        )
    }
}
