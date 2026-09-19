package org.hogwarts.android.core.designsystem.kit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

@Immutable
data class MenuLink(val key: String, val label: String, val onClick: () -> Unit, val enabled: Boolean = true)

@Immutable
data class MenuSection(val title: String, val links: List<MenuLink>)

@Immutable
data class MenuControl(
    val key: String,
    /** The glyph, unless [content] draws the control itself. */
    val icon: ImageVector? = null,
    val description: String,
    /** Null when [content] handles its own taps, e.g. a control that opens a menu. */
    val onClick: (() -> Unit)? = null,
    val badge: Int = 0,
    /** Replaces the glyph, e.g. the user's avatar. */
    val content: (@Composable () -> Unit)? = null,
)

/**
 * The phone menu — mirrors `template/mobile-nav/mobile-nav.tsx`: a full-screen
 * sheet under the header on a 90% background, the six-control toolbar row
 * (40dp targets, 24dp glyphs) over a hairline, then sections of 24sp links.
 * No icons beside the links, as on the web.
 */
@Composable
fun MenuPopover(
    visible: Boolean,
    onDismiss: () -> Unit,
    controls: List<MenuControl>,
    sections: List<MenuSection>,
    modifier: Modifier = Modifier,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    BackHandler(enabled = visible, onBack = onDismiss)
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(100)), exit = fadeOut(tween(100)), modifier = modifier) {
        Box(
            Modifier
                .fillMaxSize()
                // `bg-background/90`, read off the live popover
                // (oklab(1 0 0 / 0.9)). The web also blurs what shows
                // through; Compose can only do that by blurring the content
                // behind, so the alpha carries it here.
                .background(colors.background.copy(alpha = 0.90f))
                .clickable(indication = null, interactionSource = null, onClick = {}),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                if (controls.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier.padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            controls.forEach { control -> ToolbarControl(control) }
                        }
                        Hairline(colors.border)
                    }
                }
                sections.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(section.title, style = type.bodyMedium, color = colors.mutedForeground)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            section.links.forEach { link ->
                                Text(
                                    text = link.label,
                                    style = type.menuLink.copy(fontWeight = FontWeight.Medium),
                                    color = if (link.enabled) colors.foreground else colors.mutedForeground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = link.enabled, role = Role.Button) {
                                            onDismiss()
                                            link.onClick()
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarControl(control: MenuControl) {
    val colors = HogwartsTheme.colors
    val onClick = control.onClick
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClickLabel = control.description, onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        val content = control.content
        if (content != null) {
            content()
        } else if (control.icon != null) {
            Icon(
                control.icon,
                contentDescription = control.description,
                tint = colors.foreground,
                modifier = Modifier.size(24.dp),
            )
        }
        if (control.badge > 0) {
            // The web's `Badge` on the mail control: the count itself, not a
            // dot — `h-4 min-w-4 px-1 text-[10px] font-semibold tabular-nums`
            // on destructive, hung 6px past the button's top start corner.
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-6).dp, y = (-6).dp)
                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                    .clip(CircleShape)
                    .background(colors.destructive)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    control.badge.toString(),
                    style = HogwartsTheme.type.caption.copy(
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }
    }
}
