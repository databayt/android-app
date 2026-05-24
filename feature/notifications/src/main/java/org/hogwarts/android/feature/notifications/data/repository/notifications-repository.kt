package org.hogwarts.android.feature.notifications.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationPreference

interface NotificationsRepository {

    fun getNotifications(type: String? = null): Flow<Resource<List<AppNotification>>>

    fun getUnreadCount(): Flow<Int>

    suspend fun markRead(notificationId: String)

    suspend fun markAllRead()

    suspend fun getPreferences(): Result<List<NotificationPreference>>

    suspend fun updatePreferences(preferences: List<NotificationPreference>): Result<Unit>

    suspend fun registerDeviceToken(token: String): Result<Unit>
}
