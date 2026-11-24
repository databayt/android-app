package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.R
import androidx.compose.foundation.background

/** Door to «جاهزية الصفوف» for admins and class supervisors (`/school/analysis`). */
@Composable
fun ReadinessEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.muted)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.dash_readiness_title), style = type.body, color = colors.foreground)
        Text(stringResource(R.string.dash_readiness_subtitle), style = type.caption, color = colors.mutedForeground)
    }
}
