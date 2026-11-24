package org.hogwarts.android.feature.admin.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.admin.R
import org.hogwarts.android.feature.admin.domain.model.ClassReadiness
import org.hogwarts.android.feature.admin.domain.model.ReadinessLevel
import org.hogwarts.android.feature.admin.domain.model.ReadinessPerson
import org.hogwarts.android.feature.admin.domain.model.ReadinessReason

/**
 * «جاهزية الصفوف» — who in each class is set up to take part in online classes.
 * A list of class cards for a day; a tap opens that class's supervisor,
 * sessions and students. Layout direction follows the app locale (RTL in Arabic).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassReadinessScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClassReadinessViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val selected = state.selected

    // Back from a class's detail returns to the list, not out of the screen.
    BackHandler(enabled = selected != null) { viewModel.select(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selected?.name ?: stringResource(R.string.readiness_title)) },
                navigationIcon = {
                    IconButton(onClick = { if (selected != null) viewModel.select(null) else onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.admin_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            if (selected == null) {
                DateRow(
                    date = state.date,
                    enabled = state.date != null && !state.isLoading,
                    onPrevious = viewModel::previousDay,
                    onNext = viewModel::nextDay,
                    onToday = viewModel::today,
                )
            }
            PullToRefreshBox(
                isRefreshing = state.isLoading && state.classes.isNotEmpty(),
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading && state.classes.isEmpty() -> Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) { CircularProgressIndicator() }

                    state.error != null -> EmptyState(
                        icon = Icons.Default.Info,
                        title = stringResource(
                            when (state.error) {
                                ReadinessError.NotSupervisor -> R.string.readiness_not_supervisor_title
                                ReadinessError.Offline -> R.string.readiness_offline_title
                                else -> R.string.admin_error_title
                            }
                        ),
                        subtitle = stringResource(
                            when (state.error) {
                                ReadinessError.NotSupervisor -> R.string.readiness_not_supervisor_subtitle
                                ReadinessError.Offline -> R.string.readiness_offline_subtitle
                                else -> R.string.readiness_error_subtitle
                            }
                        ),
                        modifier = Modifier.fillMaxSize()
                    )

                    selected != null -> ClassDetail(selected)

                    state.classes.isEmpty() -> EmptyState(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.readiness_empty_title),
                        subtitle = stringResource(R.string.readiness_empty_subtitle),
                        modifier = Modifier.fillMaxSize()
                    )

                    else -> LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.classes, key = { it.sectionId }) { c ->
                            ClassCard(c, onClick = { viewModel.select(c.sectionId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRow(
    date: String?,
    enabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious, enabled = enabled) {
            // Mirrored in RTL: "previous" stays on the reading-start side.
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.readiness_previous_day))
        }
        TextButton(onClick = onToday, enabled = !enabled || date != null) {
            Text(date ?: "—", style = MaterialTheme.typography.titleMedium)
        }
        IconButton(onClick = onNext, enabled = enabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.readiness_next_day))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClassCard(c: ClassReadiness, onClick: () -> Unit) {
    val counts = c.counts
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(c.name, style = MaterialTheme.typography.titleMedium)
            SupervisorLine(c.supervisor)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountPill(stringResource(R.string.readiness_count_on_app, counts.onApp), ReadinessLevel.Ok)
                CountPill(stringResource(R.string.readiness_count_android_web, counts.androidWeb), ReadinessLevel.Warn)
                CountPill(stringResource(R.string.readiness_count_not_attending, counts.notAttending), ReadinessLevel.Bad)
                CountPill(stringResource(R.string.readiness_count_sign_in_trouble, counts.signInTrouble), ReadinessLevel.Bad)
            }
        }
    }
}

@Composable
private fun SupervisorLine(s: ReadinessPerson?) {
    if (s == null) {
        Text(
            stringResource(R.string.readiness_no_supervisor),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.readiness_supervisor_name, s.name),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f, fill = false)
        )
        LevelDot(s.level)
    }
}

@Composable
private fun ClassDetail(c: ClassReadiness) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle(stringResource(R.string.readiness_section_supervisor)) }
        item {
            val s = c.supervisor
            if (s == null) {
                Text(stringResource(R.string.readiness_no_supervisor), color = MaterialTheme.colorScheme.error)
            } else {
                PersonCard(s) {
                    Text(
                        stringResource(if (s.canCover) R.string.readiness_can_cover else R.string.readiness_cannot_cover),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (s.canCover) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        item { SectionTitle(stringResource(R.string.readiness_section_sessions)) }
        items(c.sessions.size) { i ->
            val s = c.sessions[i]
            val teacher = s.teacher
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${s.start}  ${s.subject ?: stringResource(R.string.readiness_no_subject)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (teacher == null) {
                        Text(
                            stringResource(R.string.readiness_no_teacher_login),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(teacher.name, style = MaterialTheme.typography.bodyMedium)
                        Chips(teacher.reasons)
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.readiness_section_students, c.students.size)) }
        items(c.students.size) { i -> PersonCard(c.students[i]) }
    }
}

@Composable
private fun PersonCard(p: ReadinessPerson, extra: @Composable () -> Unit = {}) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(p.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f, fill = false))
                LevelDot(p.level)
            }
            p.username?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            extra()
            Chips(p.reasons)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Chips(reasons: List<ReadinessReason>) {
    if (reasons.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        reasons.forEach { CountPill(stringResource(it.labelRes), it.level) }
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

@Composable
private fun CountPill(text: String, level: ReadinessLevel) {
    val (bg, fg) = levelColors(level)
    Surface(shape = RoundedCornerShape(50), color = bg, contentColor = fg) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LevelDot(level: ReadinessLevel) {
    val (_, fg) = levelColors(level)
    Surface(shape = RoundedCornerShape(50), color = fg, modifier = Modifier.size(8.dp)) {}
}

@Composable
private fun levelColors(level: ReadinessLevel): Pair<Color, Color> = when (level) {
    ReadinessLevel.Ok -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    ReadinessLevel.Warn -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    ReadinessLevel.Bad -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
}
