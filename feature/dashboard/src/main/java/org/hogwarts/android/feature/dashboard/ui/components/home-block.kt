package org.hogwarts.android.feature.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.kit.AppTile
import org.hogwarts.android.core.designsystem.kit.AppTileItem
import org.hogwarts.android.core.designsystem.kit.TileArt
import org.hogwarts.android.core.designsystem.locale.currentLocale
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.dashboard.R
import java.time.LocalDate
import java.time.format.TextStyle

private val CardShape = RoundedCornerShape(28.dp)

/**
 * The phone dashboard's first rows — mirrors hogwarts `dashboard/home-block-client.tsx`:
 * the mint calendar card beside a 2x2 cluster (Notifications, Messages, Lumos,
 * Subjects), every horizontal gap 32dp, rows 20dp. The calendar leads; RTL
 * mirrors the order the same way the web grid does.
 */
@Composable
fun HomeBlock(
    eventsToday: Int?,
    onOpen: (href: String) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val locale = currentLocale()

    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .shadow(4.dp, CardShape, clip = false)
                    .clip(CardShape)
                    .background(BrandColors.CalendarMint)
                    .clickable { onOpen("/events") }
                    .padding(16.dp),
            ) {
                Text(
                    today.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
                    color = BrandColors.WeekdayRed,
                    style = type.section.copy(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    // The web prints the raw day number (`getDate()`), Latin digits in both languages.
                    today.dayOfMonth.toString(),
                    color = BrandColors.Ink,
                    style = type.figureWide.copy(fontSize = 84.sp, lineHeight = 84.sp, letterSpacing = (-4).sp),
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                if (eventsToday != null) {
                    Text(
                        if (eventsToday > 0) {
                            stringResource(R.string.dash_events_today, eventsToday.toString())
                        } else {
                            stringResource(R.string.dash_no_events)
                        },
                        color = BrandColors.Ink.copy(alpha = 0.55f),
                        style = type.tileLabel.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
            Text(
                stringResource(R.string.dash_today),
                style = type.tileLabel,
                color = colors.foreground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val tiles = listOf(
            AppTileItem("notifications", stringResource(R.string.dash_tile_notifications), { onOpen("/notifications") }, art = TileArt.Notifications),
            AppTileItem("messages", stringResource(R.string.dash_tile_messages), { onOpen("/messages") }, art = TileArt.Message),
            AppTileItem("lumos", stringResource(R.string.dash_tile_lumos), { onOpen("/lumos") }, art = TileArt.Stream),
            AppTileItem("subjects", stringResource(R.string.dash_tile_subjects), { onOpen("/subjects") }, art = TileArt.Subject),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            tiles.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.Top) {
                    row.forEach { tile -> AppTile(tile, Modifier.weight(1f), maxFaceSize = null, labelLines = 1) }
                }
            }
        }
    }
}
