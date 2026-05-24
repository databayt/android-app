package org.hogwarts.android.feature.students.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.core.data.util.networkBoundResource
import org.hogwarts.android.core.database.dao.StudentDao
import org.hogwarts.android.core.database.entity.StudentEntity
import org.hogwarts.android.feature.students.data.remote.StudentsApi
import org.hogwarts.android.feature.students.data.remote.dto.CreateStudentDto
import org.hogwarts.android.feature.students.data.remote.dto.StudentDto
import org.hogwarts.android.feature.students.data.remote.dto.UpdateStudentDto
import org.hogwarts.android.feature.students.domain.model.Student
import org.hogwarts.android.feature.students.domain.model.StudentStatus
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first implementation of [StudentsRepository].
 *
 * Uses networkBoundResource for read operations.
 * Write operations go to API first, then cache locally.
 */
@Singleton
class StudentsRepositoryImpl @Inject constructor(
    private val api: StudentsApi,
    private val dao: StudentDao,
    private val tenantContext: TenantContext
) : StudentsRepository {

    override fun getStudents(
        classId: String?,
        status: String?,
        search: String?
    ): Flow<Resource<List<Student>>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                when {
                    search != null -> dao.search(schoolId, search)
                    classId != null -> dao.observeByClass(schoolId, classId)
                    status != null -> dao.observeByStatus(schoolId, status)
                    else -> dao.observeAll(schoolId)
                }.map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                val response = api.getStudents(
                    classId = classId,
                    status = status,
                    search = search
                )
                response.body()?.data ?: emptyList()
            },
            saveFetchResult = { dtos ->
                val entities = dtos.map { it.toEntity(schoolId) }
                dao.insertAll(entities)
            }
        )
    }

    override fun getStudent(studentId: String): Flow<Resource<Student?>> {
        val schoolId = tenantContext.requireSchoolId()
        return networkBoundResource(
            query = {
                dao.observeById(schoolId, studentId)
                    .map { entity -> entity?.toDomain() }
            },
            fetch = {
                val response = api.getStudent(studentId)
                response.body()
            },
            saveFetchResult = { dto ->
                if (dto != null) {
                    dao.insert(dto.toEntity(schoolId))
                }
            }
        )
    }

    override suspend fun createStudent(student: Student): Student {
        val schoolId = tenantContext.requireSchoolId()
        val dto = CreateStudentDto(
            firstName = student.firstName,
            lastName = student.lastName,
            email = student.email,
            phone = student.phone,
            dateOfBirth = student.dateOfBirth?.toString(),
            gender = student.gender,
            classId = student.classId,
            section = student.section,
            guardianName = student.guardianName,
            guardianPhone = student.guardianPhone
        )
        val response = api.createStudent(dto)
        val created = response.body() ?: throw Exception("Failed to create student")
        val entity = created.toEntity(schoolId)
        dao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun updateStudent(student: Student): Student {
        val schoolId = tenantContext.requireSchoolId()
        val dto = UpdateStudentDto(
            firstName = student.firstName,
            lastName = student.lastName,
            email = student.email,
            phone = student.phone,
            status = student.status.name,
            classId = student.classId,
            section = student.section,
            guardianName = student.guardianName,
            guardianPhone = student.guardianPhone
        )
        val response = api.updateStudent(student.id, dto)
        val updated = response.body() ?: throw Exception("Failed to update student")
        val entity = updated.toEntity(schoolId)
        dao.insert(entity)
        return entity.toDomain()
    }
}

// --- Mappers ---

private fun StudentDto.toEntity(schoolId: String) = StudentEntity(
    id = id,
    schoolId = schoolId,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    dateOfBirth = dateOfBirth?.let { LocalDate.parse(it) },
    gender = gender,
    enrollmentNumber = enrollmentNumber,
    classId = classId,
    className = className,
    section = section,
    guardianName = guardianName,
    guardianPhone = guardianPhone,
    status = status,
    avatarUrl = avatarUrl,
    lastSyncAt = Instant.now()
)

private fun StudentEntity.toDomain() = Student(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    dateOfBirth = dateOfBirth,
    gender = gender,
    enrollmentNumber = enrollmentNumber,
    classId = classId,
    className = className,
    section = section,
    guardianName = guardianName,
    guardianPhone = guardianPhone,
    status = StudentStatus.fromString(status),
    avatarUrl = avatarUrl
)
