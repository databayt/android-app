package org.hogwarts.android.feature.announcements.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.AnnouncementDao
import org.hogwarts.android.core.database.entity.AnnouncementEntity
import org.hogwarts.android.feature.announcements.data.remote.AnnouncementsApi
import org.hogwarts.android.feature.announcements.data.remote.dto.AnnouncementDto
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementStatus
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementsRepositoryImpl @Inject constructor(
    private val api: AnnouncementsApi,
    private val dao: AnnouncementDao,
    private val tenantContext: TenantContext
) : AnnouncementsRepository {

    override fun getAnnouncements(type: String?): Flow<Resource<List<Announcement>>> {
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
                val response = api.getAnnouncements(type)
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                dao.insertAll(dtos.map { it.toEntity(schoolId) })
            }
        )
    }

    override fun getAnnouncement(announcementId: String): Flow<Resource<Announcement?>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                dao.observeById(schoolId, announcementId)
                    .map { it?.toDomain() }
            },
            fetch = {
                val response = api.getAnnouncement(announcementId)
                response.body()
            },
            saveFetchResult = { dto ->
                if (dto != null) {
                    dao.insert(dto.toEntity(schoolId))
                }
            }
        )
    }

    override fun getUpcomingEvents(): Flow<Resource<List<Announcement>>> {
        val schoolId = tenantContext.requireSchoolId()
        val today = LocalDate.now().toString()
        return networkBoundResource(
            query = {
                dao.observeUpcomingEvents(schoolId, today)
                    .map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getEvents(fromDate = today)
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                dao.insertAll(dtos.map { it.toEntity(schoolId) })
            }
        )
    }
}

// --- Mappers ---

private fun AnnouncementDto.toEntity(schoolId: String) = AnnouncementEntity(
    id = id,
    schoolId = schoolId,
    title = title,
    content = content,
    type = type,
    category = category,
    authorId = authorId,
    authorName = authorName,
    targetAudience = targetAudience,
    date = LocalDate.parse(date),
    startTime = startTime?.let { LocalTime.parse(it) },
    endTime = endTime?.let { LocalTime.parse(it) },
    venue = venue,
    isImportant = isImportant,
    attachmentUrl = attachmentUrl,
    status = status,
    lastSyncAt = Instant.now()
)

private fun AnnouncementEntity.toDomain() = Announcement(
    id = id,
    title = title,
    content = content,
    type = AnnouncementType.fromString(type),
    category = category,
    authorId = authorId,
    authorName = authorName,
    targetAudience = targetAudience,
    date = date,
    startTime = startTime,
    endTime = endTime,
    venue = venue,
    isImportant = isImportant,
    attachmentUrl = attachmentUrl,
    status = AnnouncementStatus.fromString(status)
)
