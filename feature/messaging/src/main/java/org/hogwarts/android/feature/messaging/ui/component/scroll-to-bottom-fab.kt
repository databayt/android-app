package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors

@Composable
fun ScrollToBottomFab(
    onClick: () -> Unit,
    unreadCount: Int,
    modifier: Modifier = Modifier,
) {
    val waColors = LocalWhatsAppColors.current
    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
        ) {
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = "Scroll to bottom",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (unreadCount > 0) {
            val label = if (unreadCount > 99) "99+" else "$unreadCount"
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-6).dp)
                    .background(waColors.surfaceProduct, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}
