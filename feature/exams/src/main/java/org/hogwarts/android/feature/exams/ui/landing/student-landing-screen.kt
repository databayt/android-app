package org.hogwarts.android.feature.exams.ui.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.BadgeVariant
import org.hogwarts.android.core.designsystem.kit.BrandBanner
import org.hogwarts.android.core.designsystem.kit.BrandPill
import org.hogwarts.android.core.designsystem.kit.DateTile
import org.hogwarts.android.core.designsystem.kit.LabelBadge
import org.hogwarts.android.core.designsystem.kit.ListRow
import org.hogwarts.android.core.designsystem.kit.ListRows
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.TileArt
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.domain.model.Exam
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
internal fun StudentLandingScreen(
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    viewModel: StudentLandingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StudentLandingView(state = state, tabs = tabs, links = links, onRetry = viewModel::load)
}

/**
 * Student view of `/exams` on a phone — the `md:hidden` branch of
 * `exams/student-content.tsx`: the next exam as the green banner, the
 * section's doors as tiles, then upcoming exams and recent results as rows.
 */
@Composable
internal fun StudentLandingView(
    state: StudentLandingUiState,
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    onRetry: () -> Unit,
) {
    val format = examsFormat()
    ExamsPage(tabs = tabs) {
        if (state.noRecord) {
            NoticeCard(
                icon = Icons.Outlined.School,
                title = stringResource(R.string.exams_student_no_record),
                description = stringResource(R.string.exams_contact_admin),
            )
            return@ExamsPage
        }
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            if (state.loadFailed) LoadFailedNote(onRetry)

            fun daysUntil(exam: Exam): Int =
                exam.examDate?.let { ChronoUnit.DAYS.between(state.today, format.date(it)).toInt() } ?: 0

            val next = state.upcoming.firstOrNull()
            if (state.loading && next == null) {
                SkeletonBlock(Modifier.fillMaxWidth().height(232.dp))
            } else {
                val noUpcoming = stringResource(R.string.exams_student_no_upcoming)
                val nextWhen = next?.let { whenLabel(daysUntil(it)) }
                BrandBanner(
                    eyebrow = next?.let { "${stringResource(R.string.exams_upcoming_exams)} · $nextWhen" },
                    headline = buildAnnotatedString {
                        if (next != null) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(next.title) }
                            val line = listOfNotNull(next.subject?.takeIf { it.isNotBlank() }, next.examDate?.let { format.dayMonth(format.date(it)) })
                            if (line.isNotEmpty()) {
                                append("\n")
                                withStyle(SpanStyle(fontSize = 18.sp)) { append(line.joinToString(" · ")) }
                            }
                        } else {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(noUpcoming) }
                        }
                    },
                    actions = {
                        BrandPill(stringResource(R.string.exams_nav_quiz), onClick = { links.open("/exams/quiz") })
                        BrandPill(stringResource(R.string.exams_block_qbank_title), onClick = { links.open("/exams/qbank") }, ghost = true)
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
                    AppTileItem("quiz", stringResource(R.string.exams_nav_quiz), { links.open("/exams/quiz") }, icon = Icons.Outlined.AutoAwesome, tint = TileTint.Orange) to null,
                ),
            )

            if (state.upcoming.isNotEmpty()) {
                Column {
                    SectionHeader(
                        stringResource(R.string.exams_home_my_upcoming),
                        linkLabel = stringResource(R.string.exams_home_view_all),
                        onLinkClick = { links.open("/exams/upcoming") },
                    )
                    val minutes = stringResource(R.string.exams_minutes)
                    ListRows(
                        rows = state.upcoming.map { exam ->
                            {
                                val days = daysUntil(exam)
                                val date = exam.examDate?.let(format::date) ?: state.today
                                ListRow(
                                    title = exam.title,
                                    art = { DateTile(weekday = format.weekday(date), day = format.day(date)) },
                                    badge = {
                                        LabelBadge(whenLabel(days), variant = if (days == 0) BadgeVariant.Destructive else BadgeVariant.Secondary)
                                    },
                                    description = exam.subject?.takeIf { it.isNotBlank() },
                                    meta = exam.startTime?.let { "${ltr(it)} · ${exam.durationMinutes ?: 0} $minutes" },
                                )
                            }
                        },
                    )
                }
            }

            Column {
                SectionHeader(
                    stringResource(R.string.exams_home_my_results),
                    linkLabel = stringResource(R.string.exams_home_view_all),
                    onLinkClick = { links.open("/exams/result") },
                )
                if (state.results.isEmpty()) {
                    if (!state.loading) EmptyLine(stringResource(R.string.exams_no_results))
                } else {
                    ListRows(
                        divided = true,
                        rows = state.results.map { result ->
                            {
                                ListRow(
                                    title = result.title,
                                    description = listOfNotNull(
                                        result.subject?.takeIf { it.isNotBlank() },
                                        result.gradedAt?.let { format.shortDate(format.date(it)) },
                                    ).joinToString(" · ").ifEmpty { null },
                                    trailing = { ResultTrailing(result.percentage, result.score, result.maxScore, result.grade) },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
