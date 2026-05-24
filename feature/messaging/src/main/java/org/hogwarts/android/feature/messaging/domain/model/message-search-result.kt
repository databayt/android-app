package org.hogwarts.android.feature.messaging.domain.model

import java.time.Instant

/** Unified result shape for global / per-conversation / starred message queries.
 *  Matches web MessageSearchResultDto + StarredMessageInnerDto. */
data class MessageSearchResult(
    val id: String,
    val conversationId: String,
    val conversationTitle: String = "",
    val conversationType: String = "direct",
    val senderId: String = "",
    val senderName: String = "",
    val content: String,
    val contentType: String = "text",
    val sentAt: Instant,
    val starredAt: Instant? = null,
)
