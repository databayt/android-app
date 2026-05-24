package org.hogwarts.android.feature.exams.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.ExamDao
import org.hogwarts.android.core.database.entity.ExamEntity
import org.hogwarts.android.feature.exams.data.remote.ExamsApi
import org.hogwarts.android.feature.exams.data.remote.dto.ExamDto
import org.hogwarts.android.feature.exams.domain.model.Exam
import org.hogwarts.android.feature.exams.domain.model.ExamResult
import org.hogwarts.android.feature.exams.domain.model.ExamStatus
import org.hogwarts.android.feature.exams.domain.model.ExamType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamsRepositoryImpl @Inject constructor(
    private val api: ExamsApi,
    private val dao: ExamDao,
    private val tenantContext: TenantContext
) : ExamsRepository {

    override fun getExams(status: String?): Flow<Resource<List<Exam>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                if (status != null) {
                    dao.observeByStatus(schoolId, status)
                } else {
                    dao.observeAll(schoolId)
                }.map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getExams(status)
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                dao.insertAll(dtos.map { it.toEntity(schoolId) })
            }
        )
    }

    override fun getExam(examId: String): Flow<Resource<Exam?>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                dao.observeById(schoolId, examId)
                    .map { it?.toDomain() }
            },
            fetch = {
                val response = api.getExam(examId)
                response.body()
            },
            saveFetchResult = { dto ->
                if (dto != null) {
                    dao.insert(dto.toEntity(schoolId))
                }
            }
        )
    }
}

// --- Mappers ---

private fun ExamDto.toEntity(schoolId: String) = ExamEntity(
    id = id,
    schoolId = schoolId,
    title = title,
    subjectId = subjectId,
    subjectName = subjectName,
    date = LocalDate.parse(date),
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime),
    venue = venue,
    instructions = instructions,
    type = type,
    status = status,
    maxMarks = maxMarks,
    passingMarks = passingMarks,
    marksObtained = marksObtained,
    grade = grade,
    remarks = remarks,
    isPassed = isPassed,
    lastSyncAt = Instant.now()
)

private fun ExamEntity.toDomain() = Exam(
    id = id,
    title = title,
    subjectId = subjectId,
    subjectName = subjectName,
    date = date,
    startTime = startTime,
    endTime = endTime,
    venue = venue,
    instructions = instructions,
    type = try { ExamType.valueOf(type) } catch (_: Exception) { ExamType.WRITTEN },
    status = try { ExamStatus.valueOf(status) } catch (_: Exception) { ExamStatus.UPCOMING },
    maxMarks = maxMarks,
    passingMarks = passingMarks,
    result = marksObtained?.let { marks ->
        ExamResult(
            marksObtained = marks,
            grade = grade,
            remarks = remarks,
            isPassed = isPassed ?: false
        )
    }
)
