package org.hogwarts.android.feature.teacher.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.teacher.data.repository.TeacherRepository
import org.hogwarts.android.feature.teacher.domain.model.Assessment
import org.hogwarts.android.feature.teacher.domain.model.AttendanceMark
import org.hogwarts.android.feature.teacher.domain.model.ClassStudent
import org.hogwarts.android.feature.teacher.domain.model.GradeEntry
import org.hogwarts.android.feature.teacher.domain.model.ScheduleSlot
import org.hogwarts.android.feature.teacher.domain.model.TeacherClass
import javax.inject.Inject

class GetMyClassesUseCase @Inject constructor(
    private val repository: TeacherRepository
) {
    suspend operator fun invoke(): Result<List<TeacherClass>> {
        return try {
            Result.Success(repository.getClasses())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetClassStudentsUseCase @Inject constructor(
    private val repository: TeacherRepository
) {
    suspend operator fun invoke(classId: String): Result<List<ClassStudent>> {
        return try {
            Result.Success(repository.getClassStudents(classId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetTeacherScheduleUseCase @Inject constructor(
    private val repository: TeacherRepository
) {
    suspend operator fun invoke(): Result<List<ScheduleSlot>> {
        return try {
            Result.Success(repository.getSchedule())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetAssessmentsUseCase @Inject constructor(
    private val repository: TeacherRepository
) {
    suspend operator fun invoke(classId: String): Result<List<Assessment>> {
        return try {
            Result.Success(repository.getAssessments(classId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class SubmitBatchAttendanceUseCase @Inject constructor(
    private val repository: TeacherRepository
) {
    suspend operator fun invoke(
        classId: String,
        date: String,
        period: String?,
        marks: List<AttendanceMark>
    ): Result<Unit> {
        return try {
            repository.submitBatchAttendance(classId, date, period, marks)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class SubmitBatchGradesUseCase @Inject constructor(
    private val repository: TeacherRepository
) {
    suspend operator fun invoke(
        classId: String,
        assessmentId: String,
        grades: List<GradeEntry>
    ): Result<Unit> {
        return try {
            repository.submitBatchGrades(classId, assessmentId, grades)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
