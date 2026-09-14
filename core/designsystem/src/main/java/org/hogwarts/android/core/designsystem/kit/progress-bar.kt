package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * The neutral bar — mirrors `ui/progress.tsx` as the phone panels use it
 * (`bg-background h-1.5`): a pill track and a primary fill. On the green
 * banner use [BrandProgress] instead.
 */
@Composable
fun ProgressBar(
    value: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = HogwartsTheme.colors.background,
) {
    val pct = value.coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(HogwartsShapes.Pill)
            .background(trackColor)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(pct, 0f..100f) },
    ) {
        Box(
            Modifier
                .fillMaxWidth(pct / 100f)
                .fillMaxHeight()
                .clip(HogwartsShapes.Pill)
                .background(HogwartsTheme.colors.primary),
        )
    }
}
