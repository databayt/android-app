package org.hogwarts.android.feature.messaging.domain.model

import java.time.Instant

data class ReadReceipt(
    val messageId: String,
    val userId: String,
    val userName: String,
    val readAt: Instant
)

data class TypingIndicator(
    val conversationId: String,
    val userId: String,
    val userName: String,
    val isTyping: Boolean
)
