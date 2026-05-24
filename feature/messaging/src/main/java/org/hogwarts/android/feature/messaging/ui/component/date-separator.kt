package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.messaging.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateSeparator(
    date: Instant,
    modifier: Modifier = Modifier,
) {
    val today = stringResource(R.string.messaging_ui_today)
    val yesterday = stringResource(R.string.messaging_ui_yesterday)
    val label = remember(date, today, yesterday) { formatDateLabel(date, today, yesterday) }
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF54656F),
            modifier = Modifier
                .background(Color(0xFFFEFDFC), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

private fun formatDateLabel(instant: Instant, todayLabel: String, yesterdayLabel: String): String {
    val locale = Locale.getDefault()
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when {
        date == today -> todayLabel
        date == today.minusDays(1) -> yesterdayLabel
        date.isAfter(today.minusDays(7)) ->
            date.format(DateTimeFormatter.ofPattern("EEEE", locale))
        else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", locale))
    }
}
