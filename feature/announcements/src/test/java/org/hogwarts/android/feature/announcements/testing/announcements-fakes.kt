package org.hogwarts.android.feature.announcements.testing

import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.announcements.data.repository.AnnouncementsRepository
import org.hogwarts.android.feature.announcements.data.repository.DetailResult
import org.hogwarts.android.feature.announcements.data.repository.ListResult
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementsPage
import java.time.Duration
import java.time.Instant

/** Invented fixtures only — android-app is a public repo. */
fun announcement(
    id: String,
    title: String = "Notice $id",
    scope: String = "school",
    priority: String = "normal",
    published: Boolean = true,
    targetRole: String? = null,
    body: String = "Fictional body for $id.",
    day: Int = 10,
) = Announcement(
    id = id,
    title = title,
    body = body,
    scope = scope,
    priority = priority,
    targetRole = targetRole,
    isPublished = published,
    isPinned = false,
    createdAt = Instant.parse("2026-09-01T09:00:00Z").plus(Duration.ofDays(day - 1L)),
    updatedAt = Instant.parse("2026-09-01T10:00:00Z").plus(Duration.ofDays(day - 1L)),
    isRead = false,
)

class FakeAnnouncementsRepository : AnnouncementsRepository {
    var cached: AnnouncementsPage? = null
    /** Pages by number; searches filter page 1 by title. */
    var pages: MutableMap<Int, AnnouncementsPage> = mutableMapOf()
    var failList = false
    var details: MutableMap<String, DetailResult> = mutableMapOf()
    val requests = mutableListOf<Pair<Int, String?>>()

    override suspend fun cachedFirstPage(): AnnouncementsPage? = cached

    override suspend fun page(page: Int, title: String?): ListResult {
        requests += page to title
        if (failList) return cached?.takeIf { page == 1 && title.isNullOrBlank() }?.let { ListResult.Cached(it) } ?: ListResult.Failed("offline")
        val found = pages[page] ?: return ListResult.Failed("HTTP 500")
        return ListResult.Fresh(
            if (title.isNullOrBlank()) found
            else found.copy(items = found.items.filter { it.title.contains(title, ignoreCase = true) }).let { it.copy(total = it.items.size) },
        )
    }

    override suspend fun detail(id: String): DetailResult = details[id] ?: DetailResult.NotFound
}

class FakeSessionManager(role: UserRole) : SessionManager {
    override var currentUser: CurrentUser? = CurrentUser("u1", "u1@school.test", "school-1", role, "Test", "User")
    override val isAuthenticated: Boolean get() = currentUser != null
    override suspend fun setUser(user: CurrentUser) { currentUser = user }
    override suspend fun clearSession() { currentUser = null }
}

fun tenant(role: UserRole) = TenantContext(FakeSessionManager(role))
