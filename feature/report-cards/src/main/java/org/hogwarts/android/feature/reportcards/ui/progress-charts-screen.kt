package org.hogwarts.android.feature.reportcards.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.reportcards.R

/**
 * Progress charts screen.
 *
 * Displays GPA trend line chart across terms and
 * per-subject performance bars computed from all published report cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressChartsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgressChartsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_cards_progress_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.report_cards_back))
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: stringResource(R.string.report_cards_something_went_wrong),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.report_cards_tap_to_retry),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = AppleSpacing.Compact)
                                .clickable { viewModel.retry() }
                        )
                    }
                }
            }
            uiState.gpaTrend.isEmpty() && uiState.subjectPerformances.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = HogwartsIcons.Grades,
                        title = stringResource(R.string.report_cards_no_progress_title),
                        subtitle = stringResource(R.string.report_cards_no_progress_subtitle)
                    )
                }
            }
            else -> {
                ProgressChartsContent(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ProgressChartsContent(
    uiState: ProgressChartsUiState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    AppleInsetGroupedList(
        modifier = modifier,
        state = listState
    ) {
        // Overall GPA summary
        if (uiState.overallGpa != null) {
            item {
                AppleListSection(header = stringResource(R.string.report_cards_current_gpa)) {
                    AppleListRow(showDivider = false) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%.2f".format(uiState.overallGpa),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (uiState.gpaChange != null) {
                                val isPositive = uiState.gpaChange >= 0
                                Text(
                                    text = "${if (isPositive) "+" else ""}${"%.2f".format(uiState.gpaChange)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPositive) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // GPA Trend Line Chart
        if (uiState.gpaTrend.size >= 2) {
            item {
                AppleListSection(header = stringResource(R.string.report_cards_gpa_trend)) {
                    AppleListRow(showDivider = false) {
                        GpaTrendChart(
                            dataPoints = uiState.gpaTrend,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }

        // Per-Subject Performance Bars
        if (uiState.subjectPerformances.isNotEmpty()) {
            item {
                AppleListSection(header = stringResource(R.string.report_cards_subject_performance)) {
                    uiState.subjectPerformances.forEachIndexed { index, subject ->
                        AppleListRow(
                            showDivider = index < uiState.subjectPerformances.size - 1
                        ) {
                            SubjectPerformanceBar(performance = subject)
                        }
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(AppleSpacing.Comfortable))
        }
    }
}

/**
 * GPA trend line chart drawn with Compose Canvas.
 *
 * Draws a smooth line connecting GPA values across terms,
 * with dots at each data point and labels along the X axis.
 */
@Composable
private fun GpaTrendChart(
    dataPoints: List<GpaTrendPoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = labelColor,
        textAlign = TextAlign.Center
    )
    val valueStyle = TextStyle(
        fontSize = 10.sp,
        color = dotColor,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )

    Canvas(modifier = modifier.padding(top = 16.dp, bottom = 8.dp)) {
        if (dataPoints.size < 2) return@Canvas

        val gpas = dataPoints.map { it.gpa }
        val minGpa = (gpas.min() - 0.5f).coerceAtLeast(0f)
        val maxGpa = (gpas.max() + 0.3f).coerceAtMost(5f)
        val gpaRange = (maxGpa - minGpa).coerceAtLeast(0.1f)

        val paddingLeft = 40.dp.toPx()
        val paddingRight = 20.dp.toPx()
        val paddingTop = 20.dp.toPx()
        val paddingBottom = 40.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        // Draw horizontal grid lines
        val gridLineCount = 4
        for (i in 0..gridLineCount) {
            val y = paddingTop + chartHeight * (1f - i.toFloat() / gridLineCount)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )

            // Y-axis labels
            val gpaValue = minGpa + gpaRange * (i.toFloat() / gridLineCount)
            val labelText = "%.1f".format(gpaValue)
            val measured = textMeasurer.measure(labelText, labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = paddingLeft - measured.size.width - 6.dp.toPx(),
                    y = y - measured.size.height / 2f
                )
            )
        }

        // Compute point positions
        val points = dataPoints.mapIndexed { index, point ->
            val x = paddingLeft + chartWidth * index / (dataPoints.size - 1).toFloat()
            val y = paddingTop + chartHeight * (1f - (point.gpa - minGpa) / gpaRange)
            Offset(x, y)
        }

        // Draw the line path
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw dots and value labels at each data point
        points.forEachIndexed { index, point ->
            // Outer dot
            drawCircle(
                color = dotColor,
                radius = 5.dp.toPx(),
                center = point
            )
            // Inner white dot
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = point
            )

            // GPA value above the dot
            val valueText = "%.2f".format(dataPoints[index].gpa)
            val valueMeasured = textMeasurer.measure(valueText, valueStyle)
            drawText(
                textLayoutResult = valueMeasured,
                topLeft = Offset(
                    x = point.x - valueMeasured.size.width / 2f,
                    y = point.y - valueMeasured.size.height - 6.dp.toPx()
                )
            )

            // X-axis label (term name - use first line only for space)
            val xLabel = dataPoints[index].label.lines().firstOrNull() ?: ""
            val xMeasured = textMeasurer.measure(xLabel, labelStyle)
            drawText(
                textLayoutResult = xMeasured,
                topLeft = Offset(
                    x = point.x - xMeasured.size.width / 2f,
                    y = paddingTop + chartHeight + 8.dp.toPx()
                )
            )
        }
    }
}

