package org.hogwarts.android.feature.admin.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.admin.data.repository.AdminRepository
import org.hogwarts.android.feature.admin.domain.model.ClassRoster
import org.hogwarts.android.feature.admin.domain.model.SchoolInfo
import org.hogwarts.android.feature.admin.domain.model.SchoolStats
import org.hogwarts.android.feature.admin.domain.model.StaffMember
import javax.inject.Inject

class GetSchoolInfoUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): Result<SchoolInfo> {
        return try {
            Result.Success(repository.getSchoolInfo())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class UpdateSchoolInfoUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(info: SchoolInfo): Result<SchoolInfo> {
        return try {
            Result.Success(repository.updateSchoolInfo(info))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetStaffUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(role: String? = null, search: String? = null): Result<List<StaffMember>> {
        return try {
            Result.Success(repository.getStaff(role, search))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetClassRostersUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): Result<List<ClassRoster>> {
        return try {
            Result.Success(repository.getClassRosters())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetClassRosterUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(classId: String): Result<ClassRoster> {
        return try {
            Result.Success(repository.getClassRoster(classId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class AddStudentToClassUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(classId: String, studentId: String): Result<Unit> {
        return try {
            repository.addStudentToClass(classId, studentId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class RemoveStudentFromClassUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(classId: String, studentId: String): Result<Unit> {
        return try {
            repository.removeStudentFromClass(classId, studentId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

class GetSchoolStatsUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): Result<SchoolStats> {
        return try {
            Result.Success(repository.getSchoolStats())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
