package org.hogwarts.android.feature.messaging.ui.chats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.GlassBackdrop
import org.hogwarts.android.core.designsystem.kit.glassBackdropSource
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.ui.common.BellGlyph
import org.hogwarts.android.feature.messaging.ui.common.CrossGlyph
import org.hogwarts.android.feature.messaging.ui.common.HeaderMenuItem
import org.hogwarts.android.feature.messaging.ui.common.ReadAllGlyph
import org.hogwarts.android.feature.messaging.ui.common.SelectBar
import org.hogwarts.android.feature.messaging.ui.common.SelectChatsGlyph
import org.hogwarts.android.feature.messaging.ui.common.SingleTickGlyph
import org.hogwarts.android.feature.messaging.ui.common.WaAvatar
import org.hogwarts.android.feature.messaging.ui.common.WaHeader
import org.hogwarts.android.feature.messaging.ui.common.WaIcon
import org.hogwarts.android.feature.messaging.ui.common.bottomHairline
import org.hogwarts.android.feature.messaging.ui.common.tabBarClearance
import org.hogwarts.android.feature.messaging.ui.common.topInset
import org.hogwarts.android.feature.messaging.ui.common.wa
import org.hogwarts.android.feature.messaging.ui.common.waType

/** What the chat list draws; built by the shell from its state. */
data class ChatListModel(
    val rows: List<ChatRowData>,
    val archivedCount: Int,
    val query: String,
    val filter: ChatFilter,
    val selecting: Boolean,
    val selected: Set<String>,
    val showNotice: Boolean,
)

class ChatListActions(
    val onOpenChat: (String) -> Unit = {},
    val onExit: () -> Unit = {},
    val onQuery: (String) -> Unit = {},
    val onFilter: (ChatFilter) -> Unit = {},
    val onStartSelecting: () -> Unit = {},
    val onStopSelecting: () -> Unit = {},
    val onToggleSelected: (String) -> Unit = {},
    val onReadSelected: () -> Unit = {},
    val onReadAll: () -> Unit = {},
    val onNoticeAction: () -> Unit = {},
    val onNoticeDismiss: () -> Unit = {},
)

/**
 * Port of `ios-chat-list.tsx`: the floating header, the title block (big
 * title, search, the notification card, filter chips), the rows and the
 * encryption footer, all in one scroller that passes under header and tab bar.
 */
@Composable
fun ChatListPane(
    model: ChatListModel,
    actions: ChatListActions,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val collapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > with(density) { 34.dp.toPx() }
        }
    }
    var menuOpen by remember { mutableStateOf(false) }
    val title = stringResource(R.string.messages_title)

    Box(modifier.fillMaxSize().background(wa.surfacePrimary)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.glassBackdropSource(backdrop) else Modifier),
            contentPadding = PaddingValues(top = topInset() + 56.dp, bottom = tabBarClearance()),
        ) {
            item("title") {
                TitleBlock(model, actions, title)
            }
            item("rows-top") { Spacer(Modifier.height(8.dp)) }
            if (model.archivedCount > 0) {
                item("archived") { ArchivedRow(stringResource(R.string.messages_archived), model.archivedCount) }
            }
            items(model.rows, key = { it.id }) { row ->
                ChatRow(
                    row = row,
                    selectable = model.selecting,
                    selected = row.id in model.selected,
                    onClick = {
                        if (model.selecting) actions.onToggleSelected(row.id) else actions.onOpenChat(row.id)
                    },
                )
            }
            item("encrypt") { InfoEncrypt(Modifier.padding(top = 18.dp, bottom = 32.dp)) }
        }

        WaHeader(
            collapsingTitle = title,
            collapsed = collapsed,
            exitLabel = stringResource(R.string.messages_tab_back),
            onExit = actions.onExit,
            optionsLabel = stringResource(R.string.messages_more_options),
            optionsMenu = listOf(
                HeaderMenuItem("select-chats", stringResource(R.string.messages_select_chats), SelectChatsGlyph, actions.onStartSelecting),
                HeaderMenuItem("read-all", stringResource(R.string.messages_read_all), ReadAllGlyph, actions.onReadAll),
            ),
            menuOpen = menuOpen,
            onMenuOpenChange = { menuOpen = it },
            // The web mounts the camera and compose discs with no handler behind them.
            cameraLabel = stringResource(R.string.messages_camera),
            addLabel = stringResource(R.string.messages_new_chat),
            selectBar = if (model.selecting) SelectBar(
                doneLabel = stringResource(R.string.messages_done),
                onDone = actions.onStopSelecting,
                actionLabel = stringResource(R.string.messages_mark_read),
                onAction = actions.onReadSelected,
                actionDisabled = model.selected.isEmpty(),
            ) else null,
            backdrop = backdrop,
        )
    }
}

