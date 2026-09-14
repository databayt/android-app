package org.hogwarts.android.core.designsystem.kit

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.painter.Painter
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors

/**
 * One cell of the [GlassCapsuleTabBar]: an outline glyph, the solid glyph it
 * swaps to when current, and an optional count badge.
 */
@Immutable
data class GlassCapsuleTab(
    val key: String,
    val label: String,
    val icon: Painter,
    val selectedIcon: Painter,
    val badge: Int = 0,
)

/**
 * What sits behind a piece of glass. The content that scrolls under the bar
 * records itself into [layer] through [glassBackdropSource]; the bar draws that
 * recording back, blurred, inside its own outline. Without a source (or below
 * API 31, where `RenderEffect` does not exist) the glass is its translucent
 * fill alone, which is the web's `backdrop-filter` fallback too.
 */
@Stable
class GlassBackdrop internal constructor(internal val layer: GraphicsLayer) {
    internal var origin by mutableStateOf(Offset.Zero)
    /** Bumped on every recording so the glass redraws when the content does. */
    internal var frame by mutableIntStateOf(0)
    internal var recorded = false
}

@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdrop(layer) }
}

/** Marks the content a [GlassBackdrop] reads from. */
fun Modifier.glassBackdropSource(backdrop: GlassBackdrop): Modifier = this
    .onGloballyPositioned { backdrop.origin = it.positionInRoot() }
    .drawWithContent {
        backdrop.layer.record { this@drawWithContent.drawContent() }
        backdrop.recorded = true
        // Written without observing it, so the source does not invalidate itself.
        Snapshot.withoutReadObservation { backdrop.frame++ }
        drawLayer(backdrop.layer)
    }

/**
 * The liquid-glass material of hogwarts `globals.css` (`.wa-glass-tabbar`,
 * `.wa-glass-control`): a translucent fill over a blurred backdrop, a 0.5px
 * bright border, a refractive rim lit from the upper *start* corner (so it
 * mirrors under RTL) and a cast shadow thrown straight down (so it does not).
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape,
    fill: Color,
    blur: Dp,
    backdrop: GlassBackdrop? = null,
    castShadow: Boolean = true,
): Modifier {
    val wa = LocalWhatsAppColors.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val blurLayer = rememberGraphicsLayer()
    var selfOrigin by remember { mutableStateOf(Offset.Zero) }
    val canBlur = backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dark = wa.surfacePrimary.luminanceIsDark()
    val rimDark = if (dark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.07f)
    val shadow = if (dark) Shadow(24.dp, Color.Black.copy(alpha = 0.45f), 0.dp, DpOffset(0.dp, 8.dp))
    else Shadow(40.dp, Color.Black.copy(alpha = 0.12f), 0.dp, DpOffset(0.dp, 8.dp))
    val rimX = if (rtl) (-1).dp else 1.dp
    return this
        .then(if (castShadow) Modifier.dropShadow(shape, shadow) else Modifier)
        .onGloballyPositioned { selfOrigin = it.positionInRoot() }
        .drawBehind {
            if (canBlur && backdrop!!.recorded) {
                // Read to redraw when the content underneath records a new frame.
                @Suppress("UNUSED_VARIABLE") val frame = backdrop.frame
                val outline = shape.createOutline(size, layoutDirection, this)
                val path = Path().apply {
                    when (outline) {
                        is Outline.Rectangle -> addRect(outline.rect)
                        is Outline.Rounded -> addRoundRect(outline.roundRect)
                        is Outline.Generic -> addPath(outline.path)
                    }
                }
                blurLayer.renderEffect = BlurEffect(blur.toPx(), blur.toPx(), TileMode.Clamp)
                blurLayer.record(backdrop.layer.size) { drawLayer(backdrop.layer) }
                clipPath(path) {
                    translate(backdrop.origin.x - selfOrigin.x, backdrop.origin.y - selfOrigin.y) {
                        drawLayer(blurLayer)
                    }
                }
            }
            val outline = shape.createOutline(size, layoutDirection, this)
            when (outline) {
                is Outline.Rectangle -> drawRect(fill)
                is Outline.Rounded -> drawRoundRect(
                    fill,
                    topLeft = Offset(outline.roundRect.left, outline.roundRect.top),
                    size = androidx.compose.ui.geometry.Size(outline.roundRect.width, outline.roundRect.height),
                    cornerRadius = outline.roundRect.topLeftCornerRadius,
                )
                is Outline.Generic -> drawPath(outline.path, fill)
            }
        }
        .innerShadow(shape, Shadow(1.5.dp, wa.glassRimLight, 0.dp, DpOffset(rimX, 1.dp)))
        .innerShadow(shape, Shadow(1.5.dp, rimDark, 0.dp, DpOffset(-rimX, (-1).dp)))
        .border(0.5.dp, wa.glassBorder, shape)
}

private fun Color.luminanceIsDark(): Boolean = (0.2126f * red + 0.7152f * green + 0.0722f * blue) < 0.5f

/**
 * Port of hogwarts `messaging/mobile/ios-tabbar.tsx`: the floating capsule
 * that carries the five Messages tabs.
 *
 * Geometry is the web's: 25dp side gutter, 4dp inner padding around 54dp
 * cells (the node's 62dp bar, a capsule), a recessed selected pill that is the
 * whole cell, 26dp glyphs over a 10sp medium label, and a 16dp product-green
 * badge 15dp from the glyph's start edge and 5dp above it. The floor sits 10dp
 * off the screen edge, or tucks 8dp into the navigation-bar inset.
 */
@Composable
fun GlassCapsuleTabBar(
    tabs: List<GlassCapsuleTab>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    badgeText: (Int) -> String = { it.toString() },
    labelStyle: TextStyle = TextStyle(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.06).sp),
) {
    val wa = LocalWhatsAppColors.current
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val floor = maxOf(10.dp, navBottom - 8.dp)
    Box(
        modifier
            .fillMaxWidth()
            .padding(start = 25.dp, end = 25.dp, bottom = floor),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .glassSurface(CircleShape, wa.glassBg, blur = 20.dp, backdrop = backdrop)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = tab.key == selectedKey
                val tint = if (selected) wa.textTabbarSelected else wa.textTabbar
                Column(
                    Modifier
                        .weight(1f)
                        .height(54.dp)
                        // Painted as a capsule rather than clipped: the web's button
                        // does not cut its label at the rounded ends.
                        .then(if (selected) Modifier.background(wa.glassInner, CircleShape) else Modifier)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                        ) { onSelect(tab.key) }
                        .semantics { this.selected = selected },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                ) {
                    Box {
                        Icon(
                            painter = if (selected) tab.selectedIcon else tab.icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(26.dp),
                        )
                        if (tab.badge > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = 15.dp, y = (-5).dp)
                                    .height(16.dp)
                                    .defaultMinSize(minWidth = 16.dp)
                                    .clip(RoundedCornerShape(50))
                                    .drawBehind { drawRect(wa.surfaceProduct) }
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    badgeText(tab.badge),
                                    color = wa.textInvert,
                                    style = LocalTextStyle.current.merge(TextStyle(fontSize = 11.sp, lineHeight = 11.sp, letterSpacing = (-0.12).sp)),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Text(
                        tab.label,
                        color = tint,
                        style = LocalTextStyle.current.merge(labelStyle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 2.dp),
                    )
                }
            }
        }
    }
}
