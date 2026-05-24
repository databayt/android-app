package org.hogwarts.android.core.designsystem.atom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

@Composable
fun FilterChipsRow(
    chips: List<String>,
    selectedChip: String?,
    onChipSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    counts: Map<String, Int>? = null,
    showFadeEdges: Boolean = true
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showFadeEdges) Modifier.drawWithContent {
                    drawContent()
                    val fadeWidth = 24.dp.toPx()
                    if (scrollState.value > 0) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, Color.Transparent),
                                startX = 0f, endX = fadeWidth
                            ), size = size.copy(width = fadeWidth)
                        )
                    }
                    if (scrollState.value < scrollState.maxValue) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.White),
                                startX = size.width - fadeWidth, endX = size.width
                            ), size = size.copy(width = fadeWidth),
                            topLeft = androidx.compose.ui.geometry.Offset(size.width - fadeWidth, 0f)
                        )
                    }
                } else Modifier
            )
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            val isSelected = chip == selectedChip
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = spring(),
                label = "chipColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(),
                label = "chipContentColor"
            )

            val count = counts?.get(chip)
            val label = if (count != null) "$chip ($count)" else chip

            Surface(
                onClick = { onChipSelected(chip) },
                shape = RoundedCornerShape(50),
                color = containerColor,
                contentColor = contentColor
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun FilterChipsRowRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FilterChipsRow(
                chips = listOf("الكل", "حاضر", "غائب", "متأخر"),
                selectedChip = "الكل",
                onChipSelected = {},
                counts = mapOf("الكل" to 30, "حاضر" to 22, "غائب" to 5, "متأخر" to 3)
            )
        }
    }
}
