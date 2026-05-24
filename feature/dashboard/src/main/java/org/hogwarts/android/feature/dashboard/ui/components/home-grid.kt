package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.atom.BadgedAppIcon
import org.hogwarts.android.core.designsystem.atom.SmartStackIndicator
import org.hogwarts.android.core.designsystem.atom.SmartStackWidget
import org.hogwarts.android.core.designsystem.atom.iosTileBrush
import org.hogwarts.android.core.designsystem.atom.rememberSmartStackPagerState
import org.hogwarts.android.feature.dashboard.R
import org.hogwarts.android.feature.dashboard.ui.DashboardUiState

/**
 * iOS 26-style home grid: a 2x2 Smart Stack widget anchored to the top-left, a 2x2
 * cluster of tiles to its right, and three full-width rows of tiles below.
 *
 * Total tile slots: 16. The first 4 entries from [tiles] fill the top-right cluster
 * (rows 1–2, columns 3–4). The last 4 entries flow into row 5, which is where the
 * tiles displaced by the widget land.
 *
 * Spacing: 14dp column gap, 20dp row gap, 24dp horizontal padding — matches the
 * pre-widget grid so the dock and search pill keep their existing alignment.
 */
@Composable
fun HomeGrid(
    tiles: List<HomeTileSpec>,
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val rowGap = 20.dp
    val colGap = 14.dp

    val widgetPages = listOf<@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit>(
        { TodayWidgetPage(state) },
        { GlanceWidgetPage(state) },
        { UpNextWidgetPage(state) }
    )
    // Pager state hoisted so the floating indicator overlay can read currentPage and
    // isScrollInProgress from the same source as the pager itself.
    val pagerState = rememberSmartStackPagerState(widgetPages.size)

    // Defensive split — if the caller passes fewer tiles than expected, missing
    // slots are just empty cells rather than a crash. Anything past the first
    // four 4-tile bands flows into chunked overflow rows so callers can append
    // additional blocks without touching the grid layout.
    val topRight = tiles.take(4)
    val mid1 = tiles.drop(4).take(4)
    val mid2 = tiles.drop(8).take(4)
    val bottom = tiles.drop(12).take(4)
    val overflow = tiles.drop(16).chunked(4)

    // One short label per page in [widgetPages] — same order as the pages list.
    // Driven by [pagerState.currentPage] so the label below the widget changes as the
    // user swipes through the stack, mirroring how each tile in the cluster has its
    // own static label below the icon.
    val widgetPageLabels = listOf(
        R.string.home_widget_label_today,
        R.string.home_widget_label_glance,
        R.string.home_widget_label_activity
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(rowGap)
    ) {
        // Top block: a 2x2 tile cluster on the start, a widget column on the end. Two
        // equal-width slots plus one [colGap] exactly match two cells + one gap from
        // the 4-up rows below — the cluster's cells align with the bottom-row cells and
        // the widget's slot aligns with columns 3–4.
        //
        // The widget column mirrors an [AppIcon]'s structure: a surface (icon analogue)
        // on top, a 5dp gap, then a single label at the bottom. Because the row's
        // vertical extent is driven by the icon column (two 64dp icons + their 13sp
        // labels + 20dp inter-row gap), placing the widget's label at the column bottom
        // lines it up with the labels of the cluster's second-row tiles (Stream,
        // Subjects). The label text is variable — it changes with [pagerState.currentPage]
        // so each page in the stack identifies itself like a regular tile would.
        //
        // The surface is inset horizontally by [widgetInset] = (cellWidth − 64dp) / 2,
        // computed from the actual slot width via [BoxWithConstraints]. This is the same
        // empty-space gap each 64dp icon has inside its weight=1 cell, so the surface's
        // left/right edges sit exactly above (and below) the icons in cells 1–2 and
        // cells 3–4 across the rest of the grid. The floating indicator clears the same
        // inset so it keeps hugging the surface's visible trailing edge.
        //
        // [TopBlockLayout] is a custom layout (not Row + IntrinsicSize.Min) because
        // [SmartStackWidget] wraps a [VerticalPager] — a SubcomposeLayout that can't
        // answer intrinsic queries. The layout measures the icon column at its natural
        // height first, then forces the widget column to that exact size.
        TopBlockLayout(
            modifier = Modifier.fillMaxWidth(),
            horizontalGap = colGap,
            iconColumn = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(rowGap)
                ) {
                    TileRow(topRight.take(2), colGap)
                    TileRow(topRight.drop(2).take(2), colGap)
                }
            },
            widget = {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val cellWidth = (maxWidth - colGap) / 2
                    val widgetInset = ((cellWidth - 64.dp) / 2).coerceAtLeast(0.dp)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = widgetInset)
                        ) {
                            SmartStackWidget(
                                pages = widgetPages,
                                pagerState = pagerState,
                                aspectRatio = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            SmartStackIndicator(
                                pageCount = widgetPages.size,
                                pagerState = pagerState,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 6.dp)
                            )
                        }

                        Text(
                            text = stringResource(
                                widgetPageLabels[
                                    pagerState.currentPage.coerceIn(0, widgetPageLabels.lastIndex)
                                ]
                            ),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        )

        FullTileRow(mid1, colGap)
        FullTileRow(mid2, colGap)
        FullTileRow(bottom, colGap)
        overflow.forEach { row ->
            FullTileRow(row, colGap)
        }

        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Two equal-width slots side-by-side: an icon column and a widget. The row's height is
 * driven by the icon column's natural measurement, then the widget is sized to match.
 * Avoids `IntrinsicSize.Min` so children can include SubcomposeLayouts (e.g. pagers).
 */
