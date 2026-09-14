package org.hogwarts.android.feature.messaging.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.hogwarts.android.core.designsystem.kit.GlassBackdrop
import org.hogwarts.android.core.designsystem.kit.glassSurface
import org.hogwarts.android.feature.messaging.R

/** The status-bar inset the web reads as `env(safe-area-inset-top)`. */
@Composable
internal fun topInset(): Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

/** `HeaderCircularButton`: a 44dp glass disc, or the tinted product disc. */
@Composable
internal fun GlassDisc(
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    product: Boolean = false,
    chatTint: Boolean = false,
    backdrop: GlassBackdrop? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val surface = if (product) {
        Modifier
            .glassSurface(CircleShape, wa.surfaceProduct, blur = 0.dp, castShadow = true)
    } else {
        Modifier.glassSurface(CircleShape, if (chatTint) wa.glassBgChat else wa.glassBg, blur = 7.dp, backdrop = backdrop)
    }
    Box(
        modifier
            .size(size)
            .then(surface)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                enabled = true,
            ) { onClick?.invoke() }
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Mirrors a start-pointing glyph under RTL (`rtl:scale-x-[-1]`). */
@Composable
internal fun Modifier.mirrorInRtl(): Modifier {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return if (rtl) this.graphicsLayer { scaleX = -1f } else this
}

data class HeaderMenuItem(val id: String, val label: String, val icon: ImageVector, val onSelect: () -> Unit)

data class SelectBar(
    val doneLabel: String,
    val onDone: () -> Unit,
    val actionLabel: String,
    val onAction: () -> Unit,
    val actionDisabled: Boolean,
)

/**
 * Port of `ios-header.tsx` as the chat list uses it: a transparent bar
 * (status inset + 56dp) whose controls float as glass discs — exit and ⋯ at the
 * start, camera and the green compose disc at the end — with the large-title
 * collapse (a frosted cloud and a centred 17sp title) once [collapsed].
 */
@Composable
internal fun WaHeader(
    modifier: Modifier = Modifier,
    collapsingTitle: String? = null,
    collapsed: Boolean = false,
    exitLabel: String? = null,
    onExit: (() -> Unit)? = null,
    optionsLabel: String? = null,
    optionsMenu: List<HeaderMenuItem> = emptyList(),
    menuOpen: Boolean = false,
    onMenuOpenChange: (Boolean) -> Unit = {},
    cameraLabel: String? = null,
    onCamera: (() -> Unit)? = null,
    addLabel: String? = null,
    onAdd: (() -> Unit)? = null,
    selectBar: SelectBar? = null,
    backdrop: GlassBackdrop? = null,
) {
    val top = topInset()
    val cloud by animateFloatAsState(if (collapsed) 1f else 0f, tween(200), label = "cloud")
    Box(modifier.fillMaxWidth().height(top + 56.dp)) {
        if (collapsingTitle != null) {
            // The cloud runs 28dp past the header so the fade ends below the discs.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(top + 56.dp + 28.dp)
                    .alpha(cloud)
                    .background(
                        Brush.verticalGradient(
                            0f to wa.scrollEdgeList,
                            0.55f to wa.scrollEdgeList.copy(alpha = wa.scrollEdgeList.alpha * 0.85f),
                            1f to wa.scrollEdgeList.copy(alpha = 0f),
                        )
                    )
            )
            Text(
                collapsingTitle,
                style = waType(17f, weight = FontWeight.SemiBold, tracking = -0.34f),
                color = wa.textPrimary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 21.dp)
                    .graphicsLayer {
                        alpha = cloud
                        translationY = (1f - cloud) * 6.dp.toPx()
                    },
            )
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (selectBar != null) {
                Box(
                    Modifier
                        .height(44.dp)
                        .glassSurface(CircleShape, wa.glassBg, blur = 7.dp, backdrop = backdrop)
                        .clip(CircleShape)
                        .clickable(enabled = !selectBar.actionDisabled) { selectBar.onAction() }
                        .padding(horizontal = 18.dp)
                        .alpha(if (selectBar.actionDisabled) 0.4f else 1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(selectBar.actionLabel, style = waType(17f, tracking = -0.43f), color = wa.glassGlyph)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(wa.surfaceProduct)
                        .clickable { selectBar.onDone() }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(selectBar.doneLabel, style = waType(17f, weight = FontWeight.SemiBold, tracking = -0.43f), color = wa.textInvert)
                }
                return@Row
            }
            if (onExit != null) {
                GlassDisc(exitLabel.orEmpty(), onExit, backdrop = backdrop) {
                    WaIcon(R.drawable.ic_wa_chevron_lt_32, 26.dp, wa.glassGlyph, Modifier.mirrorInRtl())
                }
            }
            if (optionsLabel != null) {
                GlassDisc(optionsLabel, { if (optionsMenu.isNotEmpty()) onMenuOpenChange(true) }, backdrop = backdrop) {
                    WaIcon(R.drawable.ic_wa_meetball_24, 24.dp, wa.glassGlyph)
                }
            }
            Spacer(Modifier.weight(1f))
            if (cameraLabel != null) {
                GlassDisc(cameraLabel, onCamera, backdrop = backdrop) {
                    WaIcon(R.drawable.ic_wa_camera_24, 35.dp, wa.glassGlyph)
                }
            }
            if (addLabel != null) {
                GlassDisc(addLabel, onAdd, product = true) {
                    WaIcon(R.drawable.ic_wa_plus_add_24, 35.dp, wa.textInvert)
                }
            }
        }
        if (menuOpen && optionsMenu.isNotEmpty()) {
            OptionsMenu(optionsMenu, top, onDismiss = { onMenuOpenChange(false) })
        }
    }
}

/**
 * The ⋯ card, measured off `public/whatsapp/File (2).png`: 250dp wide, 32dp
 * corners, its top level with the discs, 8dp in from the start edge; rows 42dp
 * apart with the glyph slot 28dp in and the label after 13dp.
 */
@Composable
private fun OptionsMenu(items: List<HeaderMenuItem>, top: Dp, onDismiss: () -> Unit) {
    Popup(onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
        ) {
            Column(
                Modifier
                    .padding(start = 8.dp, top = top + 4.dp)
                    .width(250.dp)
                    .glassSurface(RoundedCornerShape(32.dp), wa.glassMenu, blur = 20.dp, castShadow = true)
                    .padding(vertical = 10.dp),
            ) {
                items.forEach { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clickable { onDismiss(); item.onSelect() }
                            .padding(start = 28.dp, end = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        Image(item.icon, null, Modifier.size(24.dp), colorFilter = ColorFilter.tint(wa.glassGlyph))
                        Text(item.label, style = waType(17f, tracking = -0.43f), color = wa.glassGlyph)
                    }
                }
            }
        }
    }
}

/** `IosSectionHeading`. */
@Composable
internal fun WaSectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = waType(17f, 22f, FontWeight.SemiBold, -0.34f),
        color = wa.textPrimary,
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
    )
}

/** `IosTabEmpty`: the centred placeholder of a page with nothing in it. */
@Composable
internal fun WaTabEmpty(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = waType(20f, 26f, FontWeight.SemiBold, -0.4f), color = wa.textPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(body, style = waType(15f, 21f), color = wa.textSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

/** `mirrorInRtl` for images drawn with `scale`. */
internal fun Modifier.flipX(flip: Boolean): Modifier = if (flip) scale(scaleX = -1f, scaleY = 1f) else this

internal val Transparent = Color.Transparent
