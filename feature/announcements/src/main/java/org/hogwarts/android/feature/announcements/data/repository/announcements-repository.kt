package org.hogwarts.android.feature.announcements.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.announcements.data.remote.AnnouncementsApi
import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementDto
import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementListResponse
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementsPage
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ListResult {
    data class Fresh(val page: AnnouncementsPage) : ListResult
    /** The last good first page, served because the network failed. */
    data class Cached(val page: AnnouncementsPage) : ListResult
    data class Failed(val message: String?) : ListResult
}

sealed interface DetailResult {
    data class Fresh(val announcement: Announcement) : DetailResult
    data class Cached(val announcement: Announcement) : DetailResult
    /** The server says no: gone, or the caller is not its audience. */
    data object NotFound : DetailResult
    data class Failed(val message: String?) : DetailResult
}

interface AnnouncementsRepository {
    /** The last first page this user saw, for an instant paint. */
    suspend fun cachedFirstPage(): AnnouncementsPage?

    /** A page of the list, optionally narrowed by a title search. */
    suspend fun page(page: Int, title: String? = null): ListResult

    /** The reading page. Opening it marks the notice read on the server. */
    suspend fun detail(id: String): DetailResult
}

/**
 * Network first; the last good first page and every opened notice stay on disk
 * per user, so an announcement already read can be read again offline — the web
 * service worker's saved pages. Searches are never cached.
 */
@Singleton
class AnnouncementsRepositoryImpl @Inject constructor(
    private val api: AnnouncementsApi,
    private val json: Json,
    private val tenantContext: TenantContext,
    @ApplicationContext private val context: Context,
) : AnnouncementsRepository {

    private fun lang(): String = if (Locale.getDefault().language == "ar") "ar" else "en"

    private fun dir(): File? = tenantContext.userId?.let { File(context.filesDir, "announcements/$it").apply { mkdirs() } }

    private fun listFile(): File? = dir()?.let { File(it, "list-${lang()}.json") }

    private fun detailFile(id: String): File? = dir()?.let { File(it, "item-${lang()}-${id.filter { c -> c.isLetterOrDigit() || c == '-' || c == '_' }}.json") }

    private suspend fun <T> read(file: File?, serializer: KSerializer<T>): T? = withContext(Dispatchers.IO) {
        runCatching { file?.takeIf { it.exists() }?.readText()?.let { json.decodeFromString(serializer, it) } }
            .onFailure { Timber.w(it, "Announcements cache unreadable") }
            .getOrNull()
    }

    private suspend fun <T> write(file: File?, serializer: KSerializer<T>, value: T) = withContext(Dispatchers.IO) {
        runCatching { file?.writeText(json.encodeToString(serializer, value)) }
    }

    override suspend fun cachedFirstPage(): AnnouncementsPage? =
        read(listFile(), AnnouncementListResponse.serializer())?.toDomain()

    override suspend fun page(page: Int, title: String?): ListResult {
        val search = title?.trim()?.ifEmpty { null }
        val cacheable = page == 1 && search == null
        return try {
            val response = api.getAnnouncements(page = page, perPage = PER_PAGE, title = search, lang = lang())
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (cacheable) write(listFile(), AnnouncementListResponse.serializer(), body)
                ListResult.Fresh(body.toDomain())
            } else {
                fallback(cacheable, "HTTP ${response.code()}")
            }
        } catch (e: IOException) {
            fallback(cacheable, e.message)
        }
    }

    private suspend fun fallback(cacheable: Boolean, message: String?): ListResult =
        (if (cacheable) cachedFirstPage() else null)?.let { ListResult.Cached(it) } ?: ListResult.Failed(message)

    override suspend fun detail(id: String): DetailResult {
        return try {
            val response = api.getAnnouncement(id, lang = lang())
            val body = response.body()
            when {
                response.isSuccessful && body != null -> {
                    write(detailFile(id), AnnouncementDto.serializer(), body)
                    DetailResult.Fresh(body.toDomain())
                }
                response.code() == 404 || response.code() == 403 -> {
                    withContext(Dispatchers.IO) { runCatching { detailFile(id)?.delete() } }
                    DetailResult.NotFound
                }
                else -> cachedDetail(id)?.let { DetailResult.Cached(it) } ?: DetailResult.Failed("HTTP ${response.code()}")
            }
        } catch (e: IOException) {
            cachedDetail(id)?.let { DetailResult.Cached(it) } ?: DetailResult.Failed(e.message)
        }
    }

    private suspend fun cachedDetail(id: String): Announcement? =
        read(detailFile(id), AnnouncementDto.serializer())?.toDomain()
            ?: cachedFirstPage()?.items?.firstOrNull { it.id == id }

    companion object {
        /** The web listing's page size (`announcementsSearchParams.perPage`). */
        const val PER_PAGE = 20
    }
}

internal fun AnnouncementListResponse.toDomain() = AnnouncementsPage(
    items = data.map { it.toDomain() },
    total = total,
    page = page,
    perPage = perPage,
)

internal fun AnnouncementDto.toDomain() = Announcement(
    id = id,
    title = title.orEmpty(),
    body = content.orEmpty(),
    scope = scope,
    priority = priority,
    targetRole = targetRole,
    isPublished = isPublished,
    isPinned = isPinned,
    createdAt = (createdAt ?: publishedAt)?.let { runCatching { Instant.parse(it) }.getOrNull() },
    updatedAt = updatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    isRead = isRead,
)
