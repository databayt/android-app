package org.hogwarts.android.feature.notifications.domain.usecase

import org.hogwarts.android.feature.notifications.data.repository.NotificationsRepository
import org.hogwarts.android.feature.notifications.domain.model.NotificationPreference
import javax.inject.Inject

class GetNotificationPreferencesUseCase @Inject constructor(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(): Result<List<NotificationPreference>> =
        repository.getPreferences()
}

class UpdateNotificationPreferencesUseCase @Inject constructor(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(preferences: List<NotificationPreference>): Result<Unit> =
        repository.updatePreferences(preferences)
}

class RegisterDeviceTokenUseCase @Inject constructor(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> =
        repository.registerDeviceToken(token)
}
