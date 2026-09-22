package org.hogwarts.android.feature.subjects.textbook.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.textbook.data.GuideDim
import kotlin.math.abs

/** `.book-round`: a 46-dp translucent disc that dips to 94% under the finger. */
@Composable
internal fun RoundButton(
    icon: ImageVector,
    label: String,
    palette: ReaderPalette,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.8.dp,
    size: Dp = 46.dp,
    background: Color = palette.chromeBg,
    tint: Color = palette.chromeFg,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "press")
    Box(
        modifier
            .size(size)
            .scale(scale)
            .shadow(3.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(CircleShape)
            .background(background)
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(interaction, null, enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/**
 * The reading menu — a stack of pills rising from the menu button over a
 * veiled foot of the page: Contents with progress (also the scrubber), Search,
 * Themes & Settings, then the round share · rotation lock · line guide ·
 * bookmark row. Compose has no backdrop blur, so the veil is denser than the
 * web's 35%: the wash is what the blur produced.
 */
@Composable
internal fun ReadingMenu(
    palette: ReaderPalette,
    lang: String,
    percent: Int,
    rotationLocked: Boolean,
    guide: Boolean,
    bookmarked: Boolean,
    canBookmark: Boolean,
    bottomInset: Dp,
    onClose: () -> Unit,
    onContents: () -> Unit,
    onScrub: (Float) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onShare: () -> Unit,
    onToggleRotation: () -> Unit,
    onToggleGuide: () -> Unit,
    onBookmark: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.54f to Color.Transparent,
                    0.70f to palette.bg.copy(alpha = 0.9f),
                    1f to palette.bg.copy(alpha = 0.9f),
                ),
            )
            .clickable(remember { MutableInteractionSource() }, null, onClick = onClose),
    ) {
        Column(
            Modifier.align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = bottomInset + 69.dp)
                .widthIn(max = 274.dp)
                .clickable(remember { MutableInteractionSource() }, null) {},
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            ProgressPill(
                label = stringResource(R.string.reader_contents_progress, formatNumber(percent, lang)),
                percent = percent,
                palette = palette,
                onContents = onContents,
                onScrub = onScrub,
            )
            Pill(stringResource(R.string.reader_search_book), palette, onSearch) {
                Icon(ReaderIcons.Search, null, tint = palette.pillFg, modifier = Modifier.size(22.dp))
            }
            Pill(stringResource(R.string.reader_themes_settings), palette, onSettings) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("A", color = palette.pillFg, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("A", color = palette.pillFg, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
            }
            Row(Modifier.width(274.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                val mod = Modifier.weight(1f)
                RowRound(ReaderIcons.Share, stringResource(R.string.reader_share), false, true, palette, mod, onShare)
                RowRound(
                    ReaderIcons.rotationLock(rotationLocked),
                    stringResource(if (rotationLocked) R.string.reader_rotation_unlock else R.string.reader_rotation_lock),
                    rotationLocked, true, palette, mod, onToggleRotation,
                )
                RowRound(
                    ReaderIcons.LineGuide,
                    stringResource(if (guide) R.string.reader_line_guide_off else R.string.reader_line_guide),
                    guide, true, palette, mod, onToggleGuide,
                )
                RowRound(
                    if (bookmarked) ReaderIcons.BookmarkFilled else ReaderIcons.Bookmark,
                    stringResource(R.string.reader_bookmark), bookmarked, canBookmark, palette, mod, onBookmark,
                )
            }
        }
    }
}

@Composable
private fun RowRound(
    icon: ImageVector, label: String, checked: Boolean, enabled: Boolean,
    palette: ReaderPalette, modifier: Modifier, onClick: () -> Unit,
) {
    Box(
        modifier.height(47.dp)
            .shadow(1.dp, RoundedCornerShape(23.5.dp), ambientColor = Color.Black.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(23.5.dp))
            .background(if (checked) palette.pillDark else palette.pill)
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = if (checked) palette.pillDarkFg else palette.pillFg, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun Pill(label: String, palette: ReaderPalette, onClick: () -> Unit, trailing: @Composable () -> Unit) {
    Row(
        Modifier.width(274.dp).height(45.dp)
            .shadow(1.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.06f))
            .clip(CircleShape)
            .background(palette.pill)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = palette.pillFg, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        trailing()
    }
}

/**
 * The Contents pill doubles as the scrubber, as the Books app's does: the
 * read part is a light fill growing from the reading edge, the label inverts
 * wherever the fill has reached it. A tap opens the contents; a drag turns.
 */
@Composable
private fun ProgressPill(
    label: String, percent: Int, palette: ReaderPalette,
    onContents: () -> Unit, onScrub: (Float) -> Unit,
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val contents by rememberUpdatedState(onContents)
    val scrub by rememberUpdatedState(onScrub)
    val fraction = percent / 100f
    Box(
        Modifier.width(274.dp).height(45.dp)
            .clip(CircleShape)
            .background(palette.scrubEmpty)
            .pointerInput(rtl) {
                fun ratio(x: Float) = ((if (rtl) size.width - x else x) / size.width).coerceIn(0f, 1f)
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var moved = false
                    var last = down.position.x
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        if (!moved && abs(change.position.x - down.position.x) >= 4.dp.toPx()) moved = true
                        if (moved) {
                            change.consume()
                            if (change.positionChange().x != 0f) scrub(ratio(change.position.x))
                        }
                        last = change.position.x
                    }
                    if (moved) scrub(ratio(last)) else contents()
                }
            }
            .drawWithContent {
                val w = size.width * fraction
                val left = if (rtl) size.width - w else 0f
                drawRect(palette.scrubFill, Offset(left, 0f), Size(w, size.height))
                drawContent()
            },
    ) {
        PillFace(label, palette.scrubEmptyFg, Modifier)
        PillFace(
            label, palette.scrubFillFg,
            Modifier.drawWithContent {
                val w = size.width * fraction
                val left = if (rtl) size.width - w else 0f
                clipRect(left, 0f, left + w, size.height) { this@drawWithContent.drawContent() }
            },
        )
    }
}

