package org.hogwarts.android.feature.notifications.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.notifications.data.remote.NotificationsApi
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationDto
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationListResponse
import org.hogwarts.android.feature.notifications.data.remote.dto.NotificationPreferenceUpdate
import org.hogwarts.android.feature.notifications.data.remote.dto.UpdatePreferencesRequest
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.domain.model.NotificationPage
import org.hogwarts.android.feature.notifications.domain.model.NotificationPriority
import org.hogwarts.android.feature.notifications.domain.model.PreferenceMatrix
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed interface PageResult {
    data class Fresh(val page: NotificationPage) : PageResult
    /** The last good page, because the network failed. */
    data class Cached(val page: NotificationPage) : PageResult
    data class Failed(val message: String?) : PageResult
}

interface NotificationsRepository {
    /** The user's unread total from the last answer, for the Unread tab's badge. */
    val unreadCount: StateFlow<Int?>
    suspend fun page(unreadOnly: Boolean, page: Int): PageResult
    suspend fun markRead(id: String): Result<Unit>
    suspend fun markAllRead(): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    suspend fun preferences(): Result<PreferenceMatrix>
    suspend fun savePreferences(matrix: PreferenceMatrix): Result<Unit>
}

/**
 * Network first; the last good first page of each tab is kept per user as the
 * offline fallback, the dashboard's "saved page" pattern.
 */
@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val api: NotificationsApi,
    private val json: Json,
    private val tenantContext: TenantContext,
    @ApplicationContext private val context: Context,
) : NotificationsRepository {

    private val _unreadCount = MutableStateFlow<Int?>(null)
    override val unreadCount: StateFlow<Int?> = _unreadCount.asStateFlow()

    private fun cacheFile(unreadOnly: Boolean): File? = tenantContext.userId?.let {
        File(context.filesDir, "notifications-$it-${if (unreadOnly) "unread" else "all"}.json")
    }

    override suspend fun page(unreadOnly: Boolean, page: Int): PageResult {
        return try {
            val response = api.getNotifications(unreadOnly = if (unreadOnly) true else null, page = page)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                _unreadCount.value = body.unreadCount
                if (page == 1) {
                    withContext(Dispatchers.IO) {
                        runCatching { cacheFile(unreadOnly)?.writeText(json.encodeToString(NotificationListResponse.serializer(), body)) }
                    }
                }
                PageResult.Fresh(body.toPage())
            } else {
                cached(unreadOnly, page) ?: PageResult.Failed("HTTP ${response.code()}")
            }
        } catch (e: IOException) {
            cached(unreadOnly, page) ?: PageResult.Failed(e.message)
        }
    }

    private suspend fun cached(unreadOnly: Boolean, page: Int): PageResult.Cached? {
        if (page != 1) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                cacheFile(unreadOnly)?.takeIf { it.exists() }?.readText()
                    ?.let { json.decodeFromString(NotificationListResponse.serializer(), it) }
            }.onFailure { Timber.w(it, "Notifications cache unreadable") }.getOrNull()
        }?.let { PageResult.Cached(it.toPage()) }
    }

    override suspend fun markRead(id: String): Result<Unit> = call { api.markRead(id) }
        .onSuccess { _unreadCount.value = _unreadCount.value?.minus(1)?.coerceAtLeast(0) }

    override suspend fun markAllRead(): Result<Unit> = call { api.markAllRead() }
        .onSuccess { _unreadCount.value = 0 }

    override suspend fun delete(id: String): Result<Unit> = call { api.delete(id) }

    override suspend fun preferences(): Result<PreferenceMatrix> = runCatching {
        val response = api.getPreferences()
        val body = response.body()
        if (!response.isSuccessful || body == null) error("HTTP ${response.code()}")
        PreferenceMatrix.from(body.data.map { Triple(it.type, it.channel, it.enabled) })
    }

    override suspend fun savePreferences(matrix: PreferenceMatrix): Result<Unit> = call {
        // Every switch, as the web form submits them.
        api.updatePreferences(
            UpdatePreferencesRequest(
                NotificationKind.entries.flatMap { kind ->
                    NotificationChannel.entries.map { ch -> NotificationPreferenceUpdate(kind.wire, ch.wire, matrix.isOn(kind, ch)) }
                },
            ),
        )
    }

    private suspend fun call(block: suspend () -> retrofit2.Response<Unit>): Result<Unit> = runCatching {
        val response = block()
        if (!response.isSuccessful) error("HTTP ${response.code()}")
    }
}

internal fun NotificationListResponse.toPage() = NotificationPage(
    items = data.map { it.toDomain() },
    total = total,
    unreadCount = unreadCount,
    page = page,
    perPage = perPage,
)

internal fun NotificationDto.toDomain() = AppNotification(
    id = id,
    type = type,
    priority = NotificationPriority.fromWire(priority),
    title = title,
    body = body,
    isRead = isRead,
    url = ((metadata as? JsonObject)?.get("url") as? JsonPrimitive)?.takeIf { it.isString }?.content,
    createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.EPOCH),
    actorName = actorName?.takeIf { it.isNotBlank() },
)
