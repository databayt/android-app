package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons

/**
 * Translucent "Search" pill above the dock (Figma node `I120:658;512:41975`).
 *
 * Figma spec:
 *  - Height 30dp, corner 100dp (fully rounded).
 *  - Padding h=12, v=7; content gap 2dp between icon and label.
 *  - Text: SF Pro Semibold 11.5sp white.
 *  - Surface: same [GlassBackdrop] as the dock — layered so the glyph + label always
 *    render on top of the glass, never behind it.
 *
 * [onClick] stubbed for v1 — search screen is a follow-up.
 */
@Composable
fun HomeSearchPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(30.dp)
                .clip(RoundedCornerShape(100.dp))
                .clickable(onClick = onClick)
                .semantics { role = Role.Button }
        ) {
            GlassBackdrop(cornerRadius = 100.dp)
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = HogwartsIcons.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }
        }
    }
}
