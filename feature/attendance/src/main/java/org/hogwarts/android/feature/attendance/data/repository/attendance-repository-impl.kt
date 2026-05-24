package org.hogwarts.android.feature.attendance.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.AttendanceDao
import org.hogwarts.android.core.database.entity.AttendanceEntity
import org.hogwarts.android.feature.attendance.data.remote.AttendanceApi
import org.hogwarts.android.feature.attendance.data.remote.dto.AttendanceRecordDto
import org.hogwarts.android.feature.attendance.data.remote.dto.BulkAttendanceDto
import org.hogwarts.android.feature.attendance.data.remote.dto.BulkAttendanceRecordDto
import org.hogwarts.android.feature.attendance.data.remote.dto.MarkAttendanceDto
import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStatus
import org.hogwarts.android.feature.attendance.domain.model.AttendanceSummary
import org.hogwarts.android.feature.attendance.domain.model.BulkAttendanceRequest
import org.hogwarts.android.feature.attendance.domain.model.ExcuseRequest
import org.hogwarts.android.feature.attendance.domain.model.MarkAttendanceRequest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first implementation of [AttendanceRepository].
 *
 * Uses networkBoundResource pattern:
 * 1. Show cached data from Room
 * 2. Fetch from API in background
 * 3. Save to Room
 * 4. Emit fresh data
 */
@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val api: AttendanceApi,
    private val dao: AttendanceDao,
    private val tenantContext: TenantContext
) : AttendanceRepository {

    override fun getStudentAttendance(
        studentId: String,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<Resource<List<AttendanceRecord>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                if (startDate != null && endDate != null) {
                    dao.observeByStudentDateRange(schoolId, studentId, startDate, endDate)
                } else {
                    dao.observeByStudent(schoolId, studentId)
                }.map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getStudentAttendance(
                    studentId = studentId,
                    startDate = startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate = endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                val entities = dtos.map { it.toEntity(schoolId) }
                dao.insertAll(entities)
            }
        )
    }

    override fun getClassAttendance(
        classId: String,
        date: LocalDate
    ): Flow<Resource<List<AttendanceRecord>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                dao.observeByClassAndDate(schoolId, classId, date)
                    .map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getClassAttendance(
                    classId = classId,
                    date = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                val entities = dtos.map { it.toEntity(schoolId) }
                dao.insertAll(entities)
            }
        )
    }

    override suspend fun getAttendanceSummary(studentId: String): AttendanceSummary {
        val schoolId = tenantContext.requireSchoolId()
        return try {
            val response = api.getAttendanceSummary(studentId)
            val dto = response.body() ?: throw Exception("No summary data")
            AttendanceSummary(
                totalDays = dto.totalDays,
                presentDays = dto.presentDays,
                absentDays = dto.absentDays,
                lateDays = dto.lateDays,
                excusedDays = dto.excusedDays
            )
        } catch (e: Exception) {
            // Fallback to local data
            val total = dao.countTotal(schoolId, studentId)
            val present = dao.countByStatus(schoolId, studentId, "PRESENT")
            val absent = dao.countByStatus(schoolId, studentId, "ABSENT")
            val late = dao.countByStatus(schoolId, studentId, "LATE")
            val excused = dao.countByStatus(schoolId, studentId, "EXCUSED")
            AttendanceSummary(total, present, absent, late, excused)
        }
    }

    override suspend fun markAttendance(request: MarkAttendanceRequest): AttendanceRecord {
        val response = api.markAttendance(
            MarkAttendanceDto(
                studentId = request.studentId,
                classId = request.classId,
                date = request.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = request.status.name,
                note = request.note
            )
        )
        val dto = response.body() ?: throw Exception("Failed to mark attendance")
        val schoolId = tenantContext.requireSchoolId()
        val entity = dto.toEntity(schoolId)
        dao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun markBulkAttendance(request: BulkAttendanceRequest): List<AttendanceRecord> {
        val response = api.markBulkAttendance(
            BulkAttendanceDto(
                classId = request.classId,
                date = request.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                records = request.records.map { mark ->
                    BulkAttendanceRecordDto(
                        studentId = mark.studentId,
                        status = mark.status.name,
                        note = mark.note
                    )
                }
            )
        )
        val dtos = response.body()?.data ?: throw Exception("Failed to mark bulk attendance")
        val schoolId = tenantContext.requireSchoolId()
        val entities = dtos.map { it.toEntity(schoolId) }
        dao.insertAll(entities)
        return entities.map { it.toDomain() }
    }

    override suspend fun submitExcuse(request: ExcuseRequest): ExcuseRequest {
        // TODO: Wire to real API endpoint when available
        return request.copy(id = "excuse-${System.currentTimeMillis()}")
    }
}

// --- Mappers ---

private fun AttendanceRecordDto.toEntity(schoolId: String) = AttendanceEntity(
    id = id,
    schoolId = schoolId,
    studentId = studentId,
    studentName = studentName,
    classId = classId,
    className = className,
    date = LocalDate.parse(date),
    status = status,
    checkInTime = checkInTime?.let { LocalTime.parse(it) },
    note = note,
    markedBy = markedBy,
    lastSyncAt = Instant.now()
)

private fun AttendanceEntity.toDomain() = AttendanceRecord(
    id = id,
    studentId = studentId,
    studentName = studentName,
    classId = classId,
    className = className,
    date = date,
    status = AttendanceStatus.fromString(status),
    checkInTime = checkInTime,
    note = note,
    markedBy = markedBy
)
