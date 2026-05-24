package org.hogwarts.android.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.core.designsystem.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * iOS-style large title height in dp.
 * When fully expanded, the large title area occupies this height below the inline bar.
 */
private val LargeTitleExpandedHeight = 52.dp

/**
 * Height of the compact inline navigation bar (matches iOS 44pt nav bar).
 */
private val InlineBarHeight = AppleSpacing.MinTouchTarget // 44.dp

/**
 * Height of the optional search bar area.
 */
private val SearchBarHeight = 52.dp

/**
 * Apple-style Large Title Collapsing Navigation Bar scaffold.
 *
 * Mimics the iOS `UINavigationController` large-title behavior:
 * - At scroll position zero the title is displayed large (34sp, bold) beneath the bar.
 * - As the user scrolls down, the large title collapses and a compact inline title
 *   (17sp, semibold) fades in within the top bar.
 * - An optional search bar is revealed when the user pulls down (over-scrolls).
 * - Supports an optional back button and trailing action icons.
 *
 * Spring animation specs are aligned with the project's [AppleSpring] / [AppleAnimation]
 * conventions.
 *
 * @param title The navigation title displayed in both large and inline states.
 * @param modifier Modifier applied to the root scaffold.
 * @param onBackClick If non-null, a back chevron is shown at the leading edge.
 * @param actions Trailing icon actions rendered in the inline bar.
 * @param searchBar Optional composable slot revealed on pull-down.
 * @param floatingActionButton Optional FAB slot forwarded to [Scaffold].
 * @param content Scrollable body. Receives [PaddingValues] that account for the
 *   collapsing header so content is never hidden behind it. The caller **must**
 *   wire a scroll container (LazyColumn, Column+verticalScroll, etc.) inside
 *   this lambda; the scaffold connects via [nestedScroll] to track offsets.
 */
