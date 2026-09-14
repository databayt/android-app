package org.hogwarts.android.feature.notifications.testing

import kotlinx.coroutines.flow.MutableStateFlow
import org.hogwarts.android.feature.notifications.data.repository.NotificationsRepository
import org.hogwarts.android.feature.notifications.data.repository.PageResult
import org.hogwarts.android.feature.notifications.domain.model.AppNotification
import org.hogwarts.android.feature.notifications.domain.model.NotificationPage
import org.hogwarts.android.feature.notifications.domain.model.NotificationPriority
import org.hogwarts.android.feature.notifications.domain.model.PreferenceMatrix
import java.time.Instant

fun notification(
    id: String,
    type: String = "announcement",
    read: Boolean = false,
    priority: NotificationPriority = NotificationPriority.Normal,
    title: String = "Library opens late on Thursday",
    body: String = "Doors open at ten while shelves are restocked.",
    url: String? = null,
    createdAt: Instant = Instant.parse("2026-09-14T08:00:00Z"),
) = AppNotification(id, type, priority, title, body, read, url, createdAt)

class FakeNotificationsRepository : NotificationsRepository {
    override val unreadCount = MutableStateFlow<Int?>(null)
    var rows: List<AppNotification> = emptyList()
    var result: ((Boolean, Int) -> PageResult)? = null
    val calls = mutableListOf<String>()
    var matrix: PreferenceMatrix = PreferenceMatrix.from(emptyList())
    var saved: PreferenceMatrix? = null
    var failSave = false

    override suspend fun page(unreadOnly: Boolean, page: Int): PageResult {
        calls += "page:$unreadOnly:$page"
        result?.let { return it(unreadOnly, page) }
        val items = if (unreadOnly) rows.filterNot { it.isRead } else rows
        unreadCount.value = rows.count { !it.isRead }
        return PageResult.Fresh(NotificationPage(items, items.size, rows.count { !it.isRead }, page, 20))
    }

    override suspend fun markRead(id: String): Result<Unit> {
        calls += "read:$id"
        rows = rows.map { if (it.id == id) it.copy(isRead = true) else it }
        return Result.success(Unit)
    }

    override suspend fun markAllRead(): Result<Unit> {
        calls += "readAll"
        rows = rows.map { it.copy(isRead = true) }
        unreadCount.value = 0
        return Result.success(Unit)
    }

    override suspend fun delete(id: String): Result<Unit> {
        calls += "delete:$id"
        rows = rows.filterNot { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun preferences(): Result<PreferenceMatrix> = Result.success(matrix)

    override suspend fun savePreferences(matrix: PreferenceMatrix): Result<Unit> {
        if (failSave) return Result.failure(IllegalStateException("HTTP 500"))
        saved = matrix
        return Result.success(Unit)
    }
}
