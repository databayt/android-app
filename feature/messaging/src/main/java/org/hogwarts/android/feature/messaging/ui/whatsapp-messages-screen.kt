package org.hogwarts.android.feature.messaging.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.core.designsystem.theme.WhatsAppColors

/// iOS WhatsApp-style chat / messages screen (Android).
/// Mirrors Figma: EKtlCVUnSrIQ8G8fO0msMu node 86:6042
@Composable
fun WhatsAppMessagesScreen(
    contactName: String,
    items: List<WaChatItem>,
    contactSubtitle: String = "tap here for contact info",
    unreadCount: Int? = null,
    onBack: () -> Unit = {},
    onVideo: () -> Unit = {},
    onPhone: () -> Unit = {},
    onSend: (String) -> Unit = {},
    replyDraft: WaReplyDraft? = null,
    draftText: String = "",
    modifier: Modifier = Modifier,
) {
    val wa = LocalWhatsAppColors.current
    var messageText by remember { mutableStateOf(draftText) }

    Column(modifier.fillMaxSize()) {
        WaMessagesHeader(contactName, contactSubtitle, unreadCount, onBack, onVideo, onPhone, wa)

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF5F2EB)),
        ) {
            // TODO: render wallpaper SVG — add coil-svg dependency and use
            // AsyncImage(model = "https://d1dlwtcfl0db67.cloudfront.net/wallpapers/wp-wa-chat-bg.svg")
            // or convert wp-wa-chat-bg.svg to a vector drawable via AS import.
            // Solid cream #F5F2EB matches the SVG base color.
            LazyColumn(Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { item ->
                    WaChatItemRow(item, wa)
                }
            }
        }

        WaMessagesInputBar(
            value = messageText,
            onChange = { messageText = it },
            onSend = {
                val t = messageText.trim()
                if (t.isNotEmpty()) { onSend(t); messageText = "" }
            },
            replyDraft = replyDraft,
            wa = wa,
        )
    }
}

data class WaReplyDraft(
    val senderName: String,
    val text: String,
    val onClose: () -> Unit,
)

// MARK: - Data Model

sealed class WaChatItem {
    abstract val id: String

    data class Date(override val id: String, val label: String) : WaChatItem()
    data class TextMsg(
        override val id: String,
        val side: WaSide,
        val text: String,
        val time: String,
        val status: WaBubbleStatus? = null,
        val senderName: String? = null,
        val tail: Boolean = true,
        val reactions: List<String> = emptyList(),
    ) : WaChatItem()
    data class Reply(
        override val id: String,
        val side: WaSide,
        val text: String,
        val time: String,
        val status: WaBubbleStatus? = null,
        val replySenderName: String,
        val replyText: String,
    ) : WaChatItem()
    data class Voice(
        override val id: String,
        val side: WaSide,
        val avatarFallback: String = "?",
        val durationLabel: String,
        val time: String,
        val status: WaBubbleStatus? = null,
    ) : WaChatItem()
    data class Location(
        override val id: String,
        val side: WaSide,
        val time: String,
        val status: WaBubbleStatus? = null,
    ) : WaChatItem()
}

enum class WaSide { Me, Other }
enum class WaBubbleStatus { Sent, Delivered, Read }

@Composable
private fun WaChatItemRow(item: WaChatItem, wa: WhatsAppColors) {
    when (item) {
        is WaChatItem.Date -> WaDateSeparator(item.label, wa)
        is WaChatItem.TextMsg -> Column {
            WaTextBubble(item, wa)
            if (item.reactions.isNotEmpty()) WaReactions(item.reactions, item.side, wa)
        }
        is WaChatItem.Reply -> WaReplyBubble(item, wa)
        is WaChatItem.Voice -> WaVoiceBubble(item, wa)
        is WaChatItem.Location -> WaLocationBubble(item, wa)
    }
}

// MARK: - Header

