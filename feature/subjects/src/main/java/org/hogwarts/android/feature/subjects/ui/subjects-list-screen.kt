package org.hogwarts.android.feature.subjects.ui

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
import org.hogwarts.android.core.designsystem.atom.HogwartsSearchBar
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.SubjectLevel
import org.hogwarts.android.feature.subjects.ui.components.SubjectCard
import org.hogwarts.android.feature.subjects.ui.components.gradeLabel
import org.hogwarts.android.feature.subjects.ui.components.levelLabel

/**
 * `/subjects` — the school's subjects, as the web lays them out: the page's
 * own heading, the level strip under it, then a two-column grid of cards.
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

            // The web shows a student no strip at all: Elementary/Middle/High
            // are hidden from them, and with only "All" left it drops the nav
            // bar rather than draw one tab. Catalog and Contribute are worse
            // than decorative here — both open pages whose server actions
            // refuse a student outright. A student's list is already only
            // their own grade, so there is nothing left to filter by.
            if (role != UserRole.STUDENT) item(key = "levels", span = { GridItemSpan(maxLineSpan) }) {
                LevelStrip(
                    selected = level,
                    onSelect = { level = it },
                    onOpenHref = onOpenHref,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
                HogwartsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    placeholder = stringResource(R.string.subjects_search_placeholder),
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
                        levelLabel = levelLabel(subject.primaryLevel),
                        gradeLabel = gradeLabel(subject.grades, isArabic),
                        onClick = { onNavigateToSubject(subject.id) },
                    )
                }
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

/**
 * The web's strip: الكل · ابتدائي · متوسط · ثانوي · الكتالوج · المساهمة.
 * 14sp medium, muted until chosen, the chosen one in the foreground over a
 * 2dp rule — the underline the site draws under the page you are on.
 */
@Composable
private fun LevelStrip(
    selected: SubjectLevel?,
    onSelect: (SubjectLevel?) -> Unit,
    onOpenHref: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val filters = listOf(
            null to R.string.subjects_filter_all,
            SubjectLevel.ELEMENTARY to R.string.subjects_level_elementary,
            SubjectLevel.MIDDLE to R.string.subjects_level_middle,
            SubjectLevel.HIGH to R.string.subjects_level_high,
        )
        filters.forEach { (value, label) ->
            StripEntry(
                label = stringResource(label),
                active = selected == value,
                onClick = { onSelect(value) },
            )
        }
        StripEntry(
            label = stringResource(R.string.subjects_filter_catalog),
            active = false,
            onClick = { onOpenHref("/subjects/catalog") },
        )
        StripEntry(
            label = stringResource(R.string.subjects_filter_contribute),
            active = false,
            onClick = { onOpenHref("/subjects/contribute") },
        )
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
