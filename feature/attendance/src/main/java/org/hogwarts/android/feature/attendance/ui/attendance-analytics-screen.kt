package org.hogwarts.android.feature.attendance.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.feature.attendance.R
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState

/**
 * Advanced Attendance Analytics screen.
 *
 * Displays heatmap, day-of-week patterns, and subject correlation charts
 * using Compose Canvas for custom visualizations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceAnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttendanceAnalyticsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.attendance_analytics_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = HogwartsIcons.Back,
                            contentDescription = stringResource(R.string.attendance_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                EmptyState(
                    icon = HogwartsIcons.Attendance,
                    title = stringResource(R.string.attendance_error_title),
                    subtitle = uiState.error ?: stringResource(R.string.attendance_something_went_wrong),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            uiState.analytics != null -> {
                val analytics = uiState.analytics!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Tab row
                    TabRow(
                        selectedTabIndex = uiState.selectedTab.ordinal
                    ) {
                        AnalyticsTab.entries.forEach { tab ->
                            Tab(
                                selected = uiState.selectedTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                text = {
                                    Text(
                                        text = tab.toDisplayName(
                                            heatmap = stringResource(R.string.attendance_tab_heatmap),
                                            dayPattern = stringResource(R.string.attendance_tab_day_pattern),
                                            subjects = stringResource(R.string.attendance_tab_subjects)
                                        ),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }

                    // Content based on selected tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(AppleSpacing.Standard),
                        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Standard)
                    ) {
                        when (uiState.selectedTab) {
                            AnalyticsTab.HEATMAP -> {
                                Text(
                                    text = stringResource(R.string.attendance_heatmap_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.attendance_heatmap_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HeatmapChart(
                                    data = analytics.heatmapData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                )
                                HeatmapLegend()
                            }

                            AnalyticsTab.DAY_PATTERN -> {
                                Text(
                                    text = stringResource(R.string.attendance_day_pattern_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.attendance_day_pattern_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                DayOfWeekChart(
                                    data = analytics.dayOfWeekPattern,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }

                            AnalyticsTab.SUBJECT_CORRELATION -> {
                                Text(
                                    text = stringResource(R.string.attendance_subject_correlation_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.attendance_subject_correlation_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                SubjectCorrelationChart(
                                    data = analytics.subjectCorrelation,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Heatmap grid using Compose Canvas.
 *
 * Data keys expected as "YYYY-MM-DD", values as 0.0-1.0 attendance rate.
 * Renders a 7-column (days) x N-row (weeks) grid with color-coded cells.
 */
@Composable
private fun HeatmapChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val columns = 7 // days of week
        val rows = (data.size + columns - 1) / columns
        val cellPadding = 2.dp.toPx()
        val cellWidth = (size.width - cellPadding * (columns - 1)) / columns
        val cellHeight = (size.height - cellPadding * (rows - 1)) / rows.coerceAtLeast(1)

        val sortedEntries = data.entries.sortedBy { it.key }

        sortedEntries.forEachIndexed { index, (_, rate) ->
            val col = index % columns
            val row = index / columns
            val x = col * (cellWidth + cellPadding)
            val y = row * (cellHeight + cellPadding)

            val cellColor = when {
                rate >= 0.9f -> primaryColor
                rate >= 0.7f -> primaryColor.copy(alpha = 0.7f)
                rate >= 0.5f -> primaryColor.copy(alpha = 0.4f)
                rate >= 0.3f -> errorColor.copy(alpha = 0.4f)
                rate > 0f -> errorColor.copy(alpha = 0.7f)
                else -> surfaceVariant
            }

            drawRoundRect(
                color = cellColor,
                topLeft = Offset(x, y),
                size = Size(cellWidth, cellHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
private fun HeatmapLegend(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.attendance_legend_less),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(AppleSpacing.Compact))

        listOf(
            surfaceVariant,
            errorColor.copy(alpha = 0.7f),
            errorColor.copy(alpha = 0.4f),
            primaryColor.copy(alpha = 0.4f),
            primaryColor.copy(alpha = 0.7f),
            primaryColor
        ).forEach { color ->
            Canvas(modifier = Modifier.size(16.dp)) {
                drawRoundRect(
                    color = color,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )
            }
            Spacer(modifier = Modifier.size(2.dp))
        }

        Spacer(modifier = Modifier.size(AppleSpacing.Compact))
        Text(
            text = stringResource(R.string.attendance_legend_more),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Bar chart showing attendance rate per day of week.
 */
@Composable
private fun DayOfWeekChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val entries = data.entries.toList()
        val barCount = entries.size
        val barSpacing = 8.dp.toPx()
        val labelHeight = 24.dp.toPx()
        val chartHeight = size.height - labelHeight
        val barWidth = (size.width - barSpacing * (barCount - 1)) / barCount
        val maxValue = entries.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f

        entries.forEachIndexed { index, (label, value) ->
            val x = index * (barWidth + barSpacing)
            val normalizedHeight = (value / maxValue) * chartHeight
            val barTop = chartHeight - normalizedHeight

            // Background bar
            drawRoundRect(
                color = surfaceVariant,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            // Value bar
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, barTop),
                size = Size(barWidth, normalizedHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            // Day label
            drawContext.canvas.nativeCanvas.drawText(
                label.take(3),
                x + barWidth / 2,
                size.height,
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

/**
 * Horizontal bar chart showing attendance rate per subject.
 */
@Composable
private fun SubjectCorrelationChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Compact)
    ) {
        val entries = data.entries.sortedByDescending { it.value }

        entries.forEach { (subject, rate) ->
            Column(
                verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = subject,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "%.0f%%".format(rate * 100),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (rate < 0.7f) errorColor else primaryColor
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    // Track
                    drawRoundRect(
                        color = surfaceVariant,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                    // Bar
                    drawRoundRect(
                        color = if (rate < 0.7f) errorColor else primaryColor,
                        size = Size(size.width * rate, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun AnalyticsTab.toDisplayName(
    heatmap: String,
    dayPattern: String,
    subjects: String
): String = when (this) {
    AnalyticsTab.HEATMAP -> heatmap
    AnalyticsTab.DAY_PATTERN -> dayPattern
    AnalyticsTab.SUBJECT_CORRELATION -> subjects
}
