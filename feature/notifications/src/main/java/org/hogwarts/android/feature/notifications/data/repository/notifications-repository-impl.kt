package org.hogwarts.android.feature.notifications.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.NotificationDao
import org.hogwarts.android.core.database.entity.NotificationEntity
import org.hogwarts.android.feature.notifications.data.remote.NotificationsApi
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationDto
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationPreferenceDto
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationPreferenceUpdate
import org.hogwarts.android.feature.notifications.data.remote.dto.RegisterDeviceTokenRequest
import org.hogwarts.android.feature.notifications.data.remote.dto.UpdatePreferencesRequest
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationPreference
import org.hogwarts.android.feature.notifications.domain.model.NotificationPriority
import org.hogwarts.android.feature.notifications.domain.model.NotificationType
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val api: NotificationsApi,
    private val dao: NotificationDao,
    private val tenantContext: TenantContext
) : NotificationsRepository {

    override fun getNotifications(type: String?): Flow<Resource<List<AppNotification>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                if (type != null) {
                    dao.observeByType(schoolId, type)
                } else {
                    dao.observeAll(schoolId)
                }.map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getNotifications()
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                dao.insertAll(dtos.map { it.toEntity(schoolId) })
            }
        )
    }

    override fun getUnreadCount(): Flow<Int> {
        val schoolId = tenantContext.requireSchoolId()
        return dao.observeUnreadCount(schoolId)
    }

    override suspend fun markRead(notificationId: String) {
        val schoolId = tenantContext.requireSchoolId()
        try {
            api.markRead(notificationId)
        } catch (_: Exception) { /* best effort -- offline mark still applies locally */ }
        dao.markRead(schoolId, notificationId)
    }

    override suspend fun markAllRead() {
        val schoolId = tenantContext.requireSchoolId()
        try {
            api.markAllRead()
        } catch (_: Exception) { /* best effort -- offline mark still applies locally */ }
        dao.markAllRead(schoolId)
    }

    override suspend fun getPreferences(): Result<List<NotificationPreference>> = runCatching {
        val response = api.getPreferences()
        if (!response.isSuccessful) error("HTTP ${response.code()}")
        response.body()?.data?.mapNotNull { it.toDomainOrNull() } ?: emptyList()
    }

    override suspend fun updatePreferences(
        preferences: List<NotificationPreference>
    ): Result<Unit> = runCatching {
        val updates = preferences.map { pref ->
            NotificationPreferenceUpdate(
                type = pref.type.name.lowercase(),
                channel = pref.channel.wireName,
                enabled = pref.enabled
            )
        }
        val response = api.updatePreferences(UpdatePreferencesRequest(updates))
        if (!response.isSuccessful) error("HTTP ${response.code()}")
    }

    override suspend fun registerDeviceToken(token: String): Result<Unit> = runCatching {
        val response = api.registerDeviceToken(
            RegisterDeviceTokenRequest(deviceToken = token, platform = "android")
        )
        if (!response.isSuccessful) error("HTTP ${response.code()}")
    }
}

private fun NotificationDto.toEntity(schoolId: String) = NotificationEntity(
    id = id,
    schoolId = schoolId,
    title = title,
    body = body,
    type = type,
    priority = priority ?: "normal",
    isRead = isRead,
    deepLink = deepLink,
    createdAt = Instant.parse(createdAt),
    lastSyncAt = Instant.now()
)

private fun NotificationEntity.toDomain() = AppNotification(
    id = id,
    title = title,
    body = body,
    type = NotificationType.fromString(type),
    priority = NotificationPriority.fromString(priority),
    isRead = isRead,
    deepLink = deepLink,
    createdAt = createdAt
)

private fun NotificationPreferenceDto.toDomainOrNull(): NotificationPreference? = runCatching {
    NotificationPreference(
        type = NotificationType.fromString(type),
        channel = NotificationChannel.fromString(channel),
        enabled = enabled,
        quietHoursStart = quietHoursStart,
        quietHoursEnd = quietHoursEnd,
        digestEnabled = digestEnabled,
        digestFrequency = digestFrequency
    )
}.getOrNull()
