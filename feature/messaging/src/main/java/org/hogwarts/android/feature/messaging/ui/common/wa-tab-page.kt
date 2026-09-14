package org.hogwarts.android.feature.messaging.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** The web's `pb-[calc(env(safe-area-inset-bottom)+96px)]`: room for the floating tab bar. */
@Composable
internal fun tabBarClearance() = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 96.dp

/**
 * `IosTabPage`: the scaffold every tab outside Chats shares — a big 28sp title
 * and one scroller that runs under the (empty, floating) header and the tab bar.
 */
@Composable
internal fun WaTabPage(
    title: String,
    modifier: Modifier = Modifier,
    background: Color = wa.surfacePrimary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(top = topInset() + 56.dp, bottom = tabBarClearance()),
    ) {
        Text(
            title,
            style = waType(28f, 28f, FontWeight.Bold, -1.1f),
            color = wa.textPrimary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp),
        )
        content()
    }
}

/** A hairline along the bottom edge (`border-b-[0.33px]`). */
internal fun Modifier.bottomHairline(color: Color): Modifier = drawBehind {
    val stroke = 0.33.dp.toPx()
    drawLine(color, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
}

enum class ListRowTone { Default, Danger, Product }

/**
 * `IosListRow`: the chat row's geometry — 56dp avatar, 76dp row, separator
 * inset to the text column — for Updates, Calls and Communities.
 */
@Composable
internal fun WaListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tone: ListRowTone = ListRowTone.Default,
    avatarUrl: String? = null,
    avatarIcon: Int? = null,
    avatarSkin: AvatarSkin = AvatarSkin.Person,
    meta: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val titleColor = when (tone) {
        ListRowTone.Danger -> wa.textQuoteTitle
        ListRowTone.Product -> wa.textProduct
        ListRowTone.Default -> wa.textPrimary
    }
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 16.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.66.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.padding(top = 2.dp)) {
            WaAvatar(url = avatarUrl, size = 56.dp, skin = avatarSkin, iconRes = avatarIcon, glyphSize = if (avatarIcon != null) 28.dp else 30.dp)
        }
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 76.dp)
                .bottomHairline(wa.borderSeparator)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = waType(16f, 20f, FontWeight.SemiBold, -0.32f), color = titleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!subtitle.isNullOrEmpty()) {
                    Text(subtitle, style = waType(14f, 19f, tracking = -0.14f), color = wa.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            // The trailing column keeps its 15dp end padding even when empty, as on the web.
            Box(Modifier.padding(end = 15.dp, top = 1.dp)) {
                if (meta != null) Text(meta, style = waType(14f, 19f, tracking = -0.14f), color = wa.textSecondary)
            }
        }
    }
}
