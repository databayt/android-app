package org.hogwarts.android.feature.messaging.ui.thread

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import org.hogwarts.android.core.designsystem.theme.BrandFonts
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.ui.common.ClockGlyph
import org.hogwarts.android.feature.messaging.ui.common.MapPinGlyph
import org.hogwarts.android.feature.messaging.ui.common.NoticeLockGlyph
import org.hogwarts.android.feature.messaging.ui.common.PlayGlyph
import org.hogwarts.android.feature.messaging.ui.common.SingleTickGlyph
import org.hogwarts.android.feature.messaging.ui.common.WaIcon
import org.hogwarts.android.feature.messaging.ui.common.wa
import org.hogwarts.android.feature.messaging.ui.common.waType
import kotlin.math.ceil

/** `bubble-tail.tsx`: one concave sweep, 13.4 × 18 drawn into 15 × 18, pointing right. */
private val TAIL_PATH = PathParser().parsePathString("M0 0H7.5C7.5 7 9.2 13.2 13.1 16.9C13.5 17.3 13.3 18 12.7 18H0Z").toPath()

/**
 * The tail hangs off the bubble's bottom corner on the sender's side,
 * `-end-[7.5px]` for mine and `-start-[7.5px]` flipped for theirs; logical, so
 * both follow the bubbles across under RTL.
 */
private fun Modifier.bubbleTail(side: Side, color: Color, rtl: Boolean): Modifier = drawBehind {
    val w = 15.dp.toPx()
    val h = 18.dp.toPx()
    val inset = 7.5.dp.toPx()
    // Physical right edge for "me" in LTR and "other" in RTL.
    val atRight = (side == Side.Me) != rtl
    val left = if (atRight) size.width - inset else inset - w
    translate(left, size.height - h) {
        scale(scaleX = (w / 13.4f) * (if (atRight) 1f else -1f), scaleY = h / 18f, pivot = Offset.Zero) {
            if (atRight) {
                drawPath(TAIL_PATH, color)
            } else {
                translate(-13.4f, 0f) { drawPath(TAIL_PATH, color) }
            }
        }
    }
}

@Composable
private fun rtl() = LocalLayoutDirection.current == LayoutDirection.Rtl

@Composable
private fun bubbleColor(side: Side) = if (side == Side.Me) wa.surfaceBaloonMe else wa.surfaceBaloonOther

/** `date-separator.tsx`. */
@Composable
internal fun DateSeparator(label: String) {
    Box(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 14.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(wa.surfaceDate)
                .padding(horizontal = 12.dp, vertical = 3.dp),
        ) {
            Text(label, style = waType(13f, 15f, FontWeight.SemiBold), color = wa.textPrimary)
        }
    }
}

