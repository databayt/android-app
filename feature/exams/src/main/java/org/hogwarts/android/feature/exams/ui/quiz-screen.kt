package org.hogwarts.android.feature.exams.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.feature.exams.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.quiz?.title ?: stringResource(R.string.exams_quiz_default_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.exams_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(AppleSpacing.Standard)
        ) {
            if (uiState.isSubmitted) {
                // Results
                QuizResults(
                    score = uiState.attempt?.score ?: 0,
                    totalMarks = uiState.attempt?.totalMarks ?: 0,
                    totalQuestions = uiState.totalQuestions,
                    onDone = onNavigateBack
                )
            } else {
                // Progress
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = stringResource(R.string.exams_quiz_question_of, uiState.currentQuestionIndex + 1, uiState.totalQuestions),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = AppleSpacing.Compact)
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                // Question
                uiState.currentQuestion?.let { question ->
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(AppleSpacing.Large))

                    // Options
                    question.options.forEachIndexed { index, option ->
                        val isSelected = uiState.selectedAnswer == index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .selectable(
                                    selected = isSelected,
                                    onClick = { viewModel.selectAnswer(question.id, index) },
                                    role = Role.RadioButton
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppleSpacing.Standard),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Small)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
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

                Spacer(modifier = Modifier.weight(1f))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = viewModel::previousQuestion,
                        enabled = !uiState.isFirstQuestion
                    ) {
                        Text(stringResource(R.string.exams_quiz_previous))
                    }

                    if (uiState.isLastQuestion) {
                        Button(
                            onClick = viewModel::submitQuiz,
                            enabled = uiState.attempt?.answers?.size == uiState.totalQuestions
                        ) {
                            Text(stringResource(R.string.exams_quiz_submit))
                        }
                    } else {
                        Button(onClick = viewModel::nextQuestion) {
                            Text(stringResource(R.string.exams_quiz_next))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResults(
    score: Int,
    totalMarks: Int,
    totalQuestions: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val percentage = if (totalMarks > 0) (score * 100) / totalMarks else 0
        val passed = percentage >= 50

        Icon(
            imageVector = if (passed) HogwartsIcons.Checkmark else HogwartsIcons.ErrorIcon,
            contentDescription = null,
            modifier = Modifier.padding(AppleSpacing.Standard),
            tint = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Text(
            text = if (passed) stringResource(R.string.exams_quiz_well_done) else stringResource(R.string.exams_quiz_keep_trying),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppleSpacing.Standard))

        Text(
            text = "$score / $totalMarks",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppleSpacing.Large))

        Button(onClick = onDone) {
            Text(stringResource(R.string.exams_quiz_done))
        }
    }
}
