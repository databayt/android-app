package org.hogwarts.android.feature.exams.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.InfoRow
import org.hogwarts.android.core.designsystem.kit.InfoRows
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.data.repository.ExamsApiException
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.domain.model.OwnResult
import org.hogwarts.android.feature.exams.navigation.ExamDetail
import org.hogwarts.android.feature.exams.navigation.OnlineExam
import org.hogwarts.android.feature.exams.ui.ExamsLinks
import org.hogwarts.android.feature.exams.ui.ExamsPage
import org.hogwarts.android.feature.exams.ui.ExamsTab
import org.hogwarts.android.feature.exams.ui.ExamsTabs
import org.hogwarts.android.feature.exams.ui.LoadFailedNote
import org.hogwarts.android.feature.exams.ui.NoticeCard
import org.hogwarts.android.feature.exams.ui.SkeletonBlock
import org.hogwarts.android.feature.exams.ui.examsFormat
import org.hogwarts.android.feature.exams.ui.ltr
import org.hogwarts.android.feature.exams.ui.mark
import javax.inject.Inject

data class ExamDetailUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val notFound: Boolean = false,
    val role: UserRole? = null,
    val exam: Exam? = null,
    val result: OwnResult? = null,
) {
    private val isStudent get() = role == UserRole.STUDENT

    /** Web: a student, the exam in progress, no result yet. */
    val canTake: Boolean get() = isStudent && exam?.status == "IN_PROGRESS" && result == null

    /** Web: ADMIN, TEACHER or DEVELOPER. */
    val canEdit: Boolean get() = role == UserRole.ADMIN || role == UserRole.TEACHER || role == UserRole.DEVELOPER
}

/** `exams/[id]/page.tsx`: the exam, and for a student their own result. */
@HiltViewModel
class ExamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ExamsRepository,
    tenantContext: TenantContext,
) : ViewModel() {
    private val examId = savedStateHandle.toRoute<ExamDetail>().examId
    private val _uiState = MutableStateFlow(ExamDetailUiState(role = tenantContext.userRole))
    val uiState: StateFlow<ExamDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            try {
                val student = _uiState.value.role == UserRole.STUDENT
                val result = if (student) async { runCatching { repository.ownResult(examId) }.getOrNull() } else null
                val exam = repository.exam(examId)
                _uiState.update { it.copy(loading = false, exam = exam, result = result?.await()) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ExamsApiException) {
                _uiState.update { it.copy(loading = false, notFound = e.code == 404, loadFailed = e.code != 404) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, loadFailed = true) }
            }
        }
    }
}

@Composable
fun ExamDetailScreen(
    onNavigate: (Any) -> Unit,
    onOpenHref: (String) -> Unit,
    viewModel: ExamDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val links = remember(state.role, onNavigate, onOpenHref) { ExamsLinks(state.role, onNavigate, onOpenHref) }
    ExamDetailView(
        state = state,
        tabs = { ExamsTabs(state.role, ExamsTab.Overview, links) },
        onTake = { id -> onNavigate(OnlineExam(id)) },
        onEdit = { id -> onOpenHref("/exams/$id/edit") },
        onRetry = viewModel::load,
    )
}

/**
 * `/exams/:id` on a phone. The web has no phone-only layout, so the kit
 * carries its content in order: heading and actions, the student's result,
 * the exam's facts, then its instructions.
 */
@Composable
internal fun ExamDetailView(
    state: ExamDetailUiState,
    tabs: (@Composable () -> Unit)?,
    onTake: (String) -> Unit,
    onEdit: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val format = examsFormat()
    // `exams/layout.tsx` wraps this page too, so the section's tabs stay above it.
    ExamsPage(tabs = tabs) {
        val exam = state.exam
        if (state.notFound) {
            NoticeCard(icon = Icons.Outlined.ErrorOutline, title = stringResource(R.string.exams_detail_not_found), description = stringResource(R.string.exams_contact_admin))
            return@ExamsPage
        }
        if (exam == null) {
            if (state.loadFailed) LoadFailedNote(onRetry) else SkeletonBlock(Modifier.fillMaxWidth().height(240.dp))
            return@ExamsPage
        }
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(exam.title, style = type.section, color = colors.foreground)
                Text(
                    exam.description?.takeIf { it.isNotBlank() } ?: stringResource(R.string.exams_description),
                    style = type.body,
                    color = colors.mutedForeground,
                )
            }
            if (state.canTake || state.canEdit) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.canTake) PillButton(stringResource(R.string.exams_take_exam), onClick = { onTake(exam.id) }, icon = Icons.Outlined.PlayArrow)
                    if (state.canEdit) PillButton(stringResource(R.string.exams_edit), onClick = { onEdit(exam.id) }, variant = PillVariant.Outline, icon = Icons.Outlined.Edit)
                }
            }

            val result = state.result
            if (state.role == UserRole.STUDENT && result != null) {
                val pct = result.percentage ?: 0.0
                StatPanel(
                    title = stringResource(R.string.exams_your_result),
                    items = listOfNotNull(
                        StatItem("score", stringResource(R.string.exams_score), ltr("${mark(result.score)}/${mark(result.maxScore)}")),
                        StatItem("percentage", stringResource(R.string.exams_percentage), ltr("${"%.1f".format(java.util.Locale.ENGLISH, pct)}%")),
                        result.grade?.takeIf { it.isNotBlank() }?.let { StatItem("grade", stringResource(R.string.exams_grade), it) },
                        StatItem(
                            "outcome",
                            stringResource(R.string.exams_status),
                            stringResource(if (pct >= 50) R.string.exams_passed else R.string.exams_failed),
                            tone = if (pct >= 50) StatTone.Positive else StatTone.Negative,
                        ),
                    ),
                )
            }

            val minutes = stringResource(R.string.exams_minutes)
            val passing = stringResource(R.string.exams_passing)
            InfoRows(
                rows = listOf(
                    InfoRow(stringResource(R.string.exams_exam_date), exam.examDate?.let { format.shortDate(format.date(it)) }),
                    InfoRow(
                        stringResource(R.string.exams_time),
                        listOfNotNull(exam.startTime, exam.endTime).joinToString(" - ").takeIf { it.isNotEmpty() }
                            ?.let { ltr(it) + (exam.durationMinutes?.let { d -> " · $d $minutes" } ?: "") },
                    ),
                    InfoRow(
                        stringResource(R.string.exams_marks_label),
                        exam.totalMarks?.let { total -> "$total" + (exam.passingMarks?.let { " · $passing: $it" } ?: "") },
                    ),
                    InfoRow(stringResource(R.string.exams_subject), exam.subject),
                    InfoRow(stringResource(R.string.exams_status), listOfNotNull(exam.status, exam.examType).joinToString(" · ").ifEmpty { null }),
                ),
            )

            if (!exam.instructions.isNullOrBlank()) {
                Column {
                    SectionHeader(stringResource(R.string.exams_instructions))
                    Text(exam.instructions, style = type.body, color = colors.foreground, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
