package org.hogwarts.android.feature.teacher.data.repository

import org.hogwarts.android.feature.teacher.domain.model.Assessment
import org.hogwarts.android.feature.teacher.domain.model.AttendanceMark
import org.hogwarts.android.feature.teacher.domain.model.ClassStudent
import org.hogwarts.android.feature.teacher.domain.model.GradeEntry
import org.hogwarts.android.feature.teacher.domain.model.ScheduleSlot
import org.hogwarts.android.feature.teacher.domain.model.TeacherClass

/**
 * Repository interface for teacher data.
 */
interface TeacherRepository {
    suspend fun getClasses(): List<TeacherClass>
    suspend fun getClassStudents(classId: String): List<ClassStudent>
    suspend fun getSchedule(): List<ScheduleSlot>
    suspend fun getAssessments(classId: String): List<Assessment>
    suspend fun submitBatchAttendance(classId: String, date: String, period: String?, marks: List<AttendanceMark>)
    suspend fun submitBatchGrades(classId: String, assessmentId: String, grades: List<GradeEntry>)
}
