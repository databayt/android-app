package org.hogwarts.android.feature.messaging.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.res.stringResource
import org.hogwarts.android.feature.messaging.R
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.feature.messaging.domain.model.Message
import org.hogwarts.android.feature.messaging.domain.model.MessageStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SENDER_COLORS = listOf(
    Color(0xFF10B981), // emerald
    Color(0xFF0EA5E9), // sky
    Color(0xFF8B5CF6), // violet
    Color(0xFFF43F5E), // rose
    Color(0xFFF59E0B), // amber
    Color(0xFF14B8A6), // teal
    Color(0xFF6366F1), // indigo
    Color(0xFFEC4899), // pink
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    showSenderName: Boolean,
    isGroupChat: Boolean,
    currentUserId: String = "",
    onLongPress: (Message) -> Unit = {},
    onReactionToggle: (Message, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val waColors = LocalWhatsAppColors.current
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault())
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
    ) {
        // Sender name for group chats
        if (showSenderName && isGroupChat && !isOwnMessage) {
            val senderColor = remember(message.senderId) {
                SENDER_COLORS[message.senderId.hashCode().and(0x7FFFFFFF) % SENDER_COLORS.size]
            }
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = senderColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp),
            )
        }

        // Bubble
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isOwnMessage) 12.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 12.dp,
                    )
                )
                .background(if (isOwnMessage) waColors.surfaceProduct else waColors.surfaceSearchChat)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { if (!message.isDeleted) onLongPress(message) },
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            // Reply context
            message.replyTo?.let { reply ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.06f))
                        .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                ) {
                    Text(
                        text = reply.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = waColors.textProduct,
                    )
                    Text(
                        text = reply.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOwnMessage) waColors.textInvert.copy(alpha = 0.7f)
                        else waColors.textPrimary.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // Content based on type
            if (message.isDeleted) {
                Text(
                    text = stringResource(R.string.messaging_ui_this_message_deleted),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = if (isOwnMessage) waColors.textInvert.copy(alpha = 0.6f)
                    else waColors.textPrimary.copy(alpha = 0.6f),
                )
            } else {
                when (message.contentType) {
                    "image" -> {
                        val attachment = message.attachments.firstOrNull()
                        if (attachment != null) {
                            AsyncImage(
                                model = attachment.fileUrl,
                                contentDescription = attachment.fileName,
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.FillWidth,
                            )
                            if (message.content.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = message.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isOwnMessage) waColors.textInvert
                                    else waColors.textPrimary,
                                )
                            }
                        }
                    }
                    "audio" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = stringResource(R.string.messaging_ui_voice_message),
                                modifier = Modifier.size(20.dp),
                                tint = waColors.textProduct,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.messaging_ui_voice_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isOwnMessage) waColors.textInvert
                                else waColors.textPrimary,
                            )
                        }
                    }
                    "video" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Videocam,
                                contentDescription = stringResource(R.string.messaging_ui_video),
                                modifier = Modifier.size(20.dp),
                                tint = if (isOwnMessage) waColors.textInvert
                                else waColors.textPrimary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = message.attachments.firstOrNull()?.fileName
                                    ?: stringResource(R.string.messaging_ui_video),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isOwnMessage) waColors.textInvert
                                else waColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    "file" -> {
                        val attachment = message.attachments.firstOrNull()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Description,
                                contentDescription = stringResource(R.string.messaging_ui_document),
                                modifier = Modifier.size(20.dp),
                                tint = if (isOwnMessage) waColors.textInvert
                                else waColors.textPrimary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = attachment?.fileName
                                        ?: stringResource(R.string.messaging_ui_document),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isOwnMessage) waColors.textInvert
                                    else waColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                attachment?.formattedSize?.let { size ->
                                    Text(
                                        text = size,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = waColors.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOwnMessage) waColors.textInvert
                            else waColors.textPrimary,
                        )
                        val url = remember(message.content) { extractFirstUrl(message.content) }
                        if (url != null) {
                            Spacer(Modifier.height(6.dp))
                            LinkPreviewCard(url = url)
                        }
                    }
                }
            }

            // Time + status row
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (message.isEdited) {
                    Text(
                        text = stringResource(R.string.messaging_ui_edited),
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = waColors.textSecondary,
                    )
                }
                Text(
                    text = timeFormatter.format(message.sentAt),
                    fontSize = 11.sp,
                    color = waColors.textSecondary,
                )
                if (isOwnMessage) {
                    MessageStatusIcon(status = message.status)
                }
            }
        }

        // Reactions row below the bubble
        if (message.reactions.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            MessageReactions(
                reactions = message.reactions,
                currentUserId = currentUserId,
                onToggle = { emoji -> onReactionToggle(message, emoji) },
                modifier = Modifier.padding(
                    start = if (isOwnMessage) 0.dp else 8.dp,
                    end = if (isOwnMessage) 8.dp else 0.dp,
                ),
            )
        }
    }
}
