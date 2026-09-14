package org.hogwarts.android.feature.messaging.ui.thread

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.hogwarts.android.core.designsystem.kit.glassSurface
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.ui.common.ChevronDownGlyph
import org.hogwarts.android.feature.messaging.ui.common.GlassDisc
import org.hogwarts.android.feature.messaging.ui.common.WaIcon
import org.hogwarts.android.feature.messaging.ui.common.mirrorInRtl
import org.hogwarts.android.feature.messaging.ui.common.topInset
import org.hogwarts.android.feature.messaging.ui.common.wa
import org.hogwarts.android.feature.messaging.ui.common.waType
import org.hogwarts.android.feature.messaging.ui.format.MessagingFormat

@Composable
fun ThreadRoute(
    onBack: () -> Unit,
    viewModel: ThreadViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var draft by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(viewModel) { if (draft == null) draft = viewModel.initialDraft() }
    ThreadContent(
        state = state,
        draft = draft.orEmpty(),
        onDraftChange = {
            draft = it
            viewModel.saveDraft(it)
        },
        onSend = {
            viewModel.send(it)
            draft = ""
        },
        onBack = onBack,
        onRetry = viewModel::retry,
        onLoadOlder = viewModel::loadOlder,
        onActivity = viewModel::touch,
    )
}

/** What `MessagesView` needs beyond the messages: the header's name and photo. */
@Composable
fun ThreadContent(
    state: ThreadUiState,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: (String) -> Unit,
    onLoadOlder: () -> Unit,
    onActivity: () -> Unit,
    modifier: Modifier = Modifier,
    format: MessagingFormat = MessagingFormat(LocalConfiguration.current.locales[0]),
) {
    val labels = AdaptLabels(
        today = stringResource(R.string.messages_today),
        yesterday = stringResource(R.string.messages_yesterday),
        deleted = stringResource(R.string.messages_deleted),
        photo = stringResource(R.string.messages_photo),
        video = stringResource(R.string.messages_video),
        voice = stringResource(R.string.messages_voice_message),
        document = stringResource(R.string.messages_document),
        attachment = stringResource(R.string.messages_attachment),
        userFallback = stringResource(R.string.messages_user_fallback),
    )
    val conversation = state.conversation
    val isGroup = conversation?.isGroup ?: false
    val items = remember(state.messages, state.currentUserId, isGroup, format, labels) {
        toChatItems(state.messages, state.currentUserId, isGroup, format, labels)
    }
    val name = conversation?.title ?: stringResource(if (isGroup) R.string.messages_group_fallback else R.string.messages_user_fallback)

    Column(modifier.fillMaxSize().background(wa.wallpaper)) {
        Box(Modifier.weight(1f).fillMaxWidth().chatWallpaper()) {
            MessagesList(items, state, onRetry, onLoadOlder, onActivity)
            TopContactHeader(name = name, avatarUrl = conversation?.avatarUrl, onBack = onBack)
        }
        InputBar(draft, onDraftChange, onSend)
    }
}

/**
 * The thread, newest at the bottom. Laid out bottom-up (`reverseLayout`) so it
 * opens at the latest message and an older page arriving above never moves
 * the reader — what `messages-view.tsx` does by hand with scroll anchoring.
 */
