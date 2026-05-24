package org.hogwarts.android.feature.stream.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.HogwartsButton
import org.hogwarts.android.feature.stream.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonQuizScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: (String, String) -> Unit,
    viewModel: LessonQuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stream_quiz_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.stream_back))
                    }
                },
                actions = {
                    if (!uiState.isFinished) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (uiState.timeRemainingSeconds < 60) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = formatTime(uiState.timeRemainingSeconds),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (uiState.timeRemainingSeconds < 60) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.isFinished) {
            // Results screen
            QuizResultsContent(
                uiState = uiState,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            // Question screen
            QuizQuestionContent(
                uiState = uiState,
                onSelectAnswer = viewModel::selectAnswer,
                onConfirm = viewModel::confirmAnswer,
                onNext = viewModel::nextQuestion,
                onPrevious = viewModel::previousQuestion,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun QuizQuestionContent(
    uiState: LessonQuizUiState,
    onSelectAnswer: (Int) -> Unit,
    onConfirm: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = uiState.currentQuestion ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Progress
        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1).toFloat() / uiState.totalQuestions },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "Question ${uiState.currentIndex + 1} of ${uiState.totalQuestions}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question text
        Text(
            text = question.question,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Options
        question.options.forEachIndexed { index, option ->
            val isSelected = uiState.selectedAnswer == index
            val isCorrect = index == question.correctAnswer
            val showResult = uiState.showExplanation

            val borderColor = when {
                showResult && isCorrect -> MaterialTheme.colorScheme.primary
                showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }

            val containerColor = when {
                showResult && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }

            Card(
                onClick = { if (!showResult) onSelectAnswer(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${('A' + index)}.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (showResult) {
                        if (isCorrect) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.stream_correct_content_desc),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (isSelected) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = stringResource(R.string.stream_incorrect_content_desc),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Explanation
        if (uiState.showExplanation && question.explanation != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Explanation",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentIndex > 0) {
                HogwartsButton(
                    text = "Previous",
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f)
                )
            }
            if (!uiState.showExplanation) {
                HogwartsButton(
                    text = "Check Answer",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            } else {
                HogwartsButton(
                    text = if (uiState.isLastQuestion) "Finish Quiz" else "Next",
                    onClick = onNext,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuizResultsContent(
    uiState: LessonQuizUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Score circle
        val passed = uiState.scorePercentage >= 70
        Icon(
            if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (passed) "Congratulations!" else "Keep Trying!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${uiState.scorePercentage}%",
            style = MaterialTheme.typography.displaySmall,
            color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${uiState.score} out of ${uiState.totalQuestions} correct",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (passed) "You passed the quiz!" else "You need 70% to pass. Review the material and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        HogwartsButton(
            text = "Done",
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
