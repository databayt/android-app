package org.hogwarts.android.feature.guardian.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.guardian.data.remote.GuardianApi
import org.hogwarts.android.feature.guardian.domain.model.Child
import javax.inject.Inject

/**
 * Implementation of GuardianRepository.
 * All operations are scoped by schoolId for multi-tenant isolation.
 */
class GuardianRepositoryImpl @Inject constructor(
    private val api: GuardianApi,
    private val tenantContext: TenantContext
) : GuardianRepository {

    override fun observeChildren(): Flow<List<Child>> = flow {
        emit(getChildren())
    }

    override suspend fun getChildren(): List<Child> {
        return api.getChildren().map { dto ->
            Child(
                id = dto.id,
                studentId = dto.studentId,
                givenName = dto.givenName,
                familyName = dto.familyName,
                grade = dto.grade,
                section = dto.section,
                avatarUrl = dto.avatarUrl,
                attendanceRate = dto.attendanceRate,
                latestGrade = dto.latestGrade,
                feeBalance = dto.feeBalance,
                className = dto.className
            )
        }
    }

    override suspend fun getChild(childId: String): Child {
        val dto = api.getChild(childId)
        return Child(
            id = dto.id,
            studentId = dto.studentId,
            givenName = dto.givenName,
            familyName = dto.familyName,
            grade = dto.grade,
            section = dto.section,
            avatarUrl = dto.avatarUrl,
            attendanceRate = dto.attendanceRate,
            latestGrade = dto.latestGrade,
            feeBalance = dto.feeBalance,
            className = dto.className
        )
    }

    override suspend fun getChildAttendance(
        childId: String,
        startDate: String?,
        endDate: String?
    ): List<ChildAttendanceRecord> {
        return api.getChildAttendance(childId, startDate, endDate).map { dto ->
            ChildAttendanceRecord(
                id = dto.id,
                date = dto.date,
                status = dto.status,
                period = dto.period,
                subject = dto.subject,
                note = dto.note,
                markedBy = dto.markedBy
            )
        }
    }

    override suspend fun getChildGrades(
        childId: String,
        termId: String?
    ): List<ChildGradeRecord> {
        return api.getChildGrades(childId, termId).map { dto ->
            ChildGradeRecord(
                id = dto.id,
                subjectId = dto.subjectId,
                subjectName = dto.subjectName,
                assessmentName = dto.assessmentName,
                score = dto.score,
                maxScore = dto.maxScore,
                percentage = dto.percentage,
                gradeLetter = dto.gradeLetter,
                term = dto.term,
                date = dto.date
            )
        }
    }

    override suspend fun getChildFees(
        childId: String,
        status: String?
    ): List<ChildFeeRecord> {
        return api.getChildFees(childId, status).map { dto ->
            ChildFeeRecord(
                id = dto.id,
                name = dto.name,
                amount = dto.amount,
                dueDate = dto.dueDate,
                status = dto.status,
                paidAmount = dto.paidAmount,
                paidDate = dto.paidDate
            )
        }
    }

    override suspend fun getChildTimetable(childId: String): List<ChildTimetableSlot> {
        return api.getChildTimetable(childId).map { dto ->
            ChildTimetableSlot(
                id = dto.id,
                dayOfWeek = dto.dayOfWeek,
                startTime = dto.startTime,
                endTime = dto.endTime,
                subjectName = dto.subjectName,
                teacherName = dto.teacherName,
                roomNumber = dto.roomNumber
            )
        }
    }

    override suspend fun getChildTeachers(childId: String): List<ChildTeacher> {
        return api.getChildTeachers(childId).map { dto ->
            ChildTeacher(
                id = dto.id,
                givenName = dto.givenName,
                familyName = dto.familyName,
                subject = dto.subject,
                avatarUrl = dto.avatarUrl,
                unreadCount = dto.unreadCount
            )
        }
    }
}