@Composable
private fun MessagesList(
    items: List<ChatItem>,
    state: ThreadUiState,
    onRetry: (String) -> Unit,
    onLoadOlder: () -> Unit,
    onActivity: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val nearBottomPx = with(density) { 80.dp.toPx() }
    val nearBottom by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < nearBottomPx }
    }
    var unseen by remember { mutableIntStateOf(0) }
    val reversed = remember(items) { items.asReversed() }
    // The encryption card follows the thread's first date pill once no older page is left.
    val noticeAfter = if (items.firstOrNull() is ChatItem.Date) items.first().id else null

    // Arrivals: follow them at the bottom (or when they are mine), else count them.
    var lastNewest by remember { mutableStateOf(items.lastOrNull()?.id) }
    LaunchedEffect(items.lastOrNull()?.id) {
        val newest = items.lastOrNull() ?: return@LaunchedEffect
        if (lastNewest != null && newest.id != lastNewest) {
            val mine = (newest as? ChatItem.Text)?.side == Side.Me || (newest as? ChatItem.Reply)?.side == Side.Me
            if (nearBottom || mine) listState.animateScrollToItem(0) else unseen++
        }
        lastNewest = newest.id
    }
    LaunchedEffect(nearBottom) { if (nearBottom) unseen = 0 }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .filter { it >= listState.layoutInfo.totalItemsCount - 12 }
            .collect { onLoadOlder() }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.filter { it }.collect { onActivity() }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topInset() + 70.dp, bottom = 8.dp),
        ) {
            items(reversed, key = { it.id }) { item ->
                Column {
                    if (item.id == noticeAfter && !state.hasMore && state.loaded) {
                        // Reversed layout: the date pill is drawn first, the card below it.
                        ItemView(item, onRetry)
                        EncryptionNotice()
                    } else {
                        ItemView(item, onRetry)
                    }
                }
            }
            if (state.loadingOlder && state.hasMore) {
                item("loading-older") {
                    val label = stringResource(R.string.messages_loading_older)
                    Box(Modifier.fillMaxWidth().padding(top = 8.dp).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(wa.surfaceDate), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = wa.textSecondaryAlpha, strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }

        if (!nearBottom && listState.canScrollBackward) {
            val label = stringResource(R.string.messages_scroll_to_bottom)
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 12.dp),
            ) {
                GlassDisc(label, {
                    unseen = 0
                    scope.launch { listState.animateScrollToItem(0) }
                }, size = 40.dp, chatTint = true) {
                    Image(ChevronDownGlyph, null, Modifier.size(20.dp), colorFilter = ColorFilter.tint(wa.textPrimary))
                }
                if (unseen > 0) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-6).dp)
                            .height(18.dp)
                            .defaultMinSize(minWidth = 18.dp)
                            .clip(CircleShape)
                            .background(wa.surfaceProduct)
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (unseen > 99) "99+" else unseen.toString(), style = waType(11f, 11f, FontWeight.SemiBold), color = wa.textInvert)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemView(item: ChatItem, onRetry: (String) -> Unit) {
    when (item) {
        is ChatItem.Date -> DateSeparator(item.label)
        is ChatItem.Text -> TextBubble(item, onRetry = if (item.status == BubbleStatus.Failed) ({ onRetry(item.id) }) else null)
        is ChatItem.Reply -> ReplyBubble(item)
        is ChatItem.Voice -> VoiceBubble(item)
        is ChatItem.Location -> LocationBubble(item)
    }
}

/**
 * `chat-wallpaper.tsx`: the doodle tile repeated at 432dp wide. The web's SVG
 * stacks the pattern at 10% `difference` over the ground colour; that
 * composite is baked into the tile, one per theme.
 */
@Composable
private fun Modifier.chatWallpaper(): Modifier {
    val dark = wa.surfacePrimary.run { (0.2126f * red + 0.7152f * green + 0.0722f * blue) < 0.5f }
    val tile: ImageBitmap = ImageBitmap.imageResource(if (dark) R.drawable.wa_chat_wallpaper_dark else R.drawable.wa_chat_wallpaper_light)
    val ground = wa.wallpaper
    return drawBehind {
        drawRect(ground)
        val w = 432.dp.toPx()
        val h = w * tile.height / tile.width
        val dst = IntSize(w.toInt(), h.toInt())
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawImage(tile, dstOffset = IntOffset(x.toInt(), y.toInt()), dstSize = dst)
                x += dst.width
            }
            y += dst.height
        }
    }
}

/**
 * `top-contact-header.tsx`: no bar, glass controls over the wallpaper — the
 * back disc, the photo and name, and the video/voice capsule — above a frosted
 * scroll-edge strip that fades bubbles out under the name.
 */
