package org.hogwarts.android.feature.exams.ui.take

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.designsystem.kit.ChoiceChip
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.kit.FormButton
import org.hogwarts.android.core.designsystem.kit.FormButtonVariant
import org.hogwarts.android.core.designsystem.kit.Input
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.ProgressBar
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.data.repository.ExamsApiException
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.OnlineQuestion
import org.hogwarts.android.feature.exams.domain.model.SubmitOutcome
import org.hogwarts.android.feature.exams.navigation.OnlineExam as OnlineExamRoute
import org.hogwarts.android.feature.exams.ui.ExamsPage
import org.hogwarts.android.feature.exams.ui.NoticeCard
import org.hogwarts.android.feature.exams.ui.ltr
import org.hogwarts.android.feature.exams.ui.mark
import javax.inject.Inject

/** Why the session could not open — the route's 4xx messages, named. */
enum class TakeProblem { NotInProgress, AlreadySubmitted, MaxAttempts, Forbidden, NotFound, Network }

data class OnlineExamUiState(
    val starting: Boolean = true,
    val problem: TakeProblem? = null,
    val exam: OnlineExam? = null,
    val secondsLeft: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val confirming: Boolean = false,
    val submitting: Boolean = false,
    val submitFailed: Boolean = false,
    val outcome: SubmitOutcome? = null,
) {
    val answeredCount: Int get() = exam?.questions?.count { !answers[it.id].isNullOrBlank() } ?: 0
    val total: Int get() = exam?.questions?.size ?: 0
}

/**
 * `/exams/:id/take` on the mobile routes: `GET /exams/:id/online` opens or
 * resumes the session (and its clock), `POST /exams/:id/answers` submits once.
 */
