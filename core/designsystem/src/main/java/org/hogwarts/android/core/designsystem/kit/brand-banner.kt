package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * The green banner of /library, /live and the dashboard's next action —
 * mirrors `shared/brand-banner.tsx`. The ground (#00bc6d) is a brand colour
 * that does not invert, and every piece of ink on it is pinned to #050505.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrandBanner(
    headline: AnnotatedString,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actions: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    val type = HogwartsTheme.type
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HogwartsShapes.Banner)
            .background(BrandColors.Green)
            .padding(horizontal = 32.dp, vertical = 40.dp),
    ) {
        if (eyebrow != null) {
            Text(eyebrow, style = type.body, color = BrandColors.Ink.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
        }
        Text(headline, style = type.bannerHeadline, color = BrandColors.Ink)
        if (actions != null) {
            FlowRow(
                modifier = Modifier.padding(top = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) { actions() }
        }
        if (footer != null) Box(Modifier.padding(top = 28.dp)) { footer() }
    }
}

/** The banner's pill: white with dark ink, or a quiet ghost. */
@Composable
fun BrandPill(label: String, onClick: () -> Unit, ghost: Boolean = false) {
    PillButton(label = label, onClick = onClick, variant = if (ghost) PillVariant.BrandGhost else PillVariant.BrandWhite)
}

/** Progress on the green: a dark 15% track and a dark fill. */
@Composable
fun BrandProgress(value: Float, modifier: Modifier = Modifier) {
    val pct = value.coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(HogwartsShapes.Pill)
            .background(BrandColors.Ink.copy(alpha = 0.15f))
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(pct, 0f..100f) },
    ) {
        Box(
            Modifier
                .fillMaxWidth(pct / 100f)
                .fillMaxHeight()
                .clip(HogwartsShapes.Pill)
                .background(BrandColors.Ink),
        )
    }
}
