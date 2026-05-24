package org.hogwarts.android.core.designsystem.atom

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Apple-style tab bar (Figma node 12:2378, 402×95).
 * Outer padding: top=16, bottom=25, horizontal=25.
 * Inner pill container (Tab Bar Buttons): full-round (296dp), shadow 0x8y 40blur @ rgba(0,0,0,0.12).
 * Inner padding: start=2, end=10. Tab cell: 102dp wide, pt=6, pb=7, px=8.
 */
@Composable
fun HogwartsTabBar(
    modifier: Modifier = Modifier,
    scrollState: LazyListState? = null,
    content: @Composable RowScope.() -> Unit
) {
    val isScrolled by remember(scrollState) {
        derivedStateOf {
            scrollState?.let {
                it.firstVisibleItemIndex > 0 || it.firstVisibleItemScrollOffset > 0
            } ?: false
        }
    }

    val tabBarHeight by animateDpAsState(
        targetValue = if (isScrolled) 52.dp else 54.dp,
        label = "tabBarHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(PaddingValues(start = 25.dp, end = 25.dp, top = 16.dp, bottom = 25.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 40.dp,
                    shape = RoundedCornerShape(296.dp),
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(RoundedCornerShape(296.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .height(tabBarHeight)
                .selectableGroup()
                .padding(PaddingValues(start = 2.dp, end = 10.dp)),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * A single tab item within [HogwartsTabBar].
 * Cell: 102dp wide, pt=6, pb=7, px=8. Icon 28dp. Label 10sp SemiBold, tracking=-0.1sp, lineHeight=12sp.
 * Selected fill: full-round (100dp), color vibrant/tertiary (#ededed). Selected text: primary.
 */
@Composable
fun HogwartsTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    labelAlpha: Float = 1f
) {
    val iconColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val textColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val iconOffset by animateDpAsState(
        targetValue = if (labelAlpha < 0.5f) 0.dp else (-1).dp,
        label = "iconOffset"
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            // Figma: vibrant/tertiary #ededed — fills---vibrant/tertiary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .offset(x = (-2).dp)
                    .width(106.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Column(
            modifier = Modifier.padding(
                PaddingValues(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 7.dp)
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = iconOffset)
            )
            if (labelAlpha > 0f) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.1).sp
                    ),
                    color = textColor,
                    modifier = Modifier.alpha(labelAlpha)
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Composable
private fun HogwartsTabBarSurfaceShim() {
    // Keep Surface import path alive for external consumers — noop.
    Surface {}
}
