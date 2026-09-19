package org.hogwarts.android.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.data.remote.NextActionDto
import org.hogwarts.android.feature.dashboard.ui.components.ChartSection
import org.hogwarts.android.feature.dashboard.ui.components.HomeBlock
import org.hogwarts.android.feature.dashboard.ui.components.NextActionBanner
import org.hogwarts.android.feature.dashboard.ui.components.QuickActions
import org.hogwarts.android.feature.dashboard.ui.components.RoleSections
import org.hogwarts.android.feature.dashboard.ui.components.TodayTimetable

/**
 * The phone dashboard — mirrors hogwarts `dashboard/content.tsx` below `md`:
 * home block, next action, today's classes, quick actions, then the role's
 * own dashboard, 24dp apart on a 16dp gutter. The platform header and Menu
 * come from the app shell.
 *
 * The role's own dashboard is what every role client renders under its quick
 * actions, in the web's order: the chart section, then resource usage, then
 * invoice history. The charts draw the web's own deterministic placeholder
 * figures — `chart-section.tsx` carries a "TODO: Replace with real data"
 * comment — but they are a third of the page the reader sees, so the app
 * draws what the web draws. They sit outside the `sections` block because
 * they need no fetch to render.
 */
@Composable
fun DashboardScreen(
    onOpenHref: (href: String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        state = state,
        onOpenHref = onOpenHref,
        onAcknowledge = viewModel::acknowledge,
        onRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onOpenHref: (href: String) -> Unit,
    onAcknowledge: (NextActionDto) -> Unit,
    onRefresh: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (state.isOffline) {
                Text(stringResource(R.string.dash_offline), style = type.caption, color = colors.mutedForeground)
            }

            HomeBlock(eventsToday = state.data?.eventsToday, onOpen = onOpenHref)

            val data = state.data
            if (data == null) {
                if (!state.isLoading && state.error != null) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.dash_error), style = type.body, color = colors.mutedForeground)
                        Box(Modifier.padding(top = 12.dp)) {
                            PillButton(stringResource(R.string.dash_retry), onClick = onRefresh, variant = PillVariant.Muted)
                        }
                    }
                }
                return@Column
            }

            NextActionBanner(actions = state.nextActions, onOpen = onOpenHref, onAcknowledge = onAcknowledge)
            TodayTimetable(
                timetable = data.todayTimetable,
                role = state.role,
                weekday = state.weekday,
                onOpenTimetable = { onOpenHref("/timetable") },
            )
            QuickActions(actions = data.quickActions, onOpen = onOpenHref)
            ChartSection(role = state.role)
            state.sections?.let { sections ->
                RoleSections(role = state.role, resources = sections.resourceUsage, invoices = sections.invoices)
            }
        }
    }
}