/** `encryption-notice.tsx`: the cream card that opens every thread. */
@Composable
internal fun EncryptionNotice() {
    val text = stringResource(R.string.messages_encryption_notice)
    val more = stringResource(R.string.messages_encryption_learn_more)
    Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 13.dp), contentAlignment = Alignment.Center) {
        val lock = "lock"
        val annotated = androidx.compose.ui.text.buildAnnotatedString {
            appendInlineContent(lock, "🔒")
            append(text)
            append(" ")
            pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.SemiBold))
            append(more)
            pop()
        }
        val noticeInk = wa.textNotice
        Text(
            annotated,
            style = waType(14.5f, 20f),
            color = noticeInk,
            textAlign = TextAlign.Center,
            inlineContent = mapOf(
                lock to androidx.compose.foundation.text.InlineTextContent(
                    androidx.compose.ui.text.Placeholder(15.5f.toSp(), 12.5f.toSp(), androidx.compose.ui.text.PlaceholderVerticalAlign.TextCenter)
                ) {
                    Image(NoticeLockGlyph, null, Modifier.padding(end = 5.dp).size(10.5.dp, 12.5.dp), colorFilter = ColorFilter.tint(noticeInk))
                }
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(wa.surfaceNotice)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

private fun Float.toSp() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)

/** `bubble-timestamp.tsx`: the clock time and the tick state, inline. */
@Composable
internal fun BubbleTimestamp(time: String, status: BubbleStatus?, modifier: Modifier = Modifier, ink: Color = wa.textSecondaryAlpha) {
    val label = when (status) {
        BubbleStatus.Sending -> stringResource(R.string.messages_status_sending)
        BubbleStatus.Sent -> stringResource(R.string.messages_status_sent)
        BubbleStatus.Delivered -> stringResource(R.string.messages_status_delivered)
        BubbleStatus.Read -> stringResource(R.string.messages_status_read)
        BubbleStatus.Failed, null -> null
    }
    Row(
        modifier.then(if (label != null) Modifier.semantics { contentDescription = label } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // The clock is Latin digits in both locales, so it keeps the Latin face:
        // the Arabic face's taller line box would push every bubble 10dp down.
        Text(
            time,
            style = waType(11.5f, 11.5f).copy(
                fontFamily = BrandFonts.LatinText,
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
            ),
            color = ink,
        )
        when (status) {
            BubbleStatus.Sending -> Image(ClockGlyph, null, Modifier.size(12.dp), colorFilter = ColorFilter.tint(ink))
            BubbleStatus.Sent -> Image(SingleTickGlyph, null, Modifier.size(13.dp, 10.dp), colorFilter = ColorFilter.tint(ink))
            BubbleStatus.Delivered, BubbleStatus.Read -> WaIcon(
                R.drawable.ic_wa_check_bubble_17,
                17.dp,
                if (status == BubbleStatus.Read) wa.readTick else ink,
            )
            else -> Unit
        }
    }
}

/**
 * Text that hugs its longest line (`useHugLongestLine`): a wrapped paragraph
 * otherwise keeps the full width it wrapped at and leaves a gap beside its
 * shorter lines, where the iOS app narrows the bubble.
 */
@Composable
private fun HugText(text: String, style: TextStyle, color: Color, maxWidth: Dp) {
    val measurer = rememberTextMeasurer()
    Layout(content = { Text(text, style = style, color = color) }) { measurables, constraints ->
        val max = minOf(constraints.maxWidth, maxWidth.roundToPx())
        val result = measurer.measure(text, style, constraints = Constraints(maxWidth = max))
        val widest = if (result.lineCount <= 1) result.size.width
        else (0 until result.lineCount).maxOf { ceil(result.getLineRight(it) - result.getLineLeft(it)).toInt() }
        val placeable = measurables.first().measure(Constraints.fixedWidth(widest.coerceAtMost(max)).copy(minHeight = 0, maxHeight = constraints.maxHeight))
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
}

/** The red mark beside a message the server did not take. */
@Composable
private fun FailedMark() {
    Box(Modifier.size(20.dp).clip(CircleShape).background(wa.failedMark), contentAlignment = Alignment.Center) {
        Text("!", style = waType(13f, 13f, FontWeight.Bold), color = Color.White)
    }
}

/** `message-bubble.tsx`. */
@Composable
internal fun TextBubble(item: ChatItem.Text, onRetry: (() -> Unit)?) {
    val isMe = item.side == Side.Me
    val failed = item.status == BubbleStatus.Failed
    val rtl = rtl()
    val corner = 20.dp
    val shape = if (!item.tail) RoundedCornerShape(corner) else if (isMe) {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomEnd = 4.dp, bottomStart = corner)
    } else {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomEnd = corner, bottomStart = 4.dp)
    }
    val retry = stringResource(R.string.messages_not_sent)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = if (item.tail) 14.dp else 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, if (isMe) Alignment.End else Alignment.Start),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (failed) FailedMark()
        val fill = bubbleColor(item.side)
        Column(
            Modifier
                .widthIn(min = 64.dp, max = 283.dp)
                .then(if (item.tail) Modifier.bubbleTail(item.side, fill, rtl) else Modifier)
                .clip(shape)
                .background(fill)
                .border(0.33.dp, wa.surfaceShadowBaloon, shape)
                .then(if (failed && onRetry != null) Modifier.clickable(onClick = onRetry).semantics { contentDescription = retry } else Modifier)
                .padding(start = 12.dp, end = 12.dp, top = 7.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (item.senderName != null && !isMe) {
                Text(item.senderName, style = waType(12.8f, 16f, FontWeight.SemiBold, -0.13f), color = wa.surfaceProduct)
            }
            HugText(item.text, waType(17f, 24f, tracking = -0.2f), wa.textPrimary, 259.dp)
            BubbleTimestamp(
                item.time,
                if (isMe) item.status ?: BubbleStatus.Sent else null,
                Modifier.align(Alignment.End).heightIn(min = 15.dp),
            )
        }
    }
}

