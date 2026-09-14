package org.hogwarts.android.feature.messaging.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.feature.messaging.domain.model.ChatSummary
import org.hogwarts.android.feature.messaging.domain.model.MessagingViewer
import org.hogwarts.android.feature.messaging.domain.model.ThreadMessage

/**
 * Messages data: Room is the source the screens observe, the mobile API fills
 * it. Realtime is polling — prod runs no socket server, and the web polls the
 * same way while its socket is down (`messaging-client.tsx`).
 */
interface MessagingRepository {

    /** The signed-in user's id, or null when signed out. */
    val currentUserId: String?

    fun observeConversations(): Flow<List<ChatSummary>>

    fun observeConversation(conversationId: String): Flow<ChatSummary?>

    /** Refetches the list. False when the request did not come back 2xx. */
    suspend fun refreshConversations(): Boolean

    fun observeMessages(conversationId: String): Flow<List<ThreadMessage>>

    /** Refetches the newest page. Returns the older-page cursor, or a failure. */
    suspend fun refreshMessages(conversationId: String): PageResult

    /** Fetches the page before [cursor]. */
    suspend fun loadOlderMessages(conversationId: String, cursor: String): PageResult

    /**
     * Puts an optimistic row into the thread and tries the server. The row
     * only becomes a sent message on a 2xx; otherwise it turns `failed` and
     * stays queued for the background sender.
     */
    suspend fun sendMessage(conversationId: String, content: String, replyToId: String? = null)

    /** Re-sends a failed optimistic row. */
    suspend fun retryMessage(messageId: String)

    /** Clears the badge locally at once; the server call is best effort. */
    suspend fun markAsRead(conversationId: String)

    /** `GET /api/mobile/profile`, cached for the session; null when it fails. */
    suspend fun viewer(): MessagingViewer?
}

sealed interface PageResult {
    data class Loaded(val olderCursor: String?) : PageResult
    data object Failed : PageResult
}
