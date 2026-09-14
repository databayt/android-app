package org.hogwarts.android.core.designsystem.kit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * The school dashboard's phone header — mirrors `template/platform-header`
 * below `lg`: a 48dp bar with a bottom hairline holding only the animated
 * hamburger and the word "Menu". Everything else lives inside the menu.
 */
@Composable
fun PlatformHeader(
    menuLabel: String,
    menuOpen: Boolean,
    onToggleMenu: () -> Unit,
    modifier: Modifier = Modifier,
    toggleDescription: String = menuLabel,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = HogwartsTheme.colors
    Column(modifier.fillMaxWidth().background(colors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onToggleMenu,
                    )
                    .semantics {
                        contentDescription = toggleDescription
                        stateDescription = if (menuOpen) "expanded" else "collapsed"
                    }
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Hamburger(open = menuOpen)
                Text(
                    text = menuLabel,
                    style = HogwartsTheme.type.rowTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    color = colors.foreground,
                )
            }
            Box(Modifier.weight(1f))
            trailing?.invoke(this)
        }
        Hairline(colors.border)
    }
}

/** Two 16dp bars that cross into an X, like the web's 100ms hamburger. */
@Composable
private fun Hamburger(open: Boolean) {
    val color = HogwartsTheme.colors.foreground
    val progress by animateFloatAsState(if (open) 1f else 0f, tween(100), label = "hamburger")
    Canvas(Modifier.size(16.dp)) {
        val stroke = 2.dp.toPx()
        val topClosed = 4.dp.toPx() + stroke / 2
        val bottomClosed = 10.dp.toPx() + stroke / 2
        val mid = 6.4.dp.toPx() + stroke / 2
        val topY = topClosed + (mid - topClosed) * progress
        val bottomY = bottomClosed + (mid - bottomClosed) * progress
        rotate(-45f * progress, pivot = Offset(size.width / 2, topY)) {
            drawLine(color, Offset(0f, topY), Offset(size.width, topY), stroke)
        }
        rotate(45f * progress, pivot = Offset(size.width / 2, bottomY)) {
            drawLine(color, Offset(0f, bottomY), Offset(size.width, bottomY), stroke)
        }
    }
}
