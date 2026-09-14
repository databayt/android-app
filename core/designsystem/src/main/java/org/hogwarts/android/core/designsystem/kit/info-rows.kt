package org.hogwarts.android.core.designsystem.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

@Immutable
data class InfoRow(val label: String, val value: String?, val key: String = label)

/**
 * Facts as label start / value end with one hairline per pair — mirrors
 * `shared/info-rows.tsx`. The value may wrap; the label never does.
 */
@Composable
fun InfoRows(rows: List<InfoRow>, modifier: Modifier = Modifier, heading: String? = null) {
    val visible = rows.filter { !it.value.isNullOrBlank() }
    if (visible.isEmpty()) return
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(modifier.fillMaxWidth()) {
        if (heading != null) {
            Text(heading, style = type.section, color = colors.foreground, modifier = Modifier.padding(bottom = 4.dp))
        }
        visible.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(row.label, style = type.body, color = colors.mutedForeground, softWrap = false)
                Text(
                    row.value.orEmpty(),
                    style = type.bodyMedium,
                    color = colors.foreground,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
            if (index < visible.lastIndex) Hairline(colors.border)
        }
    }
}
