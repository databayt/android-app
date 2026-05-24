package org.hogwarts.android.feature.guardian.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.guardian.data.repository.ChildAttendanceRecord
import org.hogwarts.android.feature.guardian.data.repository.ChildFeeRecord
import org.hogwarts.android.feature.guardian.data.repository.ChildGradeRecord
import org.hogwarts.android.feature.guardian.data.repository.ChildTeacher
import org.hogwarts.android.feature.guardian.data.repository.ChildTimetableSlot
import org.hogwarts.android.feature.guardian.data.repository.GuardianRepository
import org.hogwarts.android.feature.guardian.domain.model.Child
import javax.inject.Inject

class GetChildrenUseCase @Inject constructor(
    private val repository: GuardianRepository
) {
    suspend operator fun invoke(): Result<List<Child>> {
        return try {
            Result.Success(repository.getChildren())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChildUseCase @Inject constructor(
    private val repository: GuardianRepository
) {
    suspend operator fun invoke(childId: String): Result<Child> {
        return try {
            Result.Success(repository.getChild(childId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChildAttendanceUseCase @Inject constructor(
    private val repository: GuardianRepository
) {
    suspend operator fun invoke(
        childId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<ChildAttendanceRecord>> {
        return try {
            Result.Success(repository.getChildAttendance(childId, startDate, endDate))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChildGradesUseCase @Inject constructor(
    private val repository: GuardianRepository
) {
    suspend operator fun invoke(
        childId: String,
        termId: String? = null
    ): Result<List<ChildGradeRecord>> {
        return try {
            Result.Success(repository.getChildGrades(childId, termId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChildFeesUseCase @Inject constructor(
    private val repository: GuardianRepository
) {
    suspend operator fun invoke(
        childId: String,
        status: String? = null
    ): Result<List<ChildFeeRecord>> {
        return try {
            Result.Success(repository.getChildFees(childId, status))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChildTimetableUseCase @Inject constructor(
    private val repository: GuardianRepository
) {
    suspend operator fun invoke(childId: String): Result<List<ChildTimetableSlot>> {
        return try {
            Result.Success(repository.getChildTimetable(childId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetChildTeachersUseCase @Inject constructor(
    private val repository: GuardianRepository
) {
    suspend operator fun invoke(childId: String): Result<List<ChildTeacher>> {
        return try {
            Result.Success(repository.getChildTeachers(childId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
