package org.hogwarts.android.feature.exams.ui.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.AppTileGrid
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.BrandBanner
import org.hogwarts.android.core.designsystem.kit.BrandPill
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.ProgressBar
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.kit.TileArt
import org.hogwarts.android.core.designsystem.kit.TileFace
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.navigation.ExamDetail
import org.hogwarts.android.feature.exams.ui.ExamsPage
import org.hogwarts.android.feature.exams.ui.ExamsLinks
import org.hogwarts.android.feature.exams.ui.LoadFailedNote
import org.hogwarts.android.feature.exams.ui.SkeletonBlock
import org.hogwarts.android.feature.exams.ui.examsFormat
import org.hogwarts.android.feature.exams.ui.ltr

@Composable
internal fun AdminLandingScreen(
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    onNavigate: (Any) -> Unit,
    viewModel: AdminLandingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AdminLandingView(state = state, tabs = tabs, links = links, onOpenExam = { onNavigate(ExamDetail(it)) }, onRetry = viewModel::load)
}

private fun figure(value: Int?): String = value?.toString() ?: "—"

/**
 * Admin view of `/exams` on a phone — the `md:hidden` branch of
 * `exams/content.tsx`: the next exam as the green banner, the figures as grey
 * panels, the jobs as app tiles and one list of rows.
 */