/** `IosTitleBlock`. */
@Composable
private fun TitleBlock(model: ChatListModel, actions: ChatListActions, title: String) {
    Column(
        Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = waType(28f, 28f, FontWeight.Bold, -1.1f), color = wa.textPrimary)
            SearchField(model.query, actions.onQuery)
        }
        if (model.showNotice) {
            NoticeCard(actions.onNoticeAction, actions.onNoticeDismiss)
        }
        FilterChips(model.filter, actions.onFilter)
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit) {
    val placeholder = stringResource(R.string.messages_search_placeholder)
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(wa.surfaceSearchChat)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        WaIcon(R.drawable.ic_wa_search_24, 16.dp, wa.textSecondary, description = stringResource(R.string.messages_search))
        val style = waType(16.4f, 20f).copy(color = wa.textPrimary)
        BasicTextField(
            value = query,
            onValueChange = onQuery,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(wa.surfaceProduct),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) Text(placeholder, style = style, color = wa.textSecondary, maxLines = 1)
                    inner()
                }
            },
        )
    }
}

/** `IosFilterChips`: word-wide chips in their own scroller, closed by a "+" chip. */
@Composable
private fun FilterChips(active: ChatFilter, onChange: (ChatFilter) -> Unit) {
    val labels = listOf(
        ChatFilter.All to stringResource(R.string.messages_filter_all),
        ChatFilter.Unread to stringResource(R.string.messages_filter_unread),
        ChatFilter.Favourites to stringResource(R.string.messages_filter_favourites),
        ChatFilter.Groups to stringResource(R.string.messages_filter_groups),
    )
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEach { (id, label) ->
            val on = id == active
            Box(
                Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(if (on) wa.surfaceCtaFiltersActive else Color.Transparent)
                    .border(1.dp, if (on) wa.borderCtaFiltersActive else wa.borderCtaFilters, RoundedCornerShape(19.dp))
                    .clickable { onChange(id) }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = waType(14f, 19f, FontWeight.SemiBold, -0.14f),
                    color = if (on) wa.textCtaFiltersActive else wa.textCtaFilters,
                    maxLines = 1,
                )
            }
        }
        val more = stringResource(R.string.messages_more_filters)
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, wa.borderCtaFilters, RoundedCornerShape(19.dp))
                .semantics { contentDescription = more },
            contentAlignment = Alignment.Center,
        ) {
            WaIcon(R.drawable.ic_wa_plus_filter_24, 24.dp, wa.textPrimary)
        }
    }
}

/** `IosNoticeCard`: asks for notification permission until granted or dismissed. */
@Composable
private fun NoticeCard(onAction: () -> Unit, onDismiss: () -> Unit) {
    val title = stringResource(R.string.messages_notice_title)
    val body = stringResource(R.string.messages_notice_body)
    val action = stringResource(R.string.messages_notice_action)
    val dismiss = stringResource(R.string.messages_notice_dismiss)
    val product = wa.textProduct
    Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .dropShadow(RoundedCornerShape(14.dp), Shadow(6.dp, Color.Black.copy(alpha = 0.10f), 0.dp, DpOffset(0.dp, 1.dp)))
                .clip(RoundedCornerShape(14.dp))
                .background(wa.surfacePrimary)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(BellGlyph, null, Modifier.padding(top = 2.dp).size(32.dp), colorFilter = ColorFilter.tint(wa.surfaceProduct))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = waType(16f, 21f, FontWeight.SemiBold, -0.32f), color = wa.textPrimary)
                val text = buildAnnotatedString {
                    append(body)
                    append(" ")
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "turn-on",
                            styles = TextLinkStyles(SpanStyle(color = product, fontWeight = FontWeight.SemiBold)),
                        ) { onAction() }
                    ) { append(action) }
                }
                Text(text, style = waType(15f, 20f), color = wa.textPrimary)
            }
            Image(
                CrossGlyph,
                dismiss,
                Modifier.size(24.dp).clip(CircleShape).clickable(onClick = onDismiss).padding(2.dp),
                colorFilter = ColorFilter.tint(wa.textSecondary),
            )
        }
    }
}