@Composable
private fun WaMessagesHeader(
    name: String,
    subtitle: String,
    unreadCount: Int?,
    onBack: () -> Unit,
    onVideo: () -> Unit,
    onPhone: () -> Unit,
    wa: WhatsAppColors,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(wa.surfacePanel),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = wa.surfaceProduct)
            }
            if (unreadCount != null && unreadCount > 0) {
                Text("$unreadCount", color = wa.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(4.dp))
            }
            Box(
                Modifier
                    .size(36.dp)
                    .background(wa.textSecondary.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(name.take(1).uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = wa.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(subtitle, color = wa.textSecondaryAlpha, fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onVideo) {
                Icon(Icons.Default.Videocam, contentDescription = "Video", tint = wa.surfaceProduct, modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = onPhone) {
                Icon(Icons.Default.Phone, contentDescription = "Phone", tint = wa.surfaceProduct, modifier = Modifier.size(20.dp))
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.33.dp)
                .background(wa.borderPanel),
        )
    }
}

// MARK: - Date Separator

@Composable
private fun WaDateSeparator(label: String, wa: WhatsAppColors) {
    Row(
        Modifier.fillMaxWidth().padding(top = 23.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            color = wa.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(wa.surfaceDate, RoundedCornerShape(8.dp))
                .border(BorderStroke(0.66.dp, wa.surfaceShadowBaloon), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 3.dp),
        )
    }
}

// MARK: - Text Bubble

@Composable
private fun WaTextBubble(item: WaChatItem.TextMsg, wa: WhatsAppColors) {
    val isMe = item.side == WaSide.Me
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Box {
            Column(
                Modifier
                    .widthIn(min = 88.dp, max = 287.dp)
                    .background(if (isMe) wa.surfaceBaloonMe else wa.surfaceBaloonOther, RoundedCornerShape(12.dp))
                    .border(BorderStroke(0.66.dp, wa.surfaceShadowBaloon), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp)
                    .padding(top = 5.5.dp, bottom = 6.5.dp),
            ) {
                if (!isMe && item.senderName != null) {
                    Text(item.senderName, color = wa.textProduct, fontSize = 12.8.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    item.text + "     ${item.time}",
                    color = wa.textPrimary,
                    fontSize = 15.8.sp,
                    lineHeight = 21.sp,
                )
            }
            Row(
                Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.time, color = wa.textSecondaryAlpha, fontSize = 11.sp)
                if (isMe) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = if (item.status == WaBubbleStatus.Read) Color(0xFF53BDEB) else wa.textSecondaryAlpha,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WaReplyBubble(item: WaChatItem.Reply, wa: WhatsAppColors) {
    val isMe = item.side == WaSide.Me
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            Modifier
                .widthIn(max = 287.dp)
                .background(if (isMe) wa.surfaceBaloonMe else wa.surfaceBaloonOther, RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp)
                .padding(top = 5.dp, bottom = 6.5.dp),
        ) {
            Row(
                Modifier
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(7.dp))
                    .padding(vertical = 4.dp),
            ) {
                Box(Modifier.width(4.dp).height(32.dp).background(Color(0xFFDA4F7A)))
                Column(Modifier.padding(start = 6.dp, end = 6.dp)) {
                    Text(item.replySenderName, color = Color(0xFFDA4F7A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(item.replyText, color = wa.textSecondary, fontSize = 13.2.sp, maxLines = 1)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(item.text + "     ${item.time}", color = wa.textPrimary, fontSize = 15.8.sp)
        }
    }
}

@Composable
private fun WaVoiceBubble(item: WaChatItem.Voice, wa: WhatsAppColors) {
    val isMe = item.side == WaSide.Me
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Row(
            Modifier
                .width(287.dp)
                .background(if (isMe) wa.surfaceBaloonMe else wa.surfaceBaloonOther, RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).background(wa.textSecondary.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) {
                Text(item.avatarFallback, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = wa.textPrimary, modifier = Modifier.size(18.dp))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(5, 9, 14, 20, 16, 10, 7, 12, 18, 22, 16, 9, 6, 12, 18, 20, 14, 8, 5, 10, 16, 22, 18, 12, 7, 5).forEach { h ->
                    Box(Modifier.width(2.dp).height(h.dp).background(wa.textSecondaryAlpha, RoundedCornerShape(1.dp)))
                }
            }
            Text(item.durationLabel, color = wa.textSecondaryAlpha, fontSize = 11.sp)
        }
    }
}

@Composable
private fun WaLocationBubble(item: WaChatItem.Location, wa: WhatsAppColors) {
    val isMe = item.side == WaSide.Me
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .width(240.dp)
                .height(130.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Red, modifier = Modifier.size(28.dp))
            Row(
                Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(item.time, color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun WaReactions(emojis: List<String>, side: WaSide, wa: WhatsAppColors) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 6.dp),
        horizontalArrangement = if (side == WaSide.Me) Arrangement.End else Arrangement.Start,
    ) {
        Row(
            Modifier
                .background(wa.surfaceBaloonOther, CircleShape)
                .border(BorderStroke(0.66.dp, wa.surfaceShadowBaloon), CircleShape)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            emojis.forEach { Text(it, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun WaMessagesInputBar(
    value: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    wa: WhatsAppColors,
    replyDraft: WaReplyDraft? = null,
) {
    val hasText = value.isNotBlank()
    Column(Modifier.fillMaxWidth().background(wa.surfacePanel)) {
        if (replyDraft != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(Modifier.width(4.dp).height(48.dp).background(wa.borderQuote))
                Row(
                    Modifier.weight(1f).padding(start = 8.dp, end = 7.5.dp, top = 8.dp, bottom = 7.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(replyDraft.senderName, color = wa.textQuoteTitle, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(replyDraft.text, color = wa.textPrimary, fontSize = 12.sp, maxLines = 1)
                    }
                    IconButton(onClick = replyDraft.onClose) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel reply", tint = wa.textSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 7.dp, end = 9.dp, top = 5.5.dp, bottom = 5.5.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Attach", tint = wa.surfaceProduct, modifier = Modifier.size(24.dp))
            }
            Row(
                Modifier
                    .weight(1f)
                    .background(wa.surfaceInputChat, RoundedCornerShape(15.dp))
                    .border(BorderStroke(0.33.dp, wa.borderInputChat), RoundedCornerShape(15.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(stringResource(R.string.messaging_compose_hint), color = wa.textSecondaryAlpha, fontSize = 16.sp)
                    }
                    BasicTextField(value, onChange)
                }
                Icon(Icons.Default.EmojiEmotions, contentDescription = "Stickers", tint = wa.textSecondary, modifier = Modifier.size(18.dp))
            }
            if (hasText) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.size(32.dp).background(wa.surfaceProduct, CircleShape),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = wa.textInvert, modifier = Modifier.size(16.dp))
                }
            } else {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = wa.surfaceProduct, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice", tint = wa.surfaceProduct, modifier = Modifier.size(20.dp))
                }
            }
        }
        // Home indicator
        Row(Modifier.fillMaxWidth().padding(top = 21.dp, bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
            Box(Modifier.width(140.dp).height(5.dp).background(wa.surfaceInvert, RoundedCornerShape(100.dp)))
        }
    }
}

@Preview(name = "EN Light", showBackground = true)
@Composable
private fun WaMessagesPreview() {
    WhatsAppMessagesScreen(
        contactName = "Emmett \"Doc\" Br",
        unreadCount = 1,
        items = listOf(
            WaChatItem.Date("d1", "Yesterday"),
            WaChatItem.Voice("v1", WaSide.Me, "M", "0:25", "22:30", WaBubbleStatus.Read),
            WaChatItem.Date("d2", "Today"),
            WaChatItem.TextMsg("m1", WaSide.Other, "Marty?", "08:21"),
            WaChatItem.TextMsg("m2", WaSide.Me, "Hey, hey, Doc, where are you?", "08:21", WaBubbleStatus.Read),
            WaChatItem.Reply("r1", WaSide.Other, "Yes. In the morning. 😃", "08:21", null, "You", "Wait a minute, wait a minute. 1:15 in the morning?"),
            WaChatItem.TextMsg("m13", WaSide.Me, "Yeah, I'll keep that in mind…", "08:23", WaBubbleStatus.Read, reactions = listOf("❤️", "❤️", "❤️")),
            WaChatItem.Location("l1", WaSide.Me, "08:24", WaBubbleStatus.Read),
        ),
    )
}
