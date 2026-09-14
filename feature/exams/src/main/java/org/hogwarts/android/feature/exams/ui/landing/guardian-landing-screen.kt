package org.hogwarts.android.feature.exams.ui.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.DateTile
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.kit.TileArt
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.ui.EmptyLine
import org.hogwarts.android.feature.exams.ui.ExamsLinks
import org.hogwarts.android.feature.exams.ui.ExamsPage
import org.hogwarts.android.feature.exams.ui.LoadFailedNote
import org.hogwarts.android.feature.exams.ui.NoticeCard
import org.hogwarts.android.feature.exams.ui.ResultTrailing
import org.hogwarts.android.feature.exams.ui.SkeletonBlock
import org.hogwarts.android.feature.exams.ui.TileRow
import org.hogwarts.android.feature.exams.ui.examsFormat
import org.hogwarts.android.feature.exams.ui.ltr
import org.hogwarts.android.feature.exams.ui.whenLabel
import java.time.temporal.ChronoUnit

@Composable
internal fun GuardianLandingScreen(
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    viewModel: GuardianLandingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GuardianLandingView(state = state, tabs = tabs, links = links, onRetry = viewModel::load)
}

/**
 * Guardian view of `/exams` on a phone — the `md:hidden` branch of
 * `exams/guardian-content.tsx`: the family's figures as one grey panel with
 * each child's average, the doors as tiles, then the dated lists as rows.
 */
@Composable
internal fun GuardianLandingView(
    state: GuardianLandingUiState,
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    onRetry: () -> Unit,
) {
    val format = examsFormat()
    ExamsPage(tabs = tabs) {
        if (state.noChildren) {
            NoticeCard(
                icon = Icons.Outlined.Groups,
                title = stringResource(R.string.exams_guardian_no_linked_students),
                description = stringResource(R.string.exams_guardian_link_students),
            )
            return@ExamsPage
        }
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            if (state.loadFailed) LoadFailedNote(onRetry)

            if (state.loading && state.children.isEmpty()) {
                SkeletonBlock(Modifier.fillMaxWidth().height(160.dp))
            } else if (!state.loadFailed) {
                val average = stringResource(R.string.exams_home_average)
                StatPanel(
                    items = listOf(
                        StatItem("upcoming", stringResource(R.string.exams_stat_upcoming), state.upcoming.size.toString(), onClick = { links.open("/exams/upcoming") }),
                        StatItem("results", stringResource(R.string.exams_block_results_title), state.results.size.toString(), onClick = { links.open("/exams/result") }),
                    ) + state.children.map { child ->
                        val avg = state.averageOf(child.id)
                        StatItem(
                            key = child.id,
                            label = child.name,
                            value = avg?.let { ltr("$it%") } ?: "—",
                            hint = average,
                            tone = when {
                                avg == null -> StatTone.Default
                                avg >= 80 -> StatTone.Positive
                                avg >= 50 -> StatTone.Default
                                else -> StatTone.Negative
                            },
                        )
                    },
                )
            }

            TileRow(
                listOf(
                    AppTileItem("upcoming", stringResource(R.string.exams_stat_upcoming), { links.open("/exams/upcoming") }, badge = state.upcoming.size) to {
                        DateTile(weekday = format.weekday(state.today), day = format.day(state.today))
                    },
                    AppTileItem("results", stringResource(R.string.exams_block_results_title), { links.open("/exams/result") }, art = TileArt.Grades) to null,
                    AppTileItem("qbank", stringResource(R.string.exams_block_qbank_title), { links.open("/exams/qbank") }, icon = Icons.AutoMirrored.Outlined.MenuBook, tint = TileTint.Indigo) to null,
                ),
            )

            Column {
                SectionHeader(
                    stringResource(R.string.exams_home_upcoming),
                    linkLabel = stringResource(R.string.exams_home_view_all),
                    onLinkClick = { links.open("/exams/upcoming") },
                )
                if (state.upcoming.isEmpty()) {
                    if (!state.loading) EmptyLine(stringResource(R.string.exams_guardian_no_upcoming))
                } else {
                    ListRows(
                        rows = state.upcoming.map { exam ->
                            {
                                val date = exam.examDate?.let(format::date) ?: state.today
                                val days = ChronoUnit.DAYS.between(state.today, date).toInt()
                                ListRow(
                                    title = exam.title,
                                    art = { DateTile(weekday = format.weekday(date), day = format.day(date)) },
                                    badge = {
                                        // The guardian list says "1 days" where the student's says "Tomorrow" — as the web does.
                                        LabelBadge(whenLabel(days, tomorrowWord = false), variant = if (days == 0) BadgeVariant.Destructive else BadgeVariant.Secondary)
                                    },
                                    description = exam.subject?.takeIf { it.isNotBlank() },
                                    meta = exam.startTime?.let(::ltr),
                                )
                            }
                        },
                    )
                }
            }

            Column {
                SectionHeader(
                    stringResource(R.string.exams_home_recent_results),
                    linkLabel = stringResource(R.string.exams_home_view_all),
                    onLinkClick = { links.open("/exams/result") },
                )
                if (state.results.isEmpty()) {
                    if (!state.loading) EmptyLine(stringResource(R.string.exams_no_results))
                } else {
                    ListRows(
                        divided = true,
                        rows = state.results.map { item ->
                            {
                                val row = item.row
                                ListRow(
                                    title = row.title,
                                    description = listOfNotNull(item.childName.takeIf { it.isNotBlank() }, row.subject?.takeIf { it.isNotBlank() })
                                        .joinToString(" · ").ifEmpty { null },
                                    meta = row.gradedAt?.let { format.shortDate(format.date(it)) },
                                    trailing = { ResultTrailing(row.percentage, row.score, row.maxScore, row.grade) },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
