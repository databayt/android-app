package org.hogwarts.android.feature.timetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.kit.PageNav
import org.hogwarts.android.core.designsystem.kit.PageNavItem
import org.hogwarts.android.core.designsystem.kit.PillButton
import org.hogwarts.android.core.designsystem.kit.PillVariant
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.timetable.R
import org.hogwarts.android.feature.timetable.domain.model.RangeMode
import org.hogwarts.android.feature.timetable.ui.components.GridSkeleton
import org.hogwarts.android.feature.timetable.ui.components.SkeletonBlock
import org.hogwarts.android.feature.timetable.ui.components.SlotSheet
import org.hogwarts.android.feature.timetable.ui.components.rememberJoin

/** What the page can ask for; the screen wires it to [TimetableViewModel], tests pass no-ops. */
data class TimetableActions(
    val onSelectTab: (TimetableTab) -> Unit = {},
    val onPickRange: (RangeMode) -> Unit = {},
    val onSelectChild: (String) -> Unit = {},
    val onFilterClassroom: (String?) -> Unit = {},
    val onFilterSubject: (String?) -> Unit = {},
    val onInspect: (String?) -> Unit = {},
    val onRetry: () -> Unit = {},
    /** A web path: sub-pages with no native screen, live rooms. */
    val onOpenHref: (String) -> Unit = {},
)

/**
 * `/timetable` on a phone. The app shell draws the platform header above it,
 * as the web layout does; the page opens with the section's tabs.
 */
@Composable
fun TimetableScreen(
    onOpenHref: (String) -> Unit,
    viewModel: TimetableViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            viewModel.tick()
        }
    }
    TimetableContent(
        state = state,
        actions = TimetableActions(
            onSelectTab = viewModel::selectTab,
            onPickRange = viewModel::pickRange,
            onSelectChild = viewModel::selectChild,
            onFilterClassroom = viewModel::filterClassroom,
            onFilterSubject = viewModel::filterSubject,
            onInspect = viewModel::inspect,
            onRetry = viewModel::refresh,
            onOpenHref = onOpenHref,
        ),
    )
}

@Composable
fun TimetableContent(state: TimetableUiState, actions: TimetableActions) {
    val colors = HogwartsTheme.colors
    val onJoin = rememberJoin(actions.onOpenHref)
    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 32.dp),
        ) {
            val tabs = state.tabs
            if (tabs.isNotEmpty()) {
                PageNav(
                    items = tabs.map { PageNavItem(it.name, stringResource(it.labelRes())) },
                    selectedKey = state.tab.name,
                    onSelect = { item ->
                        val tab = TimetableTab.valueOf(item.key)
                        val href = tab.href
                        if (href != null) actions.onOpenHref(href) else actions.onSelectTab(tab)
                    },
                )
                Spacer(Modifier.height(24.dp))
            }
            if (state.isOffline) {
                Text(
                    stringResource(R.string.timetable_offline),
                    style = HogwartsTheme.type.caption,
                    color = colors.mutedForeground,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when {
                    state.surface == TimetableSurface.Admin -> AdminView(onOpenHref = actions.onOpenHref)
                    state.failed -> LoadFailed(actions.onRetry)
                    state.surface == TimetableSurface.Guardian -> GuardianView(state, actions, onJoin)
                    state.week == null -> Loading(state.surface)
                    state.surface == TimetableSurface.Teacher -> TeacherView(state, state.week, actions, onJoin)
                    else -> StudentView(state, state.week, actions)
                }
            }
        }
        state.inspectedSlot?.let { slot ->
            SlotSheet(
                slot = slot,
                today = state.week?.today ?: -1,
                nowMinutes = state.nowMinutes,
                onJoin = onJoin,
                onDismiss = { actions.onInspect(null) },
            )
        }
    }
}

@Composable
internal fun Loading(surface: TimetableSurface) {
    if (surface == TimetableSurface.Student) {
        SkeletonBlock(Modifier.fillMaxWidth(0.4f).height(36.dp))
        Spacer(Modifier.height(32.dp))
    } else {
        SkeletonBlock(Modifier.fillMaxWidth().height(96.dp))
    }
    GridSkeleton()
}

@Composable
private fun ColumnScope.LoadFailed(onRetry: () -> Unit) {
    FormAlert(stringResource(R.string.timetable_load_failed))
    Box(Modifier.align(Alignment.CenterHorizontally)) {
        PillButton(stringResource(R.string.timetable_retry), onClick = onRetry, variant = PillVariant.Muted)
    }
}

private fun TimetableTab.labelRes(): Int = when (this) {
    TimetableTab.All -> R.string.timetable_tab_all
    TimetableTab.Analytics -> R.string.timetable_tab_analytics
    TimetableTab.Generate -> R.string.timetable_tab_generate
    TimetableTab.Conflicts -> R.string.timetable_tab_conflicts
    TimetableTab.Settings -> R.string.timetable_tab_settings
    TimetableTab.Today -> R.string.timetable_tab_today
    TimetableTab.Full -> R.string.timetable_tab_full
}
