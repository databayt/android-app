package org.hogwarts.android.feature.lumos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.lumos.R
import org.hogwarts.android.feature.lumos.domain.model.QuizQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonQuizScreen(
    onNavigateBack: () -> Unit,
    onQuizCompleted: () -> Unit = onNavigateBack,
    viewModel: LessonQuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.lumos_quiz_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.lumos_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.isFinished) {
            QuizResultView(
                score = uiState.score,
                total = uiState.questions.size,
                isPassed = uiState.isPassed,
                onDone = onQuizCompleted,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else if (uiState.questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "لا توجد أسئلة متاحة لهذا الدرس حالياً")
            }
        } else {
            val currentQuestion = uiState.questions[uiState.currentIndex]
            val selectedOption = uiState.selectedAnswers[currentQuestion.id]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Progress Bar & Question Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.lumos_question_of,
                            uiState.currentIndex + 1,
                            uiState.questions.size
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (uiState.currentIndex + 1).toFloat() / uiState.questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Question Text
                Text(
                    text = currentQuestion.question,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Options List
                currentQuestion.options.forEachIndexed { index, optionText ->
                    val isSelected = selectedOption == index
                    val isCorrect = currentQuestion.correctAnswer == index
                    val showResult = uiState.isAnswerChecked

                    val borderColor = when {
                        showResult && isCorrect -> Color(0xFF2E7D32)
                        showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }

                    val containerColor = when {
                        showResult && isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                        showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                            .clickable(enabled = !uiState.isAnswerChecked) {
                                viewModel.selectOption(currentQuestion.id, index)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(borderColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (showResult && isCorrect) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                } else if (showResult && isSelected && !isCorrect) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = ('A' + index).toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Explanation
                if (uiState.isAnswerChecked && !currentQuestion.explanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "💡 " + stringResource(R.string.lumos_explanation),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(32.dp))

                // Action Bottom Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.currentIndex > 0) {
                        OutlinedButton(
                            onClick = viewModel::previousQuestion,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.lumos_previous))
                        }
                    }

                    if (!uiState.isAnswerChecked) {
                        Button(
                            onClick = viewModel::checkAnswer,
                            enabled = selectedOption != null,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.lumos_check_answer))
                        }
                    } else {
                        Button(
                            onClick = viewModel::nextQuestion,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (uiState.currentIndex == uiState.questions.lastIndex)
                                    stringResource(R.string.lumos_finish_quiz)
                                else
                                    stringResource(R.string.lumos_next)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultView(
    score: Int,
    total: Int,
    isPassed: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) (score * 100) / total else 0

    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isPassed) "🏆" else "📚",
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPassed) stringResource(R.string.lumos_congratulations) else stringResource(R.string.lumos_keep_trying),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.lumos_score_out_of, score, total, percentage),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isPassed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isPassed) stringResource(R.string.lumos_you_passed) else stringResource(R.string.lumos_you_need_to_pass),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.lumos_done),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
