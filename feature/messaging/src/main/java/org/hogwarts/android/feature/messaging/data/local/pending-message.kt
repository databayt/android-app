package org.hogwarts.android.feature.messaging.data.local

import java.time.Instant

data class PendingMessage(
    val id: String,
    val conversationId: String,
    val content: String,
    val replyToId: String? = null,
    val attachmentUri: String? = null,
    val createdAt: Instant = Instant.now(),
    val retryCount: Int = 0,
    val status: PendingMessageStatus = PendingMessageStatus.QUEUED
)

enum class PendingMessageStatus {
    QUEUED, SENDING, SENT, FAILED
}
