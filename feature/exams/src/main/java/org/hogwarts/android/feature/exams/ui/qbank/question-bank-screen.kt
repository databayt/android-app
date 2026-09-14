package org.hogwarts.android.feature.exams.ui.qbank

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import org.hogwarts.android.core.designsystem.kit.ItemGridMore
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.data.repository.ExamsRepository
import org.hogwarts.android.feature.exams.domain.model.Question
import org.hogwarts.android.feature.exams.ui.ExamsLinks
import org.hogwarts.android.feature.exams.ui.ExamsPage
import org.hogwarts.android.feature.exams.ui.ExamsTab
import org.hogwarts.android.feature.exams.ui.ExamsTabs
import org.hogwarts.android.feature.exams.ui.LoadFailedNote
import org.hogwarts.android.feature.exams.ui.NoticeCard
import org.hogwarts.android.feature.exams.ui.SkeletonBlock
import org.hogwarts.android.feature.exams.ui.mark
import javax.inject.Inject

private const val PAGE = 30

data class QuestionBankUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val questions: List<Question> = emptyList(),
    val total: Int = 0,
) {
    val hasMore: Boolean get() = questions.size < total
}

/** `/exams/qbank` for staff: the school's questions, newest first — `GET /api/mobile/exams/question-bank`. */
@HiltViewModel
class QuestionBankViewModel @Inject constructor(
    private val repository: ExamsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuestionBankUiState())
    val uiState: StateFlow<QuestionBankUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = fetch(PAGE)

    /** The route pages by `page`; asking for a longer first page keeps the list one request. */
    fun loadMore() = fetch(_uiState.value.questions.size + PAGE)

    private fun fetch(size: Int) {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            try {
                val page = repository.questionBank(perPage = size)
                _uiState.update { it.copy(loading = false, questions = page.items, total = page.total) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, loadFailed = true) }
            }
        }
    }
}

@Composable
fun QuestionBankScreen(
    onNavigate: (Any) -> Unit,
    onOpenHref: (String) -> Unit,
    role: UserRole?,
    viewModel: QuestionBankViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val links = remember(role, onNavigate, onOpenHref) { ExamsLinks(role, onNavigate, onOpenHref) }
    QuestionBankView(
        state = state,
        tabs = { ExamsTabs(role, ExamsTab.Qbank, links) },
        onAdd = { links.open("/exams/qbank/new") },
        onOpenQuestion = { id -> links.open("/exams/qbank/$id") },
        onMore = viewModel::loadMore,
        onRetry = viewModel::load,
    )
}

@Composable
@ReadOnlyComposable
private fun typeName(type: String?): String? = when (type) {
    "MULTIPLE_CHOICE" -> stringResource(R.string.exams_qtype_multiple_choice)
    "TRUE_FALSE" -> stringResource(R.string.exams_qtype_true_false)
    "FILL_BLANK" -> stringResource(R.string.exams_qtype_fill_blank)
    "SHORT_ANSWER" -> stringResource(R.string.exams_qtype_short_answer)
    "ESSAY" -> stringResource(R.string.exams_qtype_essay)
    "MATCHING" -> stringResource(R.string.exams_qtype_matching)
    "ORDERING" -> stringResource(R.string.exams_qtype_ordering)
    "MULTI_SELECT" -> stringResource(R.string.exams_qtype_multi_select)
    else -> type
}

@Composable
@ReadOnlyComposable
private fun difficultyName(difficulty: String?): String? = when (difficulty) {
    "EASY" -> stringResource(R.string.exams_difficulty_easy)
    "MEDIUM" -> stringResource(R.string.exams_difficulty_medium)
    "HARD" -> stringResource(R.string.exams_difficulty_hard)
    else -> difficulty
}

/**
 * `/exams/qbank` on a phone. The web shows a data table; the kit carries its
 * columns in order as rows: the question, then type and difficulty, the
 * subject, and the points at the far end. Filters, bloom level, usage and
 * quality columns need data the mobile route does not return.
 */
@Composable
internal fun QuestionBankView(
    state: QuestionBankUiState,
    tabs: @Composable () -> Unit,
    onAdd: () -> Unit,
    onOpenQuestion: (String) -> Unit,
    onMore: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    ExamsPage(tabs = tabs) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            if (state.loadFailed) LoadFailedNote(onRetry)
            if (state.loading && state.questions.isEmpty()) {
                SkeletonBlock(Modifier.fillMaxWidth().height(160.dp))
                return@Column
            }
            SectionHeader(
                title = stringResource(R.string.exams_stat_question_bank),
                description = state.total.toString(),
                action = { PillButton(stringResource(R.string.exams_add_question), onClick = onAdd) },
            )
            if (state.questions.isEmpty()) {
                if (!state.loadFailed) {
                    NoticeCard(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = stringResource(R.string.exams_qbank_empty),
                        description = stringResource(R.string.exams_qbank_add_description),
                    )
                }
                return@Column
            }
            ListRows(
                divided = true,
                rows = state.questions.map { q ->
                    {
                        val points = q.points
                        ListRow(
                            title = q.text,
                            badge = difficultyName(q.difficulty)?.let { name ->
                                {
                                    LabelBadge(
                                        name,
                                        variant = when (q.difficulty) {
                                            "HARD" -> BadgeVariant.Destructive
                                            "MEDIUM" -> BadgeVariant.Secondary
                                            else -> BadgeVariant.Outline
                                        },
                                    )
                                }
                            },
                            description = typeName(q.type),
                            meta = q.subject,
                            trailing = points?.let {
                                {
                                    Text(
                                        mark(it),
                                        style = HogwartsTheme.type.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.foreground,
                                    )
                                }
                            },
                            chevron = true,
                            onClick = { onOpenQuestion(q.id) },
                        )
                    }
                },
            )
            if (state.hasMore) {
                ItemGridMore(
                    label = stringResource(R.string.exams_load_more),
                    onClick = onMore,
                    loading = state.loading,
                    loadingLabel = stringResource(R.string.exams_loading),
                )
            }
        }
    }
}
