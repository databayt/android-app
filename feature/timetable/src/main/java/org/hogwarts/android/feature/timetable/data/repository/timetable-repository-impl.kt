package org.hogwarts.android.feature.timetable.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.TimetableDao
import org.hogwarts.android.core.database.entity.TimetableEntity
import org.hogwarts.android.feature.timetable.data.remote.TimetableApi
import org.hogwarts.android.feature.timetable.data.remote.dto.TimetableEntryDto
import org.hogwarts.android.feature.timetable.domain.model.TimetableEntry
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepositoryImpl @Inject constructor(
    private val api: TimetableApi,
    private val dao: TimetableDao,
    private val tenantContext: TenantContext
) : TimetableRepository {

    override fun getTimetable(userId: String): Flow<Resource<List<TimetableEntry>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                dao.observeByUser(schoolId, userId)
                    .map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getTimetable(userId)
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                dao.deleteByUser(schoolId, userId)
                dao.insertAll(dtos.map { it.toEntity(schoolId, userId) })
            }
        )
    }
}

private fun TimetableEntryDto.toEntity(schoolId: String, userId: String) = TimetableEntity(
    id = id,
    schoolId = schoolId,
    userId = userId,
    subjectName = subjectName,
    teacherName = teacherName,
    roomNumber = roomNumber,
    dayOfWeek = dayOfWeek,
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime),
    section = section,
    lastSyncAt = Instant.now()
)

private fun TimetableEntity.toDomain() = TimetableEntry(
    id = id,
    subjectName = subjectName,
    teacherName = teacherName,
    roomNumber = roomNumber,
    dayOfWeek = DayOfWeek.of(dayOfWeek),
    startTime = startTime,
    endTime = endTime,
    section = section
)
