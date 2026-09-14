package org.hogwarts.android.feature.exams.ui.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.AppTileGrid
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.TileArt
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.feature.exams.R
import org.hogwarts.android.feature.exams.ui.ExamsLinks
import org.hogwarts.android.feature.exams.ui.ExamsPage
import org.hogwarts.android.feature.exams.ui.LoadFailedNote
import org.hogwarts.android.feature.exams.ui.NoticeCard
import org.hogwarts.android.feature.exams.ui.SkeletonBlock

@Composable
internal fun TeacherLandingScreen(
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    viewModel: TeacherLandingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TeacherLandingView(state = state, tabs = tabs, links = links, onRetry = viewModel::load)
}

/**
 * Teacher view of `/exams` on a phone — the `md:hidden` branch of
 * `exams/teacher-content.tsx`: the figures as one grey panel, the four jobs as
 * app tiles. The mobile API scopes neither exams nor questions to a teacher,
 * so those figures read "—" rather than the school's totals.
 */
@Composable
internal fun TeacherLandingView(
    state: TeacherLandingUiState,
    tabs: @Composable () -> Unit,
    links: ExamsLinks,
    onRetry: () -> Unit,
) {
    ExamsPage(tabs = tabs) {
        if (state.noRecord) {
            NoticeCard(
                icon = Icons.Outlined.School,
                title = stringResource(R.string.exams_teacher_no_record),
                description = stringResource(R.string.exams_contact_admin),
            )
            return@ExamsPage
        }
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            if (state.loadFailed) LoadFailedNote(onRetry)
            if (state.loading && state.students == null) {
                SkeletonBlock(Modifier.fillMaxWidth().height(240.dp))
            } else {
                StatPanel(
                    items = listOf(
                        StatItem("exams", stringResource(R.string.exams_stat_total_exams), "—", hint = stringResource(R.string.exams_home_in_my_classes)),
                        StatItem("upcoming", stringResource(R.string.exams_stat_upcoming), "—", onClick = { links.open("/exams/upcoming") }),
                        StatItem("questions", stringResource(R.string.exams_stat_question_bank), "—", onClick = { links.open("/exams/qbank") }),
                        StatItem(
                            "students",
                            stringResource(R.string.exams_stat_students),
                            state.students?.toString() ?: "—",
                            hint = state.classes?.let { stringResource(R.string.exams_home_across_classes, it.toString()) },
                        ),
                        StatItem("completed", stringResource(R.string.exams_stat_completed), "—"),
                        StatItem("marking", stringResource(R.string.exams_stat_pending_marking), "—", onClick = { links.open("/exams/mark") }),
                    ),
                )
            }

            Column {
                SectionHeader(stringResource(R.string.exams_quick_actions))
                AppTileGrid(
                    items = listOf(
                        AppTileItem("question", stringResource(R.string.exams_add_question), { links.open("/exams/qbank/new") }, icon = Icons.AutoMirrored.Outlined.MenuBook, tint = TileTint.Indigo),
                        AppTileItem("generate", stringResource(R.string.exams_block_generate_title), { links.open("/exams/generate") }, icon = Icons.Outlined.AutoAwesome, tint = TileTint.Purple),
                        AppTileItem("mark", stringResource(R.string.exams_block_mark_title), { links.open("/exams/mark") }, icon = Icons.Outlined.AssignmentTurnedIn, tint = TileTint.Orange),
                        AppTileItem("results", stringResource(R.string.exams_block_results_title), { links.open("/exams/result") }, art = TileArt.Grades),
                    ),
                )
            }
        }
    }
}
