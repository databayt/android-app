package org.hogwarts.android.feature.messaging.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.domain.model.Conversation
import org.hogwarts.android.feature.messaging.domain.model.Message
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val repository: MessagingRepository,
) {
    operator fun invoke(type: String? = null): Flow<Resource<List<Conversation>>> =
        repository.getConversations(type)
}

class GetMessagesUseCase @Inject constructor(
    private val repository: MessagingRepository,
) {
    operator fun invoke(conversationId: String): Flow<Resource<List<Message>>> =
        repository.getMessages(conversationId)
}

class SendMessageUseCase @Inject constructor(
    private val repository: MessagingRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        content: String,
        replyToId: String? = null,
    ): Message = repository.sendMessage(conversationId, content, replyToId)
}

class MarkAsReadUseCase @Inject constructor(
    private val repository: MessagingRepository,
) {
    suspend operator fun invoke(conversationId: String) =
        repository.markAsRead(conversationId)
}
