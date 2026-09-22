package org.hogwarts.android.feature.subjects.ui

import org.hogwarts.android.core.designsystem.kit.ReportIssueFooter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.SubjectLevel
import org.hogwarts.android.feature.subjects.ui.components.SubjectCard
import org.hogwarts.android.feature.subjects.ui.components.gradeLabel

/**
 * `/subjects` — the school's subjects, as the web lays them out: the page's
 * own heading, the tab strip under it when the reader has more than one tab,
 * then a two-column grid of cards. No search: the web page has none.
 *
 * No top app bar. The platform header and its Menu are the chrome on every
 * phone screen in this app, and a second bar under them was a wave-one
 * holdover that the web has never had.
 *
 * The strip's first four entries filter the grid in place, the way the web's
 * `/subjects/elementary|middle|high` pages narrow the same list. The catalog
 * and contribute entries are separate web pages with no native mirror, so
 * they hand off to the site rather than pretend to be tabs that do nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubject: (String) -> Unit,
    onOpenHref: (String) -> Unit = {},
    role: UserRole? = null,
    viewModel: SubjectsListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val colors = HogwartsTheme.colors
    var level by rememberSaveable { mutableStateOf<SubjectLevel?>(null) }

    val shown = remember(uiState.subjects, level) {
        if (level == null) uiState.subjects else uiState.subjects.filter { level in it.levels }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "heading", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.subjects_title),
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.foreground,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            val tabs = subjectsTabs(role, uiState.schoolLevels)
            // With only "All" left the web draws no strip at all — one tab is
            // not navigation. That is a student's case: every other entry is
            // hidden from them, and their list is already only their grade.
            if (tabs.size > 1) item(key = "levels", span = { GridItemSpan(maxLineSpan) }) {
                LevelStrip(
                    tabs = tabs,
                    selected = level,
                    onSelect = { level = it },
                    onOpenHref = onOpenHref,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            if (uiState.isLoading && uiState.subjects.isEmpty()) {
                item(key = "loading", span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
            } else if (shown.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.subjects_no_subjects_found),
                            style = HogwartsTheme.type.body,
                            color = colors.mutedForeground,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                items(items = shown, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        gradeLabel = gradeLabel(subject.grades, isArabic),
                        onClick = { onNavigateToSubject(subject.id) },
                    )
                }
            }

            item(key = "report", span = { GridItemSpan(maxLineSpan) }) {
                ReportIssueFooter(pagePath = "/subjects")
            }

            if (uiState.error != null) {
                item(key = "error", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = uiState.error ?: "",
                        style = HogwartsTheme.type.caption,
                        color = colors.destructive,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

/** One entry in the strip: a level that filters in place, or a web page. */
private sealed interface SubjectsTab {
    data class Level(val level: SubjectLevel?, val label: Int) : SubjectsTab
    data class Page(val href: String, val label: Int) : SubjectsTab
}

/**
 * The entries `(browse)/layout.tsx` shows this reader, in its order.
 *
 * - Elementary / Middle / High: never for a student, and only when the school
 *   runs two or more stages — with one stage, "All" already is that stage.
 * - Catalog: admins and developers only.
 * - Contribute / My Contributions: everyone but students.
 */
private fun subjectsTabs(role: UserRole?, schoolLevels: Set<SubjectLevel>): List<SubjectsTab> {
    val isStudent = role == UserRole.STUDENT
    val isAdmin = role == UserRole.ADMIN || role == UserRole.DEVELOPER
    val showLevels = !isStudent && schoolLevels.size >= 2
    return buildList {
        add(SubjectsTab.Level(null, R.string.subjects_filter_all))
        if (showLevels) {
            listOf(
                SubjectLevel.ELEMENTARY to R.string.subjects_level_elementary,
                SubjectLevel.MIDDLE to R.string.subjects_level_middle,
                SubjectLevel.HIGH to R.string.subjects_level_high,
            ).forEach { (level, label) ->
                if (level in schoolLevels) add(SubjectsTab.Level(level, label))
            }
        }
        if (isAdmin) add(SubjectsTab.Page("/subjects/catalog", R.string.subjects_filter_catalog))
        if (!isStudent) {
            add(SubjectsTab.Page("/subjects/contribute", R.string.subjects_filter_contribute))
            add(SubjectsTab.Page("/subjects/contributions", R.string.subjects_filter_my_contributions))
        }
    }
}

/**
 * The web's strip. 14sp medium, muted until chosen, the chosen one in the
 * foreground over a 2dp rule — the underline the site draws under the page
 * you are on. Level entries filter the grid in place, as the web's
 * `/subjects/elementary|middle|high` pages narrow the same list; the rest are
 * separate web pages with no native mirror, so they hand off to the site.
 */
@Composable
private fun LevelStrip(
    tabs: List<SubjectsTab>,
    selected: SubjectLevel?,
    onSelect: (SubjectLevel?) -> Unit,
    onOpenHref: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        tabs.forEach { tab ->
            when (tab) {
                is SubjectsTab.Level -> StripEntry(
                    label = stringResource(tab.label),
                    active = selected == tab.level,
                    onClick = { onSelect(tab.level) },
                )
                is SubjectsTab.Page -> StripEntry(
                    label = stringResource(tab.label),
                    active = false,
                    onClick = { onOpenHref(tab.href) },
                )
            }
        }
    }
}

@Composable
private fun StripEntry(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(role = Role.Tab, onClick = onClick),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (active) colors.foreground else colors.mutedForeground,
            maxLines = 1,
        )
        Box(
            Modifier
                .padding(top = 6.dp)
                .height(2.dp)
                .fillMaxWidth()
                .background(if (active) colors.foreground else Color.Transparent),
        )
    }
}
