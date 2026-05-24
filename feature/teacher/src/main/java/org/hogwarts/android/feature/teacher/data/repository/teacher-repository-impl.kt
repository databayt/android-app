package org.hogwarts.android.feature.teacher.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.teacher.data.remote.TeacherApi
import org.hogwarts.android.feature.teacher.data.remote.dto.AttendanceRecordDto
import org.hogwarts.android.feature.teacher.data.remote.dto.BatchAttendanceRequestDto
import org.hogwarts.android.feature.teacher.data.remote.dto.BatchGradeRequestDto
import org.hogwarts.android.feature.teacher.data.remote.dto.GradeRecordDto
import org.hogwarts.android.feature.teacher.domain.model.Assessment
import org.hogwarts.android.feature.teacher.domain.model.AttendanceMark
import org.hogwarts.android.feature.teacher.domain.model.ClassStudent
import org.hogwarts.android.feature.teacher.domain.model.GradeEntry
import org.hogwarts.android.feature.teacher.domain.model.ScheduleSlot
import org.hogwarts.android.feature.teacher.domain.model.TeacherClass
import javax.inject.Inject

class TeacherRepositoryImpl @Inject constructor(
    private val api: TeacherApi,
    private val tenantContext: TenantContext
) : TeacherRepository {

    override suspend fun getClasses(): List<TeacherClass> {
        return api.getClasses().map { dto ->
            TeacherClass(
                id = dto.id,
                name = dto.name,
                grade = dto.grade,
                section = dto.section,
                subjectId = dto.subjectId,
                subjectName = dto.subjectName,
                studentCount = dto.studentCount,
                nextSessionTime = dto.nextSessionTime,
                nextSessionDay = dto.nextSessionDay
            )
        }
    }

    override suspend fun getClassStudents(classId: String): List<ClassStudent> {
        return api.getClassStudents(classId).map { dto ->
            ClassStudent(
                id = dto.id,
                givenName = dto.givenName,
                familyName = dto.familyName,
                studentNumber = dto.studentNumber,
                avatarUrl = dto.avatarUrl,
                attendanceRate = dto.attendanceRate,
                latestGrade = dto.latestGrade,
                status = dto.status
            )
        }
    }

    override suspend fun getSchedule(): List<ScheduleSlot> {
        return api.getSchedule().map { dto ->
            ScheduleSlot(
                id = dto.id,
                dayOfWeek = dto.dayOfWeek,
                startTime = dto.startTime,
                endTime = dto.endTime,
                className = dto.className,
                subjectName = dto.subjectName,
                roomNumber = dto.roomNumber,
                classId = dto.classId
            )
        }
    }

    override suspend fun getAssessments(classId: String): List<Assessment> {
        return api.getAssessments(classId).map { dto ->
            Assessment(
                id = dto.id,
                name = dto.name,
                maxMarks = dto.maxMarks,
                date = dto.date
            )
        }
    }

    override suspend fun submitBatchAttendance(
        classId: String,
        date: String,
        period: String?,
        marks: List<AttendanceMark>
    ) {
        api.submitBatchAttendance(
            classId,
            BatchAttendanceRequestDto(
                date = date,
                period = period,
                records = marks.map {
                    AttendanceRecordDto(
                        studentId = it.studentId,
                        status = it.status.name,
                        note = it.note
                    )
                }
            )
        )
    }

    override suspend fun submitBatchGrades(
        classId: String,
        assessmentId: String,
        grades: List<GradeEntry>
    ) {
        api.submitBatchGrades(
            classId,
            BatchGradeRequestDto(
                assessmentId = assessmentId,
                grades = grades.filter { it.marks != null }.map {
                    GradeRecordDto(
                        studentId = it.studentId,
                        marks = it.marks!!,
                        note = it.note
                    )
                }
            )
        )
    }
}
