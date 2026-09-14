package org.hogwarts.android.feature.messaging.ui.thread

import androidx.compose.runtime.Immutable
import org.hogwarts.android.feature.messaging.domain.model.ThreadMessage
import org.hogwarts.android.feature.messaging.ui.format.MessagingFormat

/** The tick a bubble shows — `BubbleStatus` in `bubble-timestamp.tsx`. */
enum class BubbleStatus { Sending, Sent, Delivered, Read, Failed }

enum class Side { Me, Other }

/** `ChatItem` in `messages-view.tsx`: the flat list the thread renders. */
@Immutable
sealed interface ChatItem {
    val id: String

    data class Date(override val id: String, val label: String) : ChatItem

    data class Text(
        override val id: String,
        val side: Side,
        val text: String,
        val time: String,
        val status: BubbleStatus?,
        val senderName: String?,
        val tail: Boolean,
    ) : ChatItem

    data class Reply(
        override val id: String,
        val side: Side,
        val text: String,
        val time: String,
        val status: BubbleStatus?,
        val replySenderName: String,
        val replyText: String,
    ) : ChatItem

    data class Voice(
        override val id: String,
        val side: Side,
        val avatarUrl: String?,
        val avatarFallback: String,
        val durationLabel: String,
        val time: String,
        val status: BubbleStatus?,
    ) : ChatItem

    data class Location(
        override val id: String,
        val side: Side,
        val time: String,
        val status: BubbleStatus?,
    ) : ChatItem
}

/** `AdaptLabels` in `chat/adapt.ts`. */
data class AdaptLabels(
    val today: String,
    val yesterday: String,
    val deleted: String,
    val photo: String,
    val video: String,
    val voice: String,
    val document: String,
    val attachment: String,
    val userFallback: String,
)

internal fun bubbleStatus(m: ThreadMessage): BubbleStatus = when (m.status.lowercase()) {
    "sending" -> BubbleStatus.Sending
    "failed" -> BubbleStatus.Failed
    "read" -> BubbleStatus.Read
    "delivered" -> BubbleStatus.Delivered
    else -> BubbleStatus.Sent
}

/**
 * `toChatItems` from `chat/adapt.ts`: a date pill at each day boundary, a tail
 * on the last bubble of each same-sender run, the sender named on the first
 * incoming bubble of a run in a group.
 *
 * Differences forced by the mobile API: its messages carry no reply excerpt
 * (the quoted message is looked up among the loaded ones, and a reply whose
 * source is not loaded draws as a plain bubble), no reactions, no voice
 * duration, and deleted messages are filtered out server-side. Attachments are
 * not cached, so a caption-less attachment is named from its content type.
 */
fun toChatItems(
    messages: List<ThreadMessage>,
    currentUserId: String,
    isGroup: Boolean,
    format: MessagingFormat,
    labels: AdaptLabels,
): List<ChatItem> {
    val items = ArrayList<ChatItem>(messages.size + 4)
    val byId = messages.associateBy { it.id }
    messages.forEachIndexed { i, m ->
        val prev = messages.getOrNull(i - 1)
        val next = messages.getOrNull(i + 1)
        if (prev == null || !format.isSameDay(prev.sentAt, m.sentAt)) {
            items += ChatItem.Date("date-${m.id}", format.daySeparator(m.sentAt, labels.today, labels.yesterday))
        }
        val isMe = m.senderId == currentUserId
        val side = if (isMe) Side.Me else Side.Other
        val time = format.clock(m.sentAt)
        val status = if (isMe) bubbleStatus(m) else null
        val tail = next == null || next.senderId != m.senderId || !format.isSameDay(next.sentAt, m.sentAt)
        val startsRun = prev == null || prev.senderId != m.senderId || !format.isSameDay(prev.sentAt, m.sentAt)
        val senderName = if (isGroup && !isMe && startsRun) m.senderName.ifEmpty { labels.userFallback } else null

        when (m.contentType) {
            "voice", "audio" -> {
                items += ChatItem.Voice(
                    id = m.id,
                    side = side,
                    avatarUrl = m.senderAvatarUrl,
                    avatarFallback = m.senderName.ifEmpty { labels.userFallback }.take(1).uppercase(),
                    durationLabel = "0:00",
                    time = time,
                    status = status,
                )
                return@forEachIndexed
            }
            "location" -> {
                items += ChatItem.Location(m.id, side, time, status)
                return@forEachIndexed
            }
        }

        val source = m.replyToId?.let(byId::get)
        if (source != null) {
            items += ChatItem.Reply(
                id = m.id,
                side = side,
                text = m.content,
                time = time,
                status = status,
                replySenderName = source.senderName.ifEmpty { labels.userFallback },
                replyText = source.content,
            )
            return@forEachIndexed
        }

        val text = m.content.takeIf { it.isNotBlank() } ?: when (m.contentType) {
            "image" -> labels.photo
            "video" -> labels.video
            "file", "document" -> labels.document
            else -> labels.attachment
        }
        items += ChatItem.Text(m.id, side, text, time, status, senderName, tail)
    }
    return items
}
