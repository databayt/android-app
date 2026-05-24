package org.hogwarts.android.feature.exams.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.atom.StatusBadge
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.domain.model.Difficulty
import org.hogwarts.android.feature.exams.domain.model.QuestionBankItem
import org.hogwarts.android.feature.exams.domain.model.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuestionBankViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.practiceMode) stringResource(R.string.exams_qbank_practice_mode)
                        else stringResource(R.string.exams_qbank_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.practiceMode) {
                            viewModel.togglePracticeMode()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.exams_back))
                    }
                },
                actions = {
                    if (!uiState.practiceMode && uiState.filteredQuestions.isNotEmpty()) {
                        IconButton(onClick = viewModel::togglePracticeMode) {
                            Icon(HogwartsIcons.Exams, contentDescription = stringResource(R.string.exams_qbank_practice_cd))
                        }
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
            uiState.practiceMode -> {
                PracticeModeContent(
                    uiState = uiState,
                    onSelectAnswer = viewModel::selectPracticeAnswer,
                    onRevealExplanation = viewModel::revealExplanation,
                    onNext = viewModel::nextPracticeQuestion,
                    onPrevious = viewModel::previousPracticeQuestion,
                    onToggleBookmark = viewModel::toggleBookmark,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            else -> {
                QuestionBankBrowseContent(
                    uiState = uiState,
                    onSubjectFilter = viewModel::onSubjectFilterChanged,
                    onDifficultyFilter = viewModel::onDifficultyFilterChanged,
                    onBookmarkedToggle = viewModel::onBookmarkedOnlyToggled,
                    onGenerateMore = viewModel::generateMoreQuestions,
                    onToggleBookmark = viewModel::toggleBookmark,
                    onStartPractice = viewModel::togglePracticeMode,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun QuestionBankBrowseContent(
    uiState: QuestionBankUiState,
    onSubjectFilter: (String?) -> Unit,
    onDifficultyFilter: (Difficulty?) -> Unit,
    onBookmarkedToggle: () -> Unit,
    onGenerateMore: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onStartPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    AppleInsetGroupedList(
        modifier = modifier,
        state = listState
    ) {
        // Filters
        item {
            Column(modifier = Modifier.padding(vertical = AppleSpacing.Compact)) {
                // Subject filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                ) {
                    FilterChip(
                        selected = uiState.selectedSubject == null,
                        onClick = { onSubjectFilter(null) },
                        label = { Text(stringResource(R.string.exams_filter_all), style = MaterialTheme.typography.labelSmall) }
                    )
                    uiState.availableSubjects.forEach { subject ->
                        FilterChip(
                            selected = uiState.selectedSubject == subject,
                            onClick = { onSubjectFilter(subject) },
                            label = { Text(subject, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppleSpacing.Tiny))

                // Difficulty filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                ) {
                    FilterChip(
                        selected = uiState.selectedDifficulty == null,
                        onClick = { onDifficultyFilter(null) },
                        label = { Text(stringResource(R.string.exams_filter_all_levels), style = MaterialTheme.typography.labelSmall) }
                    )
                    Difficulty.entries.forEach { difficulty ->
                        FilterChip(
                            selected = uiState.selectedDifficulty == difficulty,
                            onClick = { onDifficultyFilter(difficulty) },
                            label = {
                                Text(
                                    difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppleSpacing.Tiny))

                // Bookmarked filter
                FilterChip(
                    selected = uiState.showBookmarkedOnly,
                    onClick = onBookmarkedToggle,
                    label = { Text(stringResource(R.string.exams_filter_bookmarked_only), style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Question count and actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppleSpacing.Compact),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredQuestions.size} questions",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)) {
                    if (uiState.filteredQuestions.isNotEmpty()) {
                        OutlinedButton(onClick = onStartPractice) {
                            Text(stringResource(R.string.exams_action_practice), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Button(
                        onClick = onGenerateMore,
                        enabled = !uiState.isGenerating
                    ) {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.exams_action_generate_more), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Questions list
        if (uiState.filteredQuestions.isEmpty()) {
            item {
                EmptyState(
                    icon = HogwartsIcons.Exams,
                    title = "No Questions Found",
                    subtitle = "Try adjusting your filters or generate more questions."
                )
            }
        } else {
            item {
                AppleListSection(header = "Questions") {
                    uiState.filteredQuestions.forEachIndexed { index, question ->
                        AppleListRow(showDivider = index < uiState.filteredQuestions.size - 1) {
                            QuestionBankItemRow(
                                item = question,
                                onToggleBookmark = { onToggleBookmark(question.id) }
                            )
                        }
                    }
                }
            }
        }

        // Error
        if (uiState.error != null) {
            item {
                Text(
                    text = uiState.error ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(AppleSpacing.Standard)
                )
            }
        }
    }
}

@Composable
private fun QuestionBankItemRow(
    item: QuestionBankItem,
    onToggleBookmark: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
        ) {
            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)) {
                StatusBadge(
                    text = item.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = when (item.difficulty) {
                        Difficulty.EASY -> MaterialTheme.colorScheme.primary
                        Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                        Difficulty.HARD -> MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = "${item.subject} / ${item.topic}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onToggleBookmark) {
            Icon(
                imageVector = if (item.isBookmarked) HogwartsIcons.BookmarkFilled else HogwartsIcons.Bookmark,
                contentDescription = if (item.isBookmarked) "Remove bookmark" else "Bookmark",
                tint = if (item.isBookmarked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun PracticeModeContent(
    uiState: QuestionBankUiState,
    onSelectAnswer: (String) -> Unit,
    onRevealExplanation: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val question = uiState.currentPracticeQuestion

    Column(modifier = modifier) {
        // Progress
        LinearProgressIndicator(
            progress = { uiState.practiceProgress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (question == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = HogwartsIcons.Exams,
                    title = "No Questions",
                    subtitle = "No questions match your filters for practice."
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppleSpacing.Standard)
            ) {
                // Question info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${uiState.practiceQuestionIndex + 1} of ${uiState.filteredQuestions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = question.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = when (question.difficulty) {
                                Difficulty.EASY -> MaterialTheme.colorScheme.primary
                                Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                                Difficulty.HARD -> MaterialTheme.colorScheme.error
                            }
                        )
                        IconButton(onClick = { onToggleBookmark(question.id) }) {
                            Icon(
                                imageVector = if (question.isBookmarked) HogwartsIcons.BookmarkFilled else HogwartsIcons.Bookmark,
                                contentDescription = stringResource(R.string.exams_qbank_bookmark_cd),
                                tint = if (question.isBookmarked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                // Question text
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(AppleSpacing.Large))

                // Answer options (MCQ)
                if (question.type == QuestionType.MCQ && question.options != null) {
                    question.options.forEach { option ->
                        val isSelected = uiState.practiceAnswer == option
                        val isCorrect = option == question.correctAnswer
                        val showResult = uiState.showExplanation

                        val cardColor = when {
                            showResult && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                            showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        if (!uiState.showExplanation) onSelectAnswer(option)
                                    },
                                    role = Role.RadioButton
                                ),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (showResult && isCorrect) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = HogwartsIcons.Checkmark,
                                        contentDescription = stringResource(R.string.exams_qbank_correct_cd),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if (question.type == QuestionType.TRUE_FALSE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
                    ) {
                        listOf("True", "False").forEach { option ->
                            val isSelected = uiState.practiceAnswer == option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (!uiState.showExplanation) onSelectAnswer(option)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppleSpacing.Large),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
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

                Spacer(modifier = Modifier.height(AppleSpacing.Standard))

                // Check answer / Explanation
                if (uiState.practiceAnswer != null && !uiState.showExplanation) {
                    Button(
                        onClick = onRevealExplanation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.exams_qbank_check_answer))
                    }
                }

                if (uiState.showExplanation) {
                    val isCorrect = uiState.practiceAnswer == question.correctAnswer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(AppleSpacing.Standard)) {
                            Text(
                                text = if (isCorrect) "Correct!" else "Incorrect",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            if (!isCorrect) {
                                Text(
                                    text = "Correct answer: ${question.correctAnswer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            if (question.explanation != null) {
                                Spacer(modifier = Modifier.height(AppleSpacing.Compact))
                                Text(
                                    text = question.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCorrect) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppleSpacing.Standard),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = uiState.practiceQuestionIndex > 0
                ) {
                    Text(stringResource(R.string.exams_qbank_previous))
                }
                Button(
                    onClick = onNext,
                    enabled = uiState.practiceQuestionIndex < uiState.filteredQuestions.size - 1
                ) {
                    Text(stringResource(R.string.exams_qbank_next))
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun QuestionBankItemRowPreview() {
    HogwartsTheme {
        QuestionBankItemRow(
            item = QuestionBankItem(
                id = "1",
                subject = "Mathematics",
                topic = "Algebra",
                difficulty = Difficulty.MEDIUM,
                type = QuestionType.MCQ,
                question = "What is the value of x in the equation 2x + 5 = 15?",
                options = listOf("3", "5", "7", "10"),
                correctAnswer = "5",
                explanation = "Solving: 2x + 5 = 15, 2x = 10, x = 5",
                isBookmarked = true
            ),
            onToggleBookmark = {}
        )
    }
}