@HiltViewModel
class OnlineExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ExamsRepository,
) : ViewModel() {
    private val examId = savedStateHandle.toRoute<OnlineExamRoute>().examId
    private val _uiState = MutableStateFlow(OnlineExamUiState())
    val uiState: StateFlow<OnlineExamUiState> = _uiState.asStateFlow()
    private var clock: Job? = null

    init {
        start()
    }

    fun start() {
        _uiState.update { it.copy(starting = true, problem = null) }
        viewModelScope.launch {
            try {
                val exam = repository.startOnlineExam(examId)
                _uiState.update { it.copy(starting = false, exam = exam, secondsLeft = exam.secondsRemaining) }
                runClock()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ExamsApiException) {
                _uiState.update { it.copy(starting = false, problem = problemFor(e)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(starting = false, problem = TakeProblem.Network) }
            }
        }
    }

    private fun runClock() {
        clock?.cancel()
        clock = viewModelScope.launch {
            while (_uiState.value.secondsLeft > 0 && _uiState.value.outcome == null) {
                delay(1_000)
                _uiState.update { it.copy(secondsLeft = (it.secondsLeft - 1).coerceAtLeast(0)) }
            }
            if (_uiState.value.outcome == null) submit()
        }
    }

    fun answer(questionId: String, value: String) {
        _uiState.update { it.copy(answers = it.answers + (questionId to value)) }
    }

    fun askToSubmit() = _uiState.update { it.copy(confirming = true, submitFailed = false) }

    fun keepGoing() = _uiState.update { it.copy(confirming = false) }

    fun submit() {
        val state = _uiState.value
        val exam = state.exam ?: return
        if (state.submitting || state.outcome != null) return
        _uiState.update { it.copy(submitting = true, submitFailed = false) }
        viewModelScope.launch {
            try {
                val answered = state.answers.filterValues { it.isNotBlank() }
                val outcome = repository.submitAnswers(exam.examId, exam.sessionId, answered)
                clock?.cancel()
                _uiState.update { it.copy(submitting = false, confirming = false, outcome = outcome) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(submitting = false, submitFailed = true) }
            }
        }
    }

    private fun problemFor(e: ExamsApiException): TakeProblem = when {
        e.code == 403 -> TakeProblem.Forbidden
        e.code == 404 -> TakeProblem.NotFound
        e.serverMessage?.contains("already submitted", ignoreCase = true) == true -> TakeProblem.AlreadySubmitted
        e.serverMessage?.contains("Maximum attempts", ignoreCase = true) == true -> TakeProblem.MaxAttempts
        e.code == 400 -> TakeProblem.NotInProgress
        else -> TakeProblem.Network
    }
}

@Composable
fun OnlineExamScreen(
    onDone: () -> Unit,
    viewModel: OnlineExamViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnlineExamView(
        state = state,
        onAnswer = viewModel::answer,
        onAskSubmit = viewModel::askToSubmit,
        onKeepGoing = viewModel::keepGoing,
        onSubmit = viewModel::submit,
        onRetry = viewModel::start,
        onDone = onDone,
    )
}

/** `mm:ss`, as `take/timer.tsx` pads it. */
internal fun clockLabel(seconds: Int): String = String.format(java.util.Locale.ENGLISH, "%02d:%02d", seconds / 60, seconds % 60)

/**
 * The exam player on a phone, on the kit: the clock and progress as a grey
 * panel, then every question with its answer control, then one submit with
 * the web's confirmation copy. Proctoring (focus / copy violations) is not
 * reported from the app.
 */
@Composable
internal fun OnlineExamView(
    state: OnlineExamUiState,
    onAnswer: (String, String) -> Unit,
    onAskSubmit: () -> Unit,
    onKeepGoing: () -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    ExamsPage(tabs = null) {
        val outcome = state.outcome
        val exam = state.exam
        when {
            outcome != null -> {
                NoticeCard(
                    icon = Icons.Outlined.CheckCircle,
                    title = stringResource(R.string.exams_take_submitted),
                    description = stringResource(R.string.exams_take_answered_count, outcome.answered.toString(), outcome.total.toString()),
                    action = { PillButton(stringResource(R.string.exams_view_details), onClick = onDone) },
                )
                return@ExamsPage
            }
            state.problem != null -> {
                NoticeCard(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(problemTitle(state.problem)),
                    description = stringResource(R.string.exams_contact_admin),
                    action = if (state.problem == TakeProblem.Network) {
                        { PillButton(stringResource(R.string.exams_retry), onClick = onRetry) }
                    } else {
                        { PillButton(stringResource(R.string.exams_view_details), onClick = onDone) }
                    },
                )
                return@ExamsPage
            }
            state.starting || exam == null -> {
                NoticeCard(
                    icon = Icons.Outlined.HourglassTop,
                    title = stringResource(R.string.exams_take_starting),
                    description = "",
                )
                return@ExamsPage
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(exam.title, style = type.section, color = colors.foreground)

            val hurry = state.secondsLeft in 1..300
            StatPanel(
                items = listOf(
                    StatItem(
                        "remaining",
                        stringResource(R.string.exams_take_remaining),
                        ltr(clockLabel(state.secondsLeft)),
                        hint = if (hurry) stringResource(R.string.exams_take_hurry) else null,
                        tone = if (hurry) StatTone.Negative else StatTone.Default,
                    ),
                    StatItem(
                        "answered",
                        stringResource(R.string.exams_take_answered),
                        ltr("${state.answeredCount}/${state.total}"),
                        extra = { ProgressBar(if (state.total == 0) 0f else state.answeredCount * 100f / state.total) },
                    ),
                ),
            )

            if (!exam.instructions.isNullOrBlank()) {
                Text(exam.instructions, style = type.body, color = colors.mutedForeground)
            }

            exam.questions.forEachIndexed { index, question ->
                QuestionBlock(index, exam.questions.size, question, state.answers[question.id].orEmpty(), enabled = !state.submitting) {
                    onAnswer(question.id, it)
                }
            }

            if (state.submitFailed) FormAlert(stringResource(R.string.exams_take_submit_failed))

            if (state.confirming) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.exams_take_submit_title), style = type.rowTitle, color = colors.foreground)
                    Text(
                        stringResource(R.string.exams_take_answered_count, state.answeredCount.toString(), state.total.toString()),
                        style = type.body,
                        color = colors.foreground,
                    )
                    val unanswered = state.total - state.answeredCount
                    if (unanswered > 0) {
                        Text(stringResource(R.string.exams_take_unanswered_warning, unanswered.toString()), style = type.body, color = colors.destructive)
                    }
                    Text(stringResource(R.string.exams_take_final_warning), style = type.body, color = colors.mutedForeground)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormButton(stringResource(R.string.exams_take_continue), onClick = onKeepGoing, variant = FormButtonVariant.Outline, modifier = Modifier.weight(1f))
                        FormButton(
                            if (state.submitting) stringResource(R.string.exams_take_submitting) else stringResource(R.string.exams_take_submit),
                            onClick = onSubmit,
                            loading = state.submitting,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                FormButton(stringResource(R.string.exams_take_submit), onClick = onAskSubmit, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun problemTitle(problem: TakeProblem): Int = when (problem) {
    TakeProblem.NotInProgress -> R.string.exams_take_not_in_progress
    TakeProblem.AlreadySubmitted -> R.string.exams_take_already_submitted
    TakeProblem.MaxAttempts -> R.string.exams_take_max_attempts
    TakeProblem.Forbidden, TakeProblem.NotFound -> R.string.exams_detail_not_found
    TakeProblem.Network -> R.string.exams_unable_to_load
}

@Composable
private fun QuestionBlock(index: Int, total: Int, question: OnlineQuestion, answer: String, enabled: Boolean, onAnswer: (String) -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val points = question.marks?.let { m ->
            " · ${ltr(mark(m))} " + stringResource(if (m == 1.0) R.string.exams_take_point else R.string.exams_take_points)
        }.orEmpty()
        Text(
            stringResource(R.string.exams_take_question_of, (index + 1).toString(), total.toString()) + points,
            style = type.caption,
            color = colors.mutedForeground,
        )
        Text(question.text, style = type.rowTitle.copy(fontWeight = FontWeight.Medium), color = colors.foreground)
        when (question.type) {
            "TRUE_FALSE" -> {
                // Without options the server grades against "true" / "false"; show the words, send the values.
                val choices = question.options.map { it to it }.ifEmpty {
                    listOf(stringResource(R.string.exams_take_true) to "true", stringResource(R.string.exams_take_false) to "false")
                }
                Choices(choices, answer, enabled, onAnswer)
            }
            "MULTIPLE_CHOICE", "MULTI_SELECT" ->
                if (question.options.isNotEmpty()) Choices(question.options.map { it to it }, answer, enabled, onAnswer) else Unsupported()
            "SHORT_ANSWER", "FILL_BLANK" ->
                Input(answer, onAnswer, placeholder = stringResource(R.string.exams_take_answer_placeholder), enabled = enabled, modifier = Modifier.fillMaxWidth())
            "ESSAY" ->
                Input(answer, onAnswer, placeholder = stringResource(R.string.exams_take_essay_placeholder), enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp))
            else -> Unsupported()
        }
    }
}

@Composable
private fun Choices(options: List<Pair<String, String>>, selected: String, enabled: Boolean, onAnswer: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
        options.forEach { (label, value) ->
            ChoiceChip(label = label, selected = value == selected, onClick = { if (enabled) onAnswer(value) })
        }
    }
}

@Composable
private fun Unsupported() {
    Text(stringResource(R.string.exams_take_unsupported), style = HogwartsTheme.type.body, color = HogwartsTheme.colors.mutedForeground)
}