@Composable
private fun TopBlockLayout(
    iconColumn: @Composable () -> Unit,
    widget: @Composable () -> Unit,
    horizontalGap: Dp,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            iconColumn()
            widget()
        }
    ) { measurables, constraints ->
        require(measurables.size == 2) { "TopBlockLayout expects exactly two children" }
        val gapPx = horizontalGap.roundToPx()
        val maxWidth = constraints.maxWidth
        val slotWidth = ((maxWidth - gapPx) / 2).coerceAtLeast(0)

        // Measure the icon column at its natural height for the slot width — this is
        // the leader that defines the row's vertical extent.
        val iconPlaceable = measurables[0].measure(
            Constraints(minWidth = slotWidth, maxWidth = slotWidth)
        )
        val rowHeight = iconPlaceable.height

        // Size the widget exactly to the icon column's footprint.
        val widgetPlaceable = measurables[1].measure(
            Constraints.fixed(slotWidth, rowHeight)
        )

        layout(maxWidth, rowHeight) {
            iconPlaceable.place(0, 0)
            widgetPlaceable.place(slotWidth + gapPx, 0)
        }
    }
}

@Composable
private fun TileRow(tiles: List<HomeTileSpec>, gap: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        tiles.forEach { tile ->
            HomeTile(
                tile = tile,
                modifier = Modifier.weight(1f)
            )
        }
        // If row is short, pad with weighted spacers so existing tiles stay sized
        // identically to the full-width row tiles.
        repeat(2 - tiles.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FullTileRow(tiles: List<HomeTileSpec>, gap: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        tiles.forEach { tile ->
            HomeTile(
                tile = tile,
                modifier = Modifier.weight(1f)
            )
        }
        repeat(4 - tiles.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeTile(
    tile: HomeTileSpec,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        BadgedAppIcon(
            label = stringResource(tile.labelRes),
            iconUrl = "",
            count = tile.badgeCount,
            onClick = tile.onClick,
            iconRes = tile.iconRes,
            fallbackIcon = tile.icon,
            fallbackBrush = iosTileBrush(tile.background)
        )
    }
}
