package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Action sheet with capsule action buttons — Figma node 1:58 (300×542).
 *
 * Container: w=300, radius=34, padding=14, gap=10.
 * Title+Description block: pt=8 pb=24 px=8 gap=10, items-start.
 *   Title: 17sp SemiBold line=22 tracking=-0.43.
 *   Description: 17sp Regular line=22 tracking=-0.43.
 * Action button: full-width h=48 px=16 py=13 radius=100, bg=fills/secondary (≈surfaceVariant).
 *   Text: 17sp Medium line=22 tracking=-0.43. Destructive → error.
 * Button-to-button gap: 10dp (from parent).
 */
data class ActionSheetAction(
    val label: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun HogwartsActionSheet(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actions: List<ActionSheetAction> = emptyList()
) {
    Surface(
        modifier = modifier.width(300.dp),
        shape = RoundedCornerShape(34.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title + Description block (pt=8, pb=24, px=8, gap=10)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 24.dp)),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        letterSpacing = (-0.43).sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            letterSpacing = (-0.43).sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Action buttons
            actions.forEach { action ->
                Button(
                    onClick = action.onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (action.isDestructive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            letterSpacing = (-0.43).sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HogwartsActionSheetPreview() {
    HogwartsTheme {
        Box(Modifier.padding(16.dp)) {
            HogwartsActionSheet(
                title = "A Short Title Is Best",
                message = "A description should be a short, complete sentence.",
                actions = listOf(
                    ActionSheetAction(label = "Action 1", isDestructive = true, onClick = {}),
                    ActionSheetAction(label = "Action 2", onClick = {}),
                    ActionSheetAction(label = "Action 3", onClick = {})
                )
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun HogwartsActionSheetRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(Modifier.padding(16.dp)) {
                HogwartsActionSheet(
                    title = "اختر الإجراء",
                    message = "حدد خيارا من الأسفل",
                    actions = listOf(
                        ActionSheetAction(label = "مشاركة", onClick = {}),
                        ActionSheetAction(label = "نسخ الرابط", onClick = {}),
                        ActionSheetAction(label = "حذف", isDestructive = true, onClick = {})
                    )
                )
            }
        }
    }
}