@Composable
internal fun AdminLandingView(
    state: AdminLandingUiState,
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    onOpenExam: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val format = examsFormat()
    ExamsPage(tabs = tabs) {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            if (state.loadFailed) LoadFailedNote(onRetry)

            val next = state.nextExam
            val headline: AnnotatedString = buildAnnotatedString {
                if (next != null) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(next.title.ifBlank { next.subject ?: stringResource(R.string.exams_upcoming_exams) })
                    }
                    val date = next.examDate?.let { format.shortDate(format.date(it)) }
                    val line = listOfNotNull(date, next.startTime?.let(::ltr)).joinToString(" · ")
                    if (line.isNotEmpty()) {
                        append("\n")
                        withStyle(SpanStyle(fontSize = 18.sp)) { append(line) }
                    }
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(stringResource(R.string.exams_no_upcoming_exams)) }
                    append("\n")
                    withStyle(SpanStyle(fontSize = 18.sp)) { append(stringResource(R.string.exams_create_description)) }
                }
            }
            if (state.loading && next == null) {
                SkeletonBlock(Modifier.fillMaxWidth().height(232.dp))
            } else {
                BrandBanner(
                    headline = headline,
                    eyebrow = next?.let { "${stringResource(R.string.exams_upcoming_exams)} · ${it.subject.orEmpty()}" },
                    actions = {
                        if (next != null) {
                            BrandPill(stringResource(R.string.exams_view_details), onClick = { onOpenExam(next.id) })
                        } else {
                            BrandPill(stringResource(R.string.exams_create_exam), onClick = { links.open("/exams/new") })
                        }
                        BrandPill(stringResource(R.string.exams_stat_upcoming), onClick = { links.open("/exams/upcoming") }, ghost = true)
                    },
                )
            }

            StatPanel(
                items = listOf(
                    // Web hints "+x%" month over month; the API carries no created dates to compute it.
                    StatItem("total", stringResource(R.string.exams_stat_total_exams), figure(state.total)),
                    StatItem(
                        "upcoming",
                        stringResource(R.string.exams_stat_upcoming),
                        figure(state.upcoming),
                        hint = stringResource(R.string.exams_stat_scheduled_for_future),
                        onClick = { links.open("/exams/upcoming") },
                    ),
                    StatItem("qbank", stringResource(R.string.exams_stat_question_bank), figure(state.questionBank), onClick = { links.open("/exams/qbank") }),
                    StatItem("students", stringResource(R.string.exams_stat_students), figure(state.students), hint = stringResource(R.string.exams_stat_enrolled_students)),
                ),
            )

            Column {
                SectionHeader(stringResource(R.string.exams_quick_actions))
                AppTileGrid(
                    items = listOf(
                        AppTileItem("new", stringResource(R.string.exams_create_exam), { links.open("/exams/new") }, icon = Icons.Outlined.CalendarToday, tint = TileTint.Blue),
                        AppTileItem("question", stringResource(R.string.exams_add_question), { links.open("/exams/qbank/new") }, icon = Icons.AutoMirrored.Outlined.MenuBook, tint = TileTint.Indigo),
                        AppTileItem(
                            "pending",
                            stringResource(R.string.exams_stat_pending_marking),
                            { links.open("/exams/mark/pending") },
                            icon = Icons.Outlined.AssignmentTurnedIn,
                            tint = TileTint.Orange,
                            badge = state.pendingMarking ?: 0,
                        ),
                        AppTileItem("ai", stringResource(R.string.exams_ai_powered), { links.open("/exams/qbank/ai-generate") }, icon = Icons.Outlined.AutoAwesome, tint = TileTint.Purple),
                    ),
                )
            }

            Column {
                SectionHeader(stringResource(R.string.exams_dashboard_title))
                val count: @Composable (Int?) -> Unit = { value ->
                    Text(figure(value), style = HogwartsTheme.type.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = HogwartsTheme.colors.foreground)
                }
                ListRows(
                    divided = true,
                    rows = listOf(
                        {
                            ListRow(
                                title = stringResource(R.string.exams_block_qbank_title),
                                description = stringResource(R.string.exams_block_qbank_description),
                                art = { TileFace(icon = Icons.AutoMirrored.Outlined.MenuBook, tint = TileTint.Indigo) },
                                trailing = { count(state.questionBank) },
                                chevron = true,
                                onClick = { links.open("/exams/qbank") },
                            )
                        },
                        {
                            ListRow(
                                title = stringResource(R.string.exams_block_generate_title),
                                description = stringResource(R.string.exams_block_generate_description),
                                art = { TileFace(icon = Icons.Outlined.AutoAwesome, tint = TileTint.Purple) },
                                chevron = true,
                                onClick = { links.open("/exams/generate") },
                            )
                        },
                        {
                            val pending = state.pendingMarking ?: 0
                            ListRow(
                                title = stringResource(R.string.exams_block_mark_title),
                                description = stringResource(R.string.exams_block_mark_description),
                                art = { TileFace(icon = Icons.Outlined.AssignmentTurnedIn, tint = TileTint.Orange) },
                                trailing = if (pending > 0) ({ LabelBadge(pending.toString(), variant = BadgeVariant.Destructive) }) else null,
                                chevron = true,
                                onClick = { links.open("/exams/mark") },
                            )
                        },
                        {
                            // Web trails the results-generated count; no mobile route returns it.
                            ListRow(
                                title = stringResource(R.string.exams_block_results_title),
                                description = stringResource(R.string.exams_block_results_description),
                                art = { TileFace(art = TileArt.Grades) },
                                trailing = { count(null) },
                                chevron = true,
                                onClick = { links.open("/exams/result") },
                            )
                        },
                    ),
                )
            }

            StatPanel(
                items = listOf(
                    StatItem(
                        "completed",
                        stringResource(R.string.exams_stat_completed),
                        figure(state.completed),
                        hint = state.completionRate?.let { "$it%" },
                        extra = state.completionRate?.let { rate -> { ProgressBar(rate.toFloat()) } },
                    ),
                    StatItem(
                        "marking",
                        stringResource(R.string.exams_stat_pending_marking),
                        figure(state.pendingMarking),
                        hint = state.markingProgress?.let { "$it%" },
                        tone = if ((state.pendingMarking ?: 0) > 0) StatTone.Warning else StatTone.Default,
                        extra = state.markingProgress?.let { rate -> { ProgressBar(rate.toFloat()) } },
                    ),
                    StatItem(
                        "templates",
                        stringResource(R.string.exams_stat_templates),
                        figure(null),
                        hint = stringResource(R.string.exams_stat_exam_templates),
                        onClick = { links.open("/exams/generate/templates") },
                    ),
                    StatItem(
                        "results",
                        stringResource(R.string.exams_stat_results_generated),
                        figure(null),
                        hint = stringResource(R.string.exams_stat_student_results),
                        onClick = { links.open("/exams/result") },
                    ),
                ),
            )

        }
    }
}