/**
 * Single subject performance bar with label, percentage, and grade.
 */
@Composable
private fun SubjectPerformanceBar(
    performance: SubjectPerformance,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        performance.percentage >= 85 -> MaterialTheme.colorScheme.primary
        performance.percentage >= 70 -> MaterialTheme.colorScheme.secondary
        performance.percentage >= 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppleSpacing.Compact)
    ) {
        // Subject name and grade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = performance.subjectName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Compact),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = performance.latestGrade,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = barColor
                )
                Text(
                    text = "${"%.0f".format(performance.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { (performance.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

// --- Previews ---

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "RTL", locale = "ar")
@Composable
private fun ProgressChartsPreview() {
    HogwartsTheme {
        ProgressChartsContent(
            uiState = ProgressChartsUiState(
                isLoading = false,
                overallGpa = 3.92f,
                gpaChange = 0.07f,
                gpaTrend = listOf(
                    GpaTrendPoint(label = "Term 1\n2024-2025", gpa = 3.50f),
                    GpaTrendPoint(label = "Term 2\n2024-2025", gpa = 3.70f),
                    GpaTrendPoint(label = "Term 1\n2025-2026", gpa = 3.85f),
                    GpaTrendPoint(label = "Term 2\n2025-2026", gpa = 3.92f)
                ),
                subjectPerformances = listOf(
                    SubjectPerformance(
                        subjectName = "Defence Against Dark Arts",
                        percentage = 95f,
                        latestGrade = "A+"
                    ),
                    SubjectPerformance(
                        subjectName = "Mathematics",
                        percentage = 90f,
                        latestGrade = "A+"
                    ),
                    SubjectPerformance(
                        subjectName = "Potions",
                        percentage = 78f,
                        latestGrade = "B+"
                    ),
                    SubjectPerformance(
                        subjectName = "History of Magic",
                        percentage = 65f,
                        latestGrade = "C+"
                    ),
                    SubjectPerformance(
                        subjectName = "Herbology",
                        percentage = 42f,
                        latestGrade = "D"
                    )
                )
            )
        )
    }
}

@Preview(name = "Single Term (No Trend)")
@Composable
private fun ProgressChartsSingleTermPreview() {
    HogwartsTheme {
        ProgressChartsContent(
            uiState = ProgressChartsUiState(
                isLoading = false,
                overallGpa = 3.85f,
                gpaChange = null,
                gpaTrend = listOf(
                    GpaTrendPoint(label = "Term 1\n2025-2026", gpa = 3.85f)
                ),
                subjectPerformances = listOf(
                    SubjectPerformance(
                        subjectName = "Mathematics",
                        percentage = 92f,
                        latestGrade = "A+"
                    ),
                    SubjectPerformance(
                        subjectName = "Potions",
                        percentage = 78f,
                        latestGrade = "B+"
                    )
                )
            )
        )
    }
}

@Preview(name = "Empty State")
@Composable
private fun ProgressChartsEmptyPreview() {
    HogwartsTheme {
        ProgressChartsContent(
            uiState = ProgressChartsUiState(
                isLoading = false,
                gpaTrend = emptyList(),
                subjectPerformances = emptyList()
            )
        )
    }
}
