package org.hogwarts.android.feature.admin.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.admin.R
import org.hogwarts.android.feature.admin.domain.model.AttentionPerson
import org.hogwarts.android.feature.admin.domain.model.ReadinessLevel
import org.hogwarts.android.feature.admin.domain.model.SchoolAnalysis
import org.hogwarts.android.feature.admin.domain.model.barFraction
import org.hogwarts.android.feature.admin.domain.model.percentText
import org.hogwarts.android.feature.admin.domain.model.ratioText
import org.hogwarts.android.feature.admin.domain.model.roleLabelRes
import org.hogwarts.android.feature.admin.domain.model.shortDate
import org.hogwarts.android.feature.admin.domain.model.slotIndex

/**
 * «التحليل» — the school's progress report: four KPIs, 7-day class attendance,
 * how people join classes, and who needs a follow-up. Admins only (a 403 shows
 * a friendly state). Charts are Compose Canvas; layout direction follows the locale.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolAnalysisScreen(
    onNavigateBack: () -> Unit,
    onOpenReadiness: () -> Unit,
    viewModel: SchoolAnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val analysis = state.analysis

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analysis_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.admin_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading && analysis != null,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when {
                state.isLoading && analysis == null -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }

                state.error != null -> EmptyState(
                    icon = Icons.Default.Info,
                    title = stringResource(
                        when (state.error) {
                            AnalysisError.Forbidden -> R.string.analysis_forbidden_title
                            AnalysisError.Offline -> R.string.readiness_offline_title
                            else -> R.string.admin_error_title
                        }
                    ),
                    subtitle = stringResource(
                        when (state.error) {
                            AnalysisError.Forbidden -> R.string.analysis_forbidden_subtitle
                            AnalysisError.Offline -> R.string.readiness_offline_subtitle
                            else -> R.string.analysis_error_subtitle
                        }
                    ),
                    modifier = Modifier.fillMaxSize()
                )

                analysis != null -> AnalysisContent(analysis, onOpenReadiness)
            }
        }
    }
}

@Composable
private fun AnalysisContent(a: SchoolAnalysis, onOpenReadiness: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(stringResource(R.string.analysis_kpi_attendance), percentText(a.attendanceRate14d), Modifier.weight(1f))
                    KpiCard(stringResource(R.string.analysis_kpi_readiness), percentText(a.readinessPct), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(stringResource(R.string.analysis_kpi_on_app), ratioText(a.counts.onApp, a.counts.members), Modifier.weight(1f))
                    KpiCard(stringResource(R.string.analysis_kpi_push_on), ratioText(a.counts.pushOn, a.counts.members), Modifier.weight(1f))
                }
            }
        }

        item {
            ChartCard(stringResource(R.string.analysis_attendance_title)) {
                if (!a.hasAttendanceData) {
                    EmptyChart()
                } else {
                    val max = a.attendanceByDay.maxOfOrNull { maxOf(it.expected, it.joined) } ?: 0
                    val expectedColor = MaterialTheme.colorScheme.surfaceVariant
                    val joinedColor = MaterialTheme.colorScheme.primary
                    BarChart(
                        dates = a.attendanceByDay.map { it.date },
                        stacks = a.attendanceByDay.map { listOf(it.expected, it.joined) },
                        colors = listOf(expectedColor, joinedColor),
                        scaleMax = max,
                        overlap = true,
                    )
                    Legend(
                        listOf(
                            joinedColor to stringResource(R.string.analysis_legend_joined),
                            expectedColor to stringResource(R.string.analysis_legend_expected),
                        )
                    )
                }
            }
        }

        item {
            ChartCard(stringResource(R.string.analysis_joins_title)) {
                if (!a.hasJoinData) {
                    EmptyChart()
                } else {
                    val appColor = MaterialTheme.colorScheme.primary
                    val webColor = MaterialTheme.colorScheme.tertiary
                    val computerColor = MaterialTheme.colorScheme.secondary
                    BarChart(
                        dates = a.joinsByDay.map { it.date },
                        stacks = a.joinsByDay.map { listOf(it.app, it.androidWeb, it.computer) },
                        colors = listOf(appColor, webColor, computerColor),
                        scaleMax = a.joinsByDay.maxOfOrNull { it.total } ?: 0,
                        overlap = false,
                    )
                    Legend(
                        listOf(
                            appColor to stringResource(R.string.readiness_on_app),
                            webColor to stringResource(R.string.readiness_android_web),
                            computerColor to stringResource(R.string.readiness_allowed_web),
                        )
                    )
                }
            }
        }

        item { SectionTitle(stringResource(R.string.analysis_attention_title)) }
        if (a.needsAttention.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.analysis_attention_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(a.needsAttention.size) { i -> AttentionCard(a.needsAttention[i]) }
        }

        item {
            Card(
                Modifier.fillMaxWidth().clickable(onClick = onOpenReadiness),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.readiness_title), style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(R.string.readiness_menu_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Auto-mirrored: points toward the end of the row in both directions.
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

@Composable
private fun EmptyChart() {
    Text(
        stringResource(R.string.analysis_chart_empty),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Bars on a Canvas. [stacks] holds one list of values per day, one entry per colour. With [overlap]
 * the values are drawn over each other from the baseline (expected behind, joined in front); otherwise
 * they are stacked. Heights scale to [scaleMax]; zero is safe. Labels sit in a Row beneath, which
 * mirrors in RTL, so the Canvas slots are mirrored by hand to match.
 */
@Composable
private fun BarChart(
    dates: List<String>,
    stacks: List<List<Int>>,
    colors: List<Color>,
    scaleMax: Int,
    overlap: Boolean,
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val n = dates.size
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        if (n == 0) return@Canvas
        val slotW = size.width / n
        val barW = slotW * 0.6f
        for (i in 0 until n) {
            val left = slotIndex(i, n, rtl) * slotW + (slotW - barW) / 2f
            var top = size.height
            stacks[i].forEachIndexed { k, v ->
                val h = size.height * barFraction(v, scaleMax)
                if (h <= 0f) return@forEachIndexed
                val y = if (overlap) size.height - h else top - h
                drawRoundRect(
                    color = colors[k],
                    topLeft = Offset(left, y),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                if (!overlap) top -= h
            }
        }
    }
    Row(Modifier.fillMaxWidth()) {
        dates.forEach {
            Text(
                shortDate(it),
                Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend(items: List<Pair<Color, String>>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(10.dp)) {
                    Surface(shape = RoundedCornerShape(2.dp), color = color, modifier = Modifier.size(10.dp)) {}
                }
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttentionCard(p: AttentionPerson) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(p.name, style = MaterialTheme.typography.titleSmall)
            val role = roleLabelRes(p.role)?.let { stringResource(it) } ?: p.role
            Text(
                listOfNotNull(role.takeIf { it.isNotBlank() }, p.sectionName?.takeIf { it.isNotBlank() }).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (p.reasons.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    p.reasons.forEach { ReasonChip(stringResource(it.labelRes), it.level) }
                }
            }
        }
    }
}

@Composable
private fun ReasonChip(text: String, level: ReadinessLevel) {
    val (bg, fg) = when (level) {
        ReadinessLevel.Ok -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ReadinessLevel.Warn -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ReadinessLevel.Bad -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(shape = RoundedCornerShape(50), color = bg, contentColor = fg) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
