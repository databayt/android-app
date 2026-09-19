package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.kit.SectionHeader
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

/**
 * The role's chart section — mirrors hogwarts `dashboard/chart-section.tsx`,
 * which sits between the phone quick actions and the resource-usage table.
 *
 * Three cards, in the web's order: the interactive bar chart full width, then
 * the radial and the stacked area, which are a two-column grid from `md` up
 * and a stack on a phone. The student sees the area chart alone — the web
 * hides their bar and radial, and a lone radial-less grid would leave a hole.
 *
 * The numbers are the web's numbers. `generateBarChartData` is reproduced
 * exactly — 91 days keyed off the day of the year, deterministic so the chart
 * does not flicker between renders — and the area series are the same literal
 * tables. Both carry the web's own "TODO: replace with real data": this draws
 * what the web draws, no more.
 */
@Composable
fun ChartSection(
    role: UserRole,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val data = roleChartData(role)
    // The student's chart section is the grades area chart alone.
    val showBar = role != UserRole.STUDENT
    val showRadial = role != UserRole.STUDENT

    Column(modifier.fillMaxWidth()) {
        SectionHeader(stringResource(sectionTitle(role)))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (showBar) BarChartCard(data, today)
            if (showRadial) RadialChartCard(data)
            AreaChartCard(data)
        }
    }
}

// ---------------------------------------------------------------------------
// Cards
// ---------------------------------------------------------------------------

/** `chart-card.tsx` — a bordered card, its title and description at the top. */
@Composable
private fun ChartCard(
    title: String? = null,
    description: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Card)
            .background(colors.card)
            .border(1.dp, colors.border, HogwartsShapes.Card)
            .padding(vertical = 20.dp),
    ) {
        if (title != null) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(title, style = type.section, color = colors.foreground)
                if (description != null) {
                    Text(
                        description,
                        style = type.caption,
                        color = colors.mutedForeground,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        content()
    }
}

/**
 * `chart-interactive-bar.tsx`. On the web the two totals are buttons that swap
 * which series the bars draw; a phone has no hover to reveal that they are
 * switchable, so they read as the totals they are and the bars stay on the
 * primary series — the web's own default.
 */
@Composable
private fun BarChartCard(data: RoleChartData, today: LocalDate) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val series = remember(today) { barSeries(today) }
    val number = tableNumberFormat()

    ChartCard(
        title = stringResource(data.barTitle),
        description = stringResource(data.barDescription),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .border(1.dp, colors.border),
        ) {
            listOf(
                data.barPrimaryLabel to series.sumOf { it.primary },
                data.barSecondaryLabel to series.sumOf { it.secondary },
            ).forEachIndexed { index, (label, total) ->
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(
                        stringResource(label),
                        style = type.caption,
                        color = colors.mutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        number.format(total),
                        style = type.section.copy(fontSize = 22.sp, lineHeight = 28.sp),
                        color = colors.foreground,
                        maxLines = 1,
                    )
                }
                if (index == 0) {
                    Box(Modifier.width(1.dp).height(64.dp).background(colors.border))
                }
            }
        }

        val mark = colors.chart[0]
        // A time axis runs oldest to newest left to right even in Arabic: the
        // web draws these into an SVG in absolute coordinates, so its plots
        // stay LTR inside the RTL page, and the newest day is at the right
        // with the date under it. Mirroring them here would put today on the
        // opposite side from the site.
        Plot {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                val max = series.maxOf { it.primary }.toFloat()
                val slot = size.width / series.size
                val barWidth = slot * 0.7f
                series.forEachIndexed { index, point ->
                    val height = size.height * (point.primary / max)
                    drawRect(
                        color = mark,
                        topLeft = Offset(index * slot + (slot - barWidth) / 2, size.height - height),
                        size = Size(barWidth, height),
                    )
                }
            }

            val formatter = DateTimeFormatter.ofPattern("d MMMM", currentLocale())
            Text(
                today.format(formatter),
                style = type.caption,
                color = colors.mutedForeground,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
        }
    }
}

/**
 * `chart-radial-text.tsx` — one value on a 250° arc, the figure and its label
 * in the middle, the trend underneath.
 */
