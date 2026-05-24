package org.hogwarts.android.feature.notifications.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.notifications.data.repository.NotificationsRepository
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationsRepository
) {
    operator fun invoke(type: String? = null): Flow<Resource<List<AppNotification>>> =
        repository.getNotifications(type)
}

class GetUnreadCountUseCase @Inject constructor(
    private val repository: NotificationsRepository
) {
    operator fun invoke(): Flow<Int> = repository.getUnreadCount()
}

class MarkNotificationReadUseCase @Inject constructor(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(notificationId: String) = repository.markRead(notificationId)
}

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke() = repository.markAllRead()
}
