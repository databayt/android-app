package org.hogwarts.android.feature.exams.ui.upcoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.DateTile
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.di.ExamsClock
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.navigation.ExamDetail
import org.hogwarts.android.feature.exams.ui.ExamsLinks
import org.hogwarts.android.feature.exams.ui.ExamsPage
import org.hogwarts.android.feature.exams.ui.ExamsTab
import org.hogwarts.android.feature.exams.ui.ExamsTabs
import org.hogwarts.android.feature.exams.ui.LoadFailedNote
import org.hogwarts.android.feature.exams.ui.NoticeCard
import org.hogwarts.android.feature.exams.ui.SkeletonBlock
import org.hogwarts.android.feature.exams.ui.examsFormat
import org.hogwarts.android.feature.exams.ui.ltr
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class UpcomingUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val today: LocalDate = LocalDate.MIN,
    val exams: List<Exam> = emptyList(),
) {
    fun daysUntil(exam: Exam, dateOf: (Instant) -> LocalDate): Int =
        exam.examDate?.let { ChronoUnit.DAYS.between(today, dateOf(it)).toInt() } ?: 0
}

/** `exams/upcoming/content.tsx`: open exams from today on, soonest first, at most 30. */
@HiltViewModel
class UpcomingViewModel @Inject constructor(
    private val repository: ExamsRepository,
    @ExamsClock private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UpcomingUiState(today = LocalDate.now(clock)))
    val uiState: StateFlow<UpcomingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false, today = LocalDate.now(clock)) }
        viewModelScope.launch {
            try {
                val exams = repository.exams(upcoming = true, perPage = 100).items
                    .filter { it.isOpen }
                    .sortedBy { it.examDate ?: Instant.MAX }
                    .take(30)
                _uiState.update { it.copy(loading = false, exams = exams) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, loadFailed = true) }
            }
        }
    }
}

@Composable
fun UpcomingScreen(
    onNavigate: (Any) -> Unit,
    onOpenHref: (String) -> Unit,
    role: UserRole?,
    viewModel: UpcomingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val links = remember(role, onNavigate, onOpenHref) { ExamsLinks(role, onNavigate, onOpenHref) }
    UpcomingView(
        state = state,
        role = role,
        tabs = { ExamsTabs(role, ExamsTab.Upcoming, links) },
        onOpenExam = { onNavigate(ExamDetail(it)) },
        onSchedule = { links.open("/exams/new") },
        onRetry = viewModel::load,
    )
}

/** Web badges shorten exam types to fit the card. */
private val examTypeLabels = mapOf(
    "MIDTERM" to "Mid", "FINAL" to "Final", "QUIZ" to "Quiz", "TEST" to "Test",
    "ASSIGNMENT" to "HW", "HOMEWORK" to "HW", "PROJECT" to "Proj", "PRACTICAL" to "Prac",
)

/**
 * `/exams/upcoming` on a phone. The web has no phone-only layout here, so the
 * kit carries its content in its order: four counts as one grey panel, then
 * the exams grouped today / tomorrow / this week / later as dated rows.
 */
@Composable
internal fun UpcomingView(
    state: UpcomingUiState,
    role: UserRole?,
    tabs: @Composable () -> Unit,
    onOpenExam: (String) -> Unit,
    onSchedule: () -> Unit,
    onRetry: () -> Unit,
) {
    val format = examsFormat()
    val colors = HogwartsTheme.colors
    ExamsPage(tabs = tabs) {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            if (state.loadFailed) LoadFailedNote(onRetry)
            if (state.loading && state.exams.isEmpty()) {
                SkeletonBlock(Modifier.fillMaxWidth().height(160.dp))
                return@Column
            }
            val days = state.exams.associate { it.id to state.daysUntil(it, format::date) }
            val today = state.exams.filter { days[it.id] == 0 }
            val tomorrow = state.exams.filter { days[it.id] == 1 }
            val thisWeek = state.exams.filter { (days[it.id] ?: 0) in 2..7 }
            val later = state.exams.filter { (days[it.id] ?: 0) > 7 }

            StatPanel(
                items = listOf(
                    StatItem("total", stringResource(R.string.exams_upcoming_stat_total), state.exams.size.toString()),
                    StatItem("today", stringResource(R.string.exams_upcoming_stat_today), today.size.toString()),
                    StatItem("tomorrow", stringResource(R.string.exams_upcoming_stat_tomorrow), tomorrow.size.toString()),
                    StatItem("week", stringResource(R.string.exams_upcoming_stat_this_week), thisWeek.size.toString()),
                ),
            )

            if (state.exams.isEmpty()) {
                if (!state.loadFailed) {
                    NoticeCard(
                        icon = Icons.Outlined.CalendarToday,
                        title = stringResource(R.string.exams_upcoming_no_exams),
                        description = stringResource(R.string.exams_upcoming_no_exams_description),
                        action = if (role != UserRole.STUDENT && role != UserRole.GUARDIAN) {
                            { PillButton(stringResource(R.string.exams_upcoming_schedule_exam), onClick = onSchedule) }
                        } else {
                            null
                        },
                    )
                }
                return@Column
            }

            val minutes = stringResource(R.string.exams_minutes_short)
            val marks = stringResource(R.string.exams_upcoming_marks)
            val todayWord = stringResource(R.string.exams_upcoming_label_today)
            val tomorrowWord = stringResource(R.string.exams_upcoming_label_tomorrow)
            val daysWord = stringResource(R.string.exams_upcoming_label_days_left)
            listOf(
                stringResource(R.string.exams_upcoming_section_today) to today,
                stringResource(R.string.exams_upcoming_section_tomorrow) to tomorrow,
                stringResource(R.string.exams_upcoming_section_this_week) to thisWeek,
                stringResource(R.string.exams_upcoming_section_later) to later,
            ).forEachIndexed { index, (title, exams) ->
                if (exams.isEmpty()) return@forEachIndexed
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "$title (${exams.size})",
                        style = HogwartsTheme.type.section,
                        color = if (index == 0) colors.destructive else colors.foreground,
                    )
                    ListRows(
                        rows = exams.map { exam ->
                            {
                                val d = days[exam.id] ?: 0
                                val date = exam.examDate?.let(format::date) ?: state.today
                                val urgency = when {
                                    d == 0 -> todayWord
                                    d == 1 -> tomorrowWord
                                    d <= 7 -> "$d $daysWord"
                                    else -> format.monthDay(date)
                                }
                                val time = listOfNotNull(exam.startTime, exam.endTime).joinToString(" - ")
                                ListRow(
                                    title = exam.title,
                                    art = { DateTile(weekday = format.weekday(date), day = format.day(date)) },
                                    badge = {
                                        LabelBadge(
                                            urgency,
                                            variant = when (d) {
                                                0 -> BadgeVariant.Destructive
                                                1 -> BadgeVariant.Secondary
                                                else -> BadgeVariant.Outline
                                            },
                                        )
                                    },
                                    description = listOfNotNull(exam.subject?.takeIf { it.isNotBlank() }, format.weekdayDate(date)).joinToString(" · "),
                                    meta = listOfNotNull(
                                        time.takeIf { it.isNotEmpty() }?.let { ltr("$it (${exam.durationMinutes ?: 0} $minutes)") },
                                        exam.examType?.let { examTypeLabels[it] ?: it },
                                        exam.totalMarks?.let { "$it $marks" },
                                    ).joinToString(" · ").ifEmpty { null },
                                    onClick = { onOpenExam(exam.id) },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
