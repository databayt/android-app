package org.hogwarts.android.feature.grades.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.GradeDao
import org.hogwarts.android.core.database.entity.GradeEntity
import org.hogwarts.android.feature.grades.data.remote.GradesApi
import org.hogwarts.android.feature.grades.data.remote.dto.GradeRecordDto
import org.hogwarts.android.feature.grades.domain.model.AssessmentType
import org.hogwarts.android.feature.grades.domain.model.GpaSummary
import org.hogwarts.android.feature.grades.domain.model.GradeRecord
import org.hogwarts.android.feature.grades.domain.model.SubjectGradeSummary
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first implementation of [GradesRepository].
 */
@Singleton
class GradesRepositoryImpl @Inject constructor(
    private val api: GradesApi,
    private val dao: GradeDao,
    private val tenantContext: TenantContext
) : GradesRepository {

    override fun getStudentGrades(
        studentId: String,
        term: String?,
        subjectId: String?
    ): Flow<Resource<List<GradeRecord>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                when {
                    subjectId != null -> dao.observeByStudentAndSubject(schoolId, studentId, subjectId)
                    term != null -> dao.observeByStudentAndTerm(schoolId, studentId, term)
                    else -> dao.observeByStudent(schoolId, studentId)
                }.map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                // Web API doesn't support term/subject_id query filters;
                // fetch all grades and filter locally via Room queries above
                val response = api.getStudentGrades(
                    studentId = studentId
                )
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                val entities = dtos.map { it.toEntity(schoolId) }
                dao.insertAll(entities)
            }
        )
    }

    override suspend fun getGpaSummary(studentId: String, term: String?): GpaSummary {
        // Web API doesn't support term filter; term param kept in repository interface for local use
        val response = api.getGpaSummary(studentId)
        val dto = response.body() ?: throw Exception("No GPA data")
        return GpaSummary(
            gpa = dto.gpa,
            totalCredits = dto.totalCredits,
            subjects = dto.subjects.map { subject ->
                SubjectGradeSummary(
                    subjectId = subject.subjectId,
                    subjectName = subject.subjectName,
                    averagePercentage = subject.averagePercentage,
                    letterGrade = subject.letterGrade,
                    totalAssessments = subject.totalAssessments
                )
            },
            term = dto.term
        )
    }
}

// --- Mappers ---

private fun GradeRecordDto.toEntity(schoolId: String) = GradeEntity(
    id = id,
    schoolId = schoolId,
    studentId = studentId,
    subjectId = subjectId,
    subjectName = subjectName,
    assessmentType = assessmentType,
    assessmentName = assessmentName,
    score = score,
    maxScore = maxScore,
    grade = grade,
    date = LocalDate.parse(date),
    term = term,
    remarks = remarks,
    lastSyncAt = Instant.now()
)

private fun GradeEntity.toDomain() = GradeRecord(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    subjectName = subjectName,
    assessmentType = AssessmentType.fromString(assessmentType),
    assessmentName = assessmentName,
    score = score,
    maxScore = maxScore,
    grade = grade,
    date = date,
    term = term,
    remarks = remarks
)
