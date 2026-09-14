package org.hogwarts.android.feature.attendance.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.AppTileGrid
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.kit.StatItem
import org.hogwarts.android.core.designsystem.kit.StatPanel
import org.hogwarts.android.core.designsystem.kit.StatTone
import org.hogwarts.android.core.designsystem.kit.TileTint
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.feature.attendance.ui.AttendancePage
import org.hogwarts.android.feature.attendance.ui.AttendanceTile
import org.hogwarts.android.feature.attendance.ui.attendanceFormat
import org.hogwarts.android.feature.attendance.ui.components.LoadFailedNote

@Composable
fun StaffOverviewScreen(
    nav: @Composable () -> Unit,
    onNavigate: (Any) -> Unit,
    viewModel: StaffOverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StaffOverviewView(state = state, nav = nav, onOpenTile = { onNavigate(it.route) }, onRetry = viewModel::load)
}

/**
 * Admin / staff view of `/attendance` — the phone branch of
 * `attendance/overview/content.tsx`: today's figures as one grey panel, then
 * quick access as home-screen tiles.
 */
@Composable
fun StaffOverviewView(
    state: StaffOverviewUiState,
    nav: @Composable () -> Unit,
    onOpenTile: (AttendanceTile) -> Unit,
    onRetry: () -> Unit,
) {
    val format = attendanceFormat()
    AttendancePage(nav = nav) {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            if (state.loadFailed && state.totals == null) LoadFailedNote(onRetry)

            val totals = state.totals
            val pending = state.loading && totals == null
            fun figure(value: Int?) = if (pending || value == null) "—" else format.count(value)
            StatPanel(
                items = listOf(
                    StatItem("present", stringResource(R.string.attendance_stat_present), figure(totals?.present), tone = StatTone.Positive),
                    StatItem(
                        "absent",
                        stringResource(R.string.attendance_stat_absent),
                        figure(totals?.absent),
                        tone = if ((totals?.absent ?: 0) > 0) StatTone.Negative else StatTone.Default,
                    ),
                    StatItem(
                        "late",
                        stringResource(R.string.attendance_stat_late),
                        figure(totals?.late),
                        tone = if ((totals?.late ?: 0) > 0) StatTone.Warning else StatTone.Default,
                    ),
                    StatItem("rate", stringResource(R.string.attendance_stat_rate), if (pending || totals == null) "—" else format.percent(totals.rate)),
                ),
            )

            Column {
                SectionHeader(stringResource(R.string.attendance_quick_access))
                AppTileGrid(
                    items = buildList {
                        add(AppTileItem(AttendanceTile.HallPass.name, stringResource(R.string.attendance_tile_hall_pass), { onOpenTile(AttendanceTile.HallPass) }, icon = Icons.Outlined.MeetingRoom, tint = TileTint.Orange))
                        add(AppTileItem(AttendanceTile.Gamification.name, stringResource(R.string.attendance_tile_gamification), { onOpenTile(AttendanceTile.Gamification) }, icon = Icons.Outlined.EmojiEvents, tint = TileTint.Yellow))
                        if (state.isAdmin) {
                            add(AppTileItem(AttendanceTile.Kiosk.name, stringResource(R.string.attendance_tile_kiosk), { onOpenTile(AttendanceTile.Kiosk) }, icon = Icons.Outlined.HowToReg, tint = TileTint.Green))
                        }
                    },
                )
            }
        }
    }
}
