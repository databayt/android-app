package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.apple.AppleShape
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.core.designsystem.theme.HogwartsPrimary
import org.hogwarts.android.core.designsystem.theme.HogwartsError
import org.hogwarts.android.core.designsystem.theme.HogwartsTertiary

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.12f),
                shape = AppleShape.Chip
            )
            .padding(horizontal = AppleSpacing.Compact, vertical = AppleSpacing.Tiny),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp).padding(end = 4.dp),
                tint = color
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun StatusBadgeRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column {
                StatusBadge(text = "مقبول", color = HogwartsTertiary)
                Spacer(Modifier.height(8.dp))
                StatusBadge(text = "مرفوض", color = HogwartsError)
                Spacer(Modifier.height(8.dp))
                StatusBadge(text = "قيد المراجعة", color = HogwartsPrimary)
            }
        }
    }
}