/** `IosArchivedRow`. */
@Composable
private fun ArchivedRow(label: String, count: Int) {
    Row(
        Modifier.fillMaxWidth().padding(start = 32.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(28.66.dp),
    ) {
        WaIcon(R.drawable.ic_wa_archived_24, 24.dp, wa.textSecondary)
        Row(
            Modifier
                .weight(1f)
                .bottomHairline(wa.borderSeparator)
                .padding(end = 15.dp, top = 2.5.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = waType(16f, 20f, FontWeight.SemiBold, -0.32f), color = wa.textPrimary, maxLines = 1)
            Text(count.toString(), style = waType(14f, 20f), color = wa.textSecondary)
        }
    }
}

/** `IosChatRow`. */
@Composable
internal fun ChatRow(row: ChatRowData, selectable: Boolean, selected: Boolean, onClick: () -> Unit) {
    val hasUnread = row.unreadCount > 0
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.66.dp),
    ) {
        if (selectable) {
            Box(
                Modifier
                    .padding(top = 19.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.background(wa.surfaceProduct)
                        else Modifier.border(1.5.dp, wa.borderCtaFilters, CircleShape)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Image(SingleTickGlyph, null, Modifier.size(12.dp), colorFilter = ColorFilter.tint(wa.textInvert))
                }
            }
        }
        Box(Modifier.padding(top = 2.dp)) {
            WaAvatar(url = row.avatarUrl, size = 56.dp, colorKey = row.avatarKey, group = row.isGroup)
        }
        Row(
            Modifier
                .weight(1f)
                .height(76.dp)
                .bottomHairline(wa.borderSeparator),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                Text(
                    row.name,
                    style = waType(16f, 20f, FontWeight.SemiBold, -0.32f),
                    color = wa.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 200.dp),
                )
                MessagePreview(row.preview, row.previewLeading)
            }
            Column(
                Modifier.width(60.dp).padding(end = 15.dp, top = 1.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    row.timestamp,
                    style = waType(14f, 19f, tracking = -0.14f),
                    color = if (hasUnread) wa.textProduct else wa.textSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                    // `whitespace-nowrap` in a 60px `items-end` column: a long
                    // "Yesterday" spills toward the text, never past the edge.
                    modifier = Modifier.wrapContentWidth(Alignment.End, unbounded = true),
                )
                if (hasUnread) {
                    Box(
                        Modifier
                            .defaultMinSize(minWidth = 16.dp)
                            .clip(CircleShape)
                            .background(wa.surfaceProduct)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(row.unreadCount.toString(), style = waType(12f, 14f, tracking = -0.12f), color = wa.textInvert)
                    }
                } else if (row.pinned) {
                    WaIcon(R.drawable.ic_wa_pin_16, 16.dp, wa.textSecondary, description = stringResource(R.string.messages_pinned))
                }
            }
        }
    }
}

/** `IosMessagePreview`: two lines, a tick glyph hung ahead of the first. */
@Composable
private fun MessagePreview(text: String, leading: PreviewLeading?) {
    Box(Modifier.fillMaxWidth()) {
        if (leading != null) {
            WaIcon(
                if (leading == PreviewLeading.CheckRead) R.drawable.ic_wa_check_read_19 else R.drawable.ic_wa_check_sent_19,
                19.dp,
                if (leading == PreviewLeading.CheckRead) wa.textProduct else wa.textSecondary,
            )
        }
        Text(
            text,
            style = waType(14f, 19f, tracking = -0.14f),
            color = wa.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = if (leading != null) 21.5.dp else 0.dp),
        )
    }
}

/** `IosInfoEncrypt`. */
@Composable
private fun InfoEncrypt(modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        WaIcon(R.drawable.ic_wa_lock_12, 12.dp, wa.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val style = waType(11f, 14f)
            Text(stringResource(R.string.messages_encrypt_prefix), style = style, color = wa.textSecondary)
            Text(stringResource(R.string.messages_encrypt_topic), style = style, color = wa.textSecondary)
            Text(stringResource(R.string.messages_encrypt_suffix), style = style, color = wa.textSecondary)
            Text(stringResource(R.string.messages_encrypt_tail), style = style, color = wa.textProduct)
        }
    }
}
