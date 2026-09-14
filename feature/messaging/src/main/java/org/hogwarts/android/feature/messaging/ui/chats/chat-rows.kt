package org.hogwarts.android.feature.messaging.ui.chats

import androidx.compose.runtime.Immutable
import org.hogwarts.android.feature.messaging.domain.model.ChatSummary
import org.hogwarts.android.feature.messaging.ui.format.MessagingFormat

/** `FilterId` in `ios-filter-chips.tsx`. */
enum class ChatFilter { All, Unread, Favourites, Groups }

/** The glyph ahead of a preview — `LeadingKind` in `ios-message-preview.tsx`. */
enum class PreviewLeading { CheckRead, CheckSent }

/** `IosChatRowData`. */
@Immutable
data class ChatRowData(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val avatarKey: String,
    val isGroup: Boolean,
    val preview: String,
    val previewLeading: PreviewLeading?,
    val timestamp: String,
    val unreadCount: Int,
    val pinned: Boolean,
)

data class RowLabels(
    val groupFallback: String,
    val directFallback: String,
    val yesterday: String,
)

/** `getConversationName`: the list route already names a 1:1 after the other person. */
fun ChatSummary.displayName(labels: RowLabels): String =
    title ?: if (isGroup) labels.groupFallback else labels.directFallback

/**
 * `filtered` in `ios-chat-list.tsx`: unread, pinned-as-favourite and group
 * filters, a name search, newest first.
 */
fun filterChats(
    chats: List<ChatSummary>,
    filter: ChatFilter,
    query: String,
    labels: RowLabels,
): List<ChatSummary> {
    val q = query.trim().lowercase()
    return chats.filter { c ->
        when (filter) {
            ChatFilter.Unread -> if (c.unreadCount == 0) return@filter false
            ChatFilter.Groups -> if (!c.isGroup) return@filter false
            ChatFilter.Favourites -> if (!c.isPinned) return@filter false
            ChatFilter.All -> Unit
        }
        q.isEmpty() || c.displayName(labels).lowercase().contains(q)
    }.sortedByDescending { it.lastMessageAt }
}

/**
 * `toRowData`. The list route's last message has no sender id, so "mine" is
 * decided by the sender's username matching the viewer's (both are the
 * server's `User.username`); with no viewer it is never mine and draws no tick.
 */
fun ChatSummary.toRowData(
    viewerUsername: String?,
    format: MessagingFormat,
    labels: RowLabels,
): ChatRowData {
    val last = lastMessage
    var preview = ""
    var leading: PreviewLeading? = null
    if (last != null) {
        preview = last.content
        val isMine = viewerUsername != null && last.senderName == viewerUsername
        if (isMine) {
            leading = if (last.status == "read") PreviewLeading.CheckRead else PreviewLeading.CheckSent
        } else if (isGroup) {
            // In a group the reference names who spoke: "Name: message".
            preview = "${last.senderName.ifEmpty { labels.directFallback }}: $preview"
        }
    }
    return ChatRowData(
        id = id,
        name = displayName(labels),
        avatarUrl = avatarUrl,
        avatarKey = id,
        isGroup = isGroup,
        preview = preview,
        previewLeading = leading,
        timestamp = format.listStamp(last?.sentAt ?: lastMessageAt, labels.yesterday),
        unreadCount = unreadCount,
        pinned = isPinned,
    )
}
