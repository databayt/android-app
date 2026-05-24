package org.hogwarts.android.core.designsystem.atom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import org.hogwarts.android.core.designsystem.apple.AppleMaterial
import org.hogwarts.android.core.designsystem.apple.liquidGlassCard
import kotlin.math.absoluteValue

/**
 * iOS 26 Smart Stack-style widget. Square 2x2 surface (by default — see [aspectRatio])
 * that hosts a vertical pager the user can swipe up/down through to cycle between
 * data pages.
 *
 * Visual contract — three layers:
 *  1. Outer Liquid Glass surface, always edge-to-edge of [modifier]'s footprint. Uses a
 *     thin material so the wallpaper shows through the gap that opens up during a swipe,
 *     mirroring the Liquid Glass treatment from Figma node 112-608.
 *  2. Per-page colored card, painted by the page composable. Lives INSIDE the glass and
 *     animates with the swipe: when settled it sits flush with the glass; mid-swipe it
 *     scales down and pads inward so the glass shows around its edges; the incoming page
 *     enters small from the side and grows to fit.
 *  3. Vertical page-indicator strip pinned to the trailing edge, stays visible during
 *     and after the swipe.
 *
 * Page composables receive a `BoxScope`. They are expected to paint their own
 * background — typically `Box(Modifier.fillMaxSize().clip(…).background(…))` — so the
 * colored card follows the scale/padding animation cleanly.
 */
@Composable
fun rememberSmartStackPagerState(pageCount: Int): PagerState =
    rememberPagerState(pageCount = { pageCount.coerceAtLeast(1) })

@Composable
fun SmartStackWidget(
    pages: List<@Composable BoxScope.() -> Unit>,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberSmartStackPagerState(pages.size),
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 28.dp,
    /** Outer surface material. Thinner = more wallpaper showing through during swipes. */
    material: AppleMaterial = AppleMaterial.Thin,
    /**
     * Width-to-height ratio. `1f` = square (the iOS Smart Stack default). Pass `null`
     * when the widget needs to fill a non-square slot — e.g. when sized by a sibling
     * column whose height includes label rows the widget should match top-to-bottom.
     */
    aspectRatio: Float? = 1f
) {
    Box(
        modifier = modifier
            .then(if (aspectRatio != null) Modifier.aspectRatio(aspectRatio) else Modifier)
            .liquidGlassCard(cornerRadius = cornerRadius, material = material)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Pages slide flush against each other; the scale+padding effect creates
            // the perceived gap, matching iOS where there's no dead space between pages.
            pageSpacing = 0.dp
        ) { page ->
            // Distance of this page from the centered viewport position, in pages.
            // 0f = settled in view. 1f = one full slot away (the next/prev card).
            val offset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)

            // At rest the inner card hugs the glass (0dp padding, scale 1).
            // As the card slides away — or the next one slides in — it shrinks slightly
            // and an inset opens between the card and the glass, revealing the Liquid
            // Glass material underneath. Peak values are tuned so the effect reads as
            // "card pop with glass behind", not full miniaturization.
            val pad = lerp(0.dp, 12.dp, offset)
            val scale = lerp(1f, 0.92f, offset)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                content = pages[page]
            )
        }
    }
}

/**
 * Vertical column of equal-sized dots that mark the current page in a [SmartStackWidget].
 *
 * Visibility is gated on [PagerState.isScrollInProgress] so the indicator only appears
 * while the user is interacting with the widget. The fade is asymmetric — fast on,
 * slower off — so the dots linger briefly after the swipe settles, matching iOS.
 *
 * Designed to be **overlaid** on the widget's trailing edge (e.g. as a sibling in a
 * shared Box, aligned to [Alignment.CenterEnd]). It floats over the surface and never
 * consumes layout width, so the widget's footprint stays untouched whether the
 * indicator is visible or faded out — the surrounding grid keeps its alignment with
 * tile rows below regardless of indicator state.
 */
@Composable
fun SmartStackIndicator(
    pageCount: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.45f),
    dotSize: Dp = 6.dp,
    dotSpacing: Dp = 6.dp
) {
    if (pageCount <= 1) return

    val scrolling = pagerState.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (scrolling) 1f else 0f,
        animationSpec = tween(durationMillis = if (scrolling) 150 else 500),
        label = "indicator-alpha"
    )

    Column(
        modifier = modifier.alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(dotSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(pageCount) { i ->
            val active = i == pagerState.currentPage
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(if (active) activeColor else inactiveColor)
            )
        }
    }
}

/**
 * One canonical page layout for the SmartStack: small uppercase title row at the top,
 * big numeric value + caption hugging the bottom-start. Mirrors the iOS "stat" widget.
 */
@Composable
fun BoxScope.SmartStackStatPage(
    title: String,
    value: String,
    caption: String? = null,
    accent: Color = Color.White,
    titleColor: Color = Color.White.copy(alpha = 0.85f),
    valueColor: Color = Color.White,
    captionColor: Color = Color.White.copy(alpha = 0.75f),
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                ),
                color = titleColor
            )
            leadingIcon?.invoke()
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                ),
                color = valueColor
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = captionColor,
                    maxLines = 2
                )
            }
        }

        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.85f))
        )
    }
}