@Composable
private fun RadialChartCard(data: RoleChartData) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    ChartCard {
        Box(
            Modifier.fillMaxWidth().height(250.dp).padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val track = colors.muted
            val mark = colors.chart[1]
            Canvas(Modifier.fillMaxSize()) {
                val outer = min(size.width, size.height) / 2
                // Recharts: innerRadius 80, outerRadius 110 — the ring is the
                // outer 30/110 of the radius, and the track a thinner band
                // inside it (polarRadius 86..74).
                val ringWidth = outer * 30f / 110f
                val ringRadius = outer - ringWidth / 2
                val trackWidth = outer * 12f / 110f
                val trackRadius = outer * 80f / 110f
                val sweep = -(data.radialValue / data.radialMax * 250f).coerceAtMost(250f)
                val centre = Offset(size.width / 2, size.height / 2)
                fun arc(color: Color, radius: Float, width: Float, degrees: Float) {
                    drawArc(
                        color = color,
                        startAngle = 0f,
                        sweepAngle = degrees,
                        useCenter = false,
                        topLeft = Offset(centre.x - radius, centre.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = width),
                    )
                }
                arc(track, trackRadius, trackWidth, -250f)
                arc(mark, ringRadius, ringWidth, sweep)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    data.radialValue.toInt().toString(),
                    style = type.figure.copy(fontSize = 36.sp, lineHeight = 40.sp),
                    color = colors.foreground,
                )
                Text(
                    stringResource(data.radialLabel),
                    style = type.caption,
                    color = colors.mutedForeground,
                )
            }
        }
        TrendFooter(data.radialTrend, data.radialTrendLabel)
    }
}

/**
 * `chart-area-stacked.tsx` — the secondary series stacked under the primary,
 * both filled at 40% under their own stroke.
 */
@Composable
private fun AreaChartCard(data: RoleChartData) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    ChartCard {
        // The series runs earliest to latest left to right, as on the web — see
        // the note in the bar chart.
        Plot { Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            val primary = colors.chart[0]
            val secondary = colors.chart[1]
            Canvas(Modifier.fillMaxWidth().height(200.dp)) {
                val points = data.areaPoints
                val max = points.maxOf { it.primary + it.secondary }.toFloat()
                val step = if (points.size > 1) size.width / (points.size - 1) else size.width
                fun x(index: Int) = index * step
                fun y(value: Float) = size.height - size.height * (value / max)

                /** One filled band between [lower] and [lower] + its own value. */
                fun band(color: Color, value: (AreaPoint) -> Float, lower: (AreaPoint) -> Float) {
                    val top = Path()
                    points.forEachIndexed { index, point ->
                        val py = y(lower(point) + value(point))
                        if (index == 0) top.moveTo(x(index), py) else top.lineTo(x(index), py)
                    }
                    val fill = Path().apply { addPath(top) }
                    for (index in points.indices.reversed()) {
                        fill.lineTo(x(index), y(lower(points[index])))
                    }
                    fill.close()
                    drawPath(fill, color.copy(alpha = 0.4f))
                    drawPath(top, color, style = Stroke(width = 2.dp.toPx()))
                }

                band(secondary, { it.secondary }, { 0f })
                band(primary, { it.primary }, { it.secondary })
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                data.areaPoints.forEach { point ->
                    Text(
                        areaLabel(point.label),
                        style = type.caption,
                        color = colors.mutedForeground,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } }
        TrendFooter(data.areaTrend, data.areaTrendLabel)
    }
}

/**
 * A plot's own direction. Charts are pictures of an axis, not text: the web
 * hands Recharts absolute SVG coordinates, so its plots read left to right
 * whatever the page direction is. Arabic inside still shapes right to left.
 */
@Composable
private fun Plot(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) { content() }
}

/** "Trending up by 0.5% this month" over the chart's own caption. */
@Composable
private fun TrendFooter(trend: Float, label: Int) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(
                if (trend >= 0) R.string.dash_chart_trend_up else R.string.dash_chart_trend_down,
                formatTrend(abs(trend)),
            ),
            style = type.bodyMedium,
            color = colors.foreground,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(label),
            style = type.caption,
            color = colors.mutedForeground,
            textAlign = TextAlign.Center,
        )
    }
}

/** The web prints the trend with one decimal (`4.5%`, `0.1%`). */
private fun formatTrend(value: Float): String = String.format(Locale.US, "%.1f", value)