@Composable
private fun BoxScope.PillFace(label: String, color: Color, modifier: Modifier) {
    Row(
        modifier.matchParentSize().padding(start = 16.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = color, fontSize = 17.sp, maxLines = 1)
        Icon(ReaderIcons.List, null, tint = color, modifier = Modifier.size(20.dp))
    }
}

/**
 * The line guide: the line under the finger held clear in a capsule while
 * the rest of the page is veiled back (`.book-guide-lens`).
 */
@Composable
internal fun LineGuide(
    palette: ReaderPalette,
    dim: GuideDim,
    centerY: Float,
    lensHeight: Float,
    insetX: Float,
) {
    val veil = when (dim) {
        GuideDim.High -> if (palette.deepVeil) 0.88f else 0.84f
        GuideDim.Medium -> if (palette.deepVeil) 0.66f else 0.62f
        GuideDim.Low -> if (palette.deepVeil) 0.44f else 0.40f
        GuideDim.None -> 0f
    }
    Canvas(Modifier.fillMaxSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
        val top = centerY - lensHeight / 2
        val radius = CornerRadius(14.dp.toPx())
        if (veil > 0f) drawRect(palette.bg.copy(alpha = veil))
        drawRoundRect(
            Color.Black, Offset(insetX, top), Size(size.width - 2 * insetX, lensHeight), radius,
            blendMode = BlendMode.Clear,
        )
        drawRoundRect(
            palette.rule, Offset(insetX, top), Size(size.width - 2 * insetX, lensHeight), radius,
            style = Stroke(width = 0.5.dp.toPx()),
        )
    }
}

/** "Background Dimming": how deep to veil the page, or turn the guide off. */
@Composable
internal fun GuideMenu(
    palette: ReaderPalette,
    dim: GuideDim,
    bottomInset: Dp,
    onDim: (GuideDim) -> Unit,
    onTurnOff: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, null, onClick = onDismiss)) {
        Column(
            Modifier.align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = bottomInset + 69.dp)
                .width(IntrinsicSize.Max)
                .widthIn(min = 240.dp)
                .shadow(12.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(palette.bg.copy(alpha = 0.96f))
                .clickable(remember { MutableInteractionSource() }, null) {}
                .padding(vertical = 6.dp),
        ) {
            Text(
                stringResource(R.string.reader_background_dimming), color = palette.muted, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            )
            val labels = mapOf(
                GuideDim.High to R.string.reader_dim_high, GuideDim.Medium to R.string.reader_dim_medium,
                GuideDim.Low to R.string.reader_dim_low, GuideDim.None to R.string.reader_dim_none,
            )
            GuideDim.entries.forEach { level ->
                GuideItem(stringResource(labels.getValue(level)), checked = dim == level, palette) { onDim(level) }
            }
            Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).height(0.5.dp).background(palette.rule))
            GuideItem(stringResource(R.string.reader_line_guide_turn_off), checked = false, palette, onTurnOff)
        }
    }
}

@Composable
private fun GuideItem(label: String, checked: Boolean, palette: ReaderPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(17.6.dp)) {
            if (checked) Icon(ReaderIcons.Check, null, tint = palette.fg, modifier = Modifier.fillMaxSize())
        }
        Text(label, color = palette.fg, fontSize = 17.sp)
    }
}

/** `.book-toast`: a dark capsule over the foot of the page for 1.8 s. */
@Composable
internal fun BoxScope.ReaderToast(text: String, palette: ReaderPalette, bottomInset: Dp) {
    Text(
        text,
        color = palette.pillDarkFg,
        fontSize = 13.6.sp,
        modifier = Modifier.align(Alignment.BottomCenter)
            .padding(bottom = bottomInset + 64.dp)
            .clip(CircleShape)
            .background(palette.pillDark)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}