@Composable
fun AppleLargeTitleScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    searchBar: (@Composable () -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val density = LocalDensity.current

    val largeTitleHeightPx = with(density) { LargeTitleExpandedHeight.toPx() }
    val searchBarHeightPx = with(density) { SearchBarHeight.toPx() }

    // Scroll offset tracks how much the large title region has been scrolled away.
    // Range: 0 (fully expanded) .. largeTitleHeightPx (fully collapsed).
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    // Over-scroll offset for search bar reveal (negative = pulling down past top).
    var overScrollOffset by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(largeTitleHeightPx, searchBarHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y

                // Scrolling up (dy negative) -- collapse the large title first.
                if (dy < 0f) {
                    // If search bar is revealed, retract it first.
                    if (overScrollOffset > 0f) {
                        val consumed = dy.coerceAtLeast(-overScrollOffset)
                        overScrollOffset = (overScrollOffset + consumed).coerceAtLeast(0f)
                        return Offset(0f, consumed)
                    }

                    val previous = scrollOffset
                    scrollOffset = (scrollOffset - dy).coerceIn(0f, largeTitleHeightPx)
                    val consumed = previous - scrollOffset
                    return Offset(0f, consumed)
                }

                // Scrolling down (dy positive) -- expand the large title.
                if (dy > 0f && scrollOffset > 0f) {
                    val previous = scrollOffset
                    scrollOffset = (scrollOffset - dy).coerceIn(0f, largeTitleHeightPx)
                    val consumed = previous - scrollOffset
                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Over-scroll past the top reveals the search bar.
                if (available.y > 0f && searchBar != null && scrollOffset == 0f) {
                    val previous = overScrollOffset
                    overScrollOffset =
                        (overScrollOffset + available.y).coerceIn(0f, searchBarHeightPx)
                    val delta = overScrollOffset - previous
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }
        }
    }

    // -- Derived animation values --

    // 0 = fully expanded, 1 = fully collapsed.
    val collapseProgress by remember {
        derivedStateOf { (scrollOffset / largeTitleHeightPx).coerceIn(0f, 1f) }
    }

    // Inline title fades in during the last 40 % of collapse.
    val inlineTitleAlpha by animateFloatAsState(
        targetValue = ((collapseProgress - 0.6f) / 0.4f).coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "inlineTitleAlpha"
    )

    // Large title fades out during the first 60 % of collapse.
    val largeTitleAlpha by animateFloatAsState(
        targetValue = 1f - (collapseProgress / 0.6f).coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "largeTitleAlpha"
    )

    // Remaining large-title height (animated for spring feel).
    val largeTitleHeight by animateDpAsState(
        targetValue = with(density) {
            ((largeTitleHeightPx - scrollOffset).coerceAtLeast(0f)).toDp()
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "largeTitleHeight"
    )

    // Search bar reveal height.
    val searchBarRevealHeight by animateDpAsState(
        targetValue = with(density) { overScrollOffset.toDp() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchBarHeight"
    )

    // Divider shown once the large title starts collapsing.
    val dividerAlpha by animateFloatAsState(
        targetValue = if (collapseProgress > 0.1f) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dividerAlpha"
    )

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Scaffold(
        modifier = modifier,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .nestedScroll(nestedScrollConnection)
        ) {
            // -- Header (fixed at top) --
            val totalHeaderHeight: Dp =
                statusBarPadding.calculateTopPadding() +
                    InlineBarHeight +
                    largeTitleHeight +
                    searchBarRevealHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Status bar spacer.
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(statusBarPadding.calculateTopPadding())
                )

                // -- Inline (compact) navigation bar --
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(InlineBarHeight)
                ) {
                    // Back button (leading).
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = HogwartsIcons.Back,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Inline title (center).
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(inlineTitleAlpha)
                            .padding(horizontal = 72.dp) // avoid overlap with leading/trailing
                    )

                    // Trailing actions.
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = AppleSpacing.Tiny),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }

                // -- Large title region --
                if (largeTitleHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(largeTitleHeight)
                            .graphicsLayer { alpha = largeTitleAlpha },
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.37.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(
                                    start = AppleSpacing.Standard,
                                    end = AppleSpacing.Standard,
                                    bottom = AppleSpacing.Compact
                                )
                        )
                    }
                }

                // -- Search bar (revealed on pull-down) --
                if (searchBar != null && searchBarRevealHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(searchBarRevealHeight)
                            .padding(horizontal = AppleSpacing.Standard)
                            .graphicsLayer {
                                alpha = (overScrollOffset / searchBarHeightPx).coerceIn(0f, 1f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        searchBar()
                    }
                }

                // -- Divider --
                HorizontalDivider(
                    modifier = Modifier.alpha(dividerAlpha),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // -- Scrollable content --
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = totalHeaderHeight)
            ) {
                content(PaddingValues(top = 0.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Composable
private fun AppleLargeTitleScaffoldLightPreview() {
    HogwartsTheme {
        AppleLargeTitleScaffoldPreviewContent()
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AppleLargeTitleScaffoldDarkPreview() {
    HogwartsTheme(darkTheme = true) {
        AppleLargeTitleScaffoldPreviewContent()
    }
}

@Preview(
    name = "RTL",
    showBackground = true,
    showSystemUi = true,
    locale = "ar"
)
@Composable
private fun AppleLargeTitleScaffoldRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HogwartsTheme {
            AppleLargeTitleScaffoldPreviewContent()
        }
    }
}

@Preview(name = "With Back Button", showBackground = true, showSystemUi = true)
@Composable
private fun AppleLargeTitleScaffoldBackPreview() {
    HogwartsTheme {
        AppleLargeTitleScaffold(
            title = "Settings",
            onBackClick = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = HogwartsIcons.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                repeat(20) { index ->
                    Text(
                        text = "Item $index",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(AppleSpacing.Standard)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppleLargeTitleScaffoldPreviewContent() {
    AppleLargeTitleScaffold(
        title = "Hogwarts",
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = HogwartsIcons.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        searchBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppleSpacing.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = HogwartsIcons.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .height(18.dp)
                            .width(18.dp)
                    )
                    Spacer(modifier = Modifier.width(AppleSpacing.Compact))
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            repeat(30) { index ->
                Text(
                    text = "Row $index",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AppleSpacing.Standard,
                            vertical = AppleSpacing.Small
                        )
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = AppleSpacing.Standard),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
