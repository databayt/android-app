package org.hogwarts.android.feature.messaging.domain.model

import java.time.Instant

/** One row of the chat list. */
data class ChatSummary(
    val id: String,
    /** Wire value: `direct`, `group`, `class`, `department`, `announcement`. */
    val type: String,
    val title: String?,
    val avatarUrl: String?,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val lastMessage: LastMessage?,
    val lastMessageAt: Instant,
) {
    /** Anything that is not a 1:1 reads as a group, as on the web. */
    val isGroup: Boolean get() = type != "direct"
}

data class LastMessage(
    val content: String,
    val senderName: String,
    val status: String,
    val sentAt: Instant,
)

/** A message as the thread shows it; `status` is the wire value or `sending`/`failed`. */
data class ThreadMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String?,
    val content: String,
    val contentType: String,
    val status: String,
    val sentAt: Instant,
    val replyToId: String?,
    val nonce: String?,
) {
    val isOptimistic: Boolean get() = id.startsWith(OPTIMISTIC_PREFIX)

    companion object {
        const val OPTIMISTIC_PREFIX = "pending_"
        fun optimisticId(nonce: String) = OPTIMISTIC_PREFIX + nonce
    }
}

/** The signed-in person as `GET /api/mobile/profile` describes them. */
data class MessagingViewer(
    val userId: String,
    val username: String?,
    val avatarUrl: String?,
    val bio: String?,
    val schoolName: String?,
    val schoolNameEn: String?,
)