/** `reply-bubble.tsx`. */
@Composable
internal fun ReplyBubble(item: ChatItem.Reply) {
    val isMe = item.side == Side.Me
    val fill = bubbleColor(item.side)
    val rtl = rtl()
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .widthIn(max = 287.dp)
                .bubbleTail(item.side, fill, rtl)
                .clip(shape)
                .background(fill)
                .border(0.66.dp, wa.surfaceShadowBaloon, shape)
                .padding(start = 6.dp, end = 6.dp, top = 5.dp, bottom = 6.5.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    Modifier
                        .widthIn(min = 200.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.Black.copy(alpha = 0.05f)),
                ) {
                    Box(Modifier.width(4.dp).height(42.dp).background(wa.replyAccent))
                    Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)) {
                        Text(item.replySenderName, style = waType(13f, 17f, FontWeight.SemiBold, -0.13f), color = wa.replyAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.replyText, style = waType(13.2f, 17f, tracking = -0.13f), color = wa.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                // The trailing gap keeps the last line clear of the timestamp.
                Text(
                    item.text + if (isMe) "    " else "   ",
                    style = waType(15.8f, 21f, tracking = -0.21f),
                    color = wa.textPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            BubbleTimestamp(
                item.time,
                if (isMe) item.status ?: BubbleStatus.Sent else null,
                Modifier.align(Alignment.BottomEnd).padding(end = 2.dp),
            )
        }
    }
}

/** `voice-note-bubble.tsx`. The API returns no duration, so the label is the web's `0:00`. */
@Composable
internal fun VoiceBubble(item: ChatItem.Voice) {
    val isMe = item.side == Side.Me
    val fill = bubbleColor(item.side)
    val rtl = rtl()
    val shape = RoundedCornerShape(20.dp)
    val bars = remember { listOf(5, 9, 14, 20, 16, 10, 7, 12, 18, 22, 16, 9, 6, 12, 18, 20, 14, 8, 5, 10, 16, 22, 18, 12, 7, 5) }
    val play = stringResource(R.string.messages_play_voice)
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .width(287.dp)
                .bubbleTail(item.side, fill, rtl)
                .clip(shape)
                .background(fill)
                .border(0.66.dp, wa.surfaceShadowBaloon, shape)
                .padding(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFD4D4D4)), contentAlignment = Alignment.Center) {
                    if (item.avatarUrl != null) {
                        AsyncImage(item.avatarUrl, null, Modifier.size(40.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    } else {
                        Text(item.avatarFallback, style = waType(14f, 14f, FontWeight.SemiBold), color = Color.White)
                    }
                }
                Image(PlayGlyph, play, Modifier.size(24.dp).padding(3.dp), colorFilter = ColorFilter.tint(wa.textPrimary))
                Box(Modifier.weight(1f)) {
                    val ink = wa.textSecondaryAlpha
                    Canvas(Modifier.fillMaxWidth().height(28.dp).padding(end = 8.dp)) {
                        val step = 4.dp.toPx()
                        bars.forEachIndexed { i, h ->
                            val x = i * step
                            if (x > size.width) return@forEachIndexed
                            val bh = h.dp.toPx()
                            drawRoundRect(
                                ink,
                                topLeft = Offset(if (layoutDirection == LayoutDirection.Rtl) size.width - x - 2.dp.toPx() else x, (size.height - bh) / 2),
                                size = androidx.compose.ui.geometry.Size(2.dp.toPx(), bh),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()),
                            )
                        }
                    }
                    Text(item.durationLabel, style = waType(11f, 13f), color = ink, modifier = Modifier.offset(y = 18.dp))
                }
            }
            BubbleTimestamp(item.time, if (isMe) item.status ?: BubbleStatus.Sent else null, Modifier.align(Alignment.BottomEnd).padding(end = 2.dp))
        }
    }
}

/** `location-bubble.tsx`: no map image comes with the message, so the grey ground and pin. */
@Composable
internal fun LocationBubble(item: ChatItem.Location) {
    val isMe = item.side == Side.Me
    val fill = bubbleColor(item.side)
    val rtl = rtl()
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .width(240.dp)
                .bubbleTail(item.side, fill, rtl)
                .clip(shape)
                .background(fill)
                .border(0.66.dp, wa.surfaceShadowBaloon, shape),
        ) {
            Box(Modifier.fillMaxWidth().height(130.dp).background(Color(0xFFE5E5E5)), contentAlignment = Alignment.Center) {
                Image(MapPinGlyph, null, Modifier.size(28.dp), colorFilter = ColorFilter.tint(Color(0xFFE83E3E)))
            }
            BubbleTimestamp(
                item.time,
                if (isMe) item.status ?: BubbleStatus.Sent else null,
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                ink = Color.White,
            )
        }
    }
}

internal typealias BubbleSlot = @Composable BoxScope.() -> Unit