@Composable
private fun TopContactHeader(name: String, avatarUrl: String?, onBack: () -> Unit) {
    val top = topInset()
    val edge = wa.scrollEdge
    Box(Modifier.fillMaxWidth().height(top + 72.dp)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to edge,
                        0.55f to edge.copy(alpha = if (edge.alpha > 0.9f) 0.72f else edge.alpha * 0.78f),
                        1f to edge.copy(alpha = 0f),
                    )
                )
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(top + 56.dp)
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            GlassDisc(stringResource(R.string.messages_back), onBack, chatTint = true) {
                WaIcon(R.drawable.ic_wa_chevron_lt_32, 34.dp, wa.glassGlyph, Modifier.mirrorInRtl())
            }
            Row(
                Modifier.weight(1f).height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD4D4D4)) // bg-neutral-300
                        .border(0.212.dp, wa.borderAvatar, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(avatarUrl, null, Modifier.size(40.dp), contentScale = ContentScale.Crop)
                    } else {
                        Text(name.take(1).uppercase(), style = waType(15f, 15f, FontWeight.SemiBold), color = Color.White)
                    }
                }
                Text(name, style = waType(17f, 22f, FontWeight.SemiBold, -0.34f), color = wa.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // The web mounts the call capsule with no handlers behind it.
            Row(
                Modifier
                    .width(102.dp)
                    .height(44.dp)
                    .glassSurface(CircleShape, wa.glassBgChat, blur = 7.dp)
                    .padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(17.dp),
            ) {
                WaIcon(R.drawable.ic_wa_video_32, 32.dp, wa.glassGlyph, description = stringResource(R.string.messages_video_call))
                WaIcon(R.drawable.ic_wa_phone_32, 32.dp, wa.glassGlyph, description = stringResource(R.string.messages_voice_call))
            }
        }
    }
}

/**
 * `input-bar.tsx`: attach, a field that grows to five lines with the sticker
 * glyph inside it, then camera and the green mic disc — which become the
 * green send disc once there is text. Return inserts a line on a touch
 * keyboard, as on the phone web; the disc sends.
 */
@Composable
private fun InputBar(value: String, onChange: (String) -> Unit, onSend: (String) -> Unit) {
    val hasText = value.isNotBlank()
    Column(
        Modifier
            .fillMaxWidth()
            .background(wa.surfacePanel)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 7.dp, end = 9.dp, top = 5.5.dp, bottom = 5.5.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WaIcon(R.drawable.ic_wa_plus_input_32, 32.dp, wa.textPrimary, description = stringResource(R.string.messages_attach))
            Row(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(15.dp))
                    .background(wa.surfaceInputChat)
                    .border(0.33.dp, wa.borderInputChat, RoundedCornerShape(15.dp))
                    .padding(start = 10.dp, end = 9.dp, top = 3.dp, bottom = 2.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val style = waType(16f, 21f, tracking = -0.32f).copy(color = wa.textPrimary)
                val placeholder = stringResource(R.string.messages_input_placeholder)
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    textStyle = style,
                    cursorBrush = SolidColor(wa.surfaceProduct),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    maxLines = 5,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 25.dp, max = 105.dp)
                        .padding(bottom = 3.dp),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) Text(placeholder, style = style, color = wa.textSecondaryAlpha, maxLines = 1)
                            inner()
                        }
                    },
                )
                WaIcon(
                    R.drawable.ic_wa_sticker_24,
                    24.dp,
                    wa.textSecondary,
                    Modifier.align(Alignment.CenterVertically),
                    description = stringResource(R.string.messages_stickers),
                )
            }
            Row(Modifier.padding(start = 6.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                if (hasText) {
                    val send = stringResource(R.string.messages_send)
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(wa.surfaceProduct)
                            .clickable { onSend(value) }
                            .semantics { contentDescription = send },
                        contentAlignment = Alignment.Center,
                    ) {
                        // `tint={false}`: the send glyph stays white in both themes.
                        WaIcon(R.drawable.ic_wa_send_24, 24.dp, Color.White)
                    }
                } else {
                    WaIcon(R.drawable.ic_wa_camera_small_32, 32.dp, wa.textPrimary, description = stringResource(R.string.messages_camera))
                    Box(
                        Modifier.size(32.dp).clip(CircleShape).background(wa.surfaceProduct),
                        contentAlignment = Alignment.Center,
                    ) {
                        WaIcon(R.drawable.ic_wa_mic_32, 23.dp, wa.textInvert, description = stringResource(R.string.messages_voice_message))
                    }
                }
            }
        }
    }
}

@Suppress("unused")
private val ZeroOffset = Offset.Zero
