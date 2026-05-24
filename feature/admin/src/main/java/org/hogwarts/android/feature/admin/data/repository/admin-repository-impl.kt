package org.hogwarts.android.feature.admin.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.admin.data.remote.AdminApi
import org.hogwarts.android.feature.admin.data.remote.dto.SchoolInfoDto
import org.hogwarts.android.feature.admin.domain.model.ClassRoster
import org.hogwarts.android.feature.admin.domain.model.SchoolInfo
import org.hogwarts.android.feature.admin.domain.model.SchoolStats
import org.hogwarts.android.feature.admin.domain.model.StaffMember
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val api: AdminApi,
    private val tenantContext: TenantContext
) : AdminRepository {

    override suspend fun getSchoolInfo(): SchoolInfo {
        return api.getSchoolInfo().toDomain()
    }

    override suspend fun updateSchoolInfo(info: SchoolInfo): SchoolInfo {
        val dto = SchoolInfoDto(
            id = info.id,
            name = info.name,
            domain = info.domain,
            logo = info.logo,
            contactEmail = info.contactEmail,
            contactPhone = info.contactPhone,
            address = info.address,
            subscription = info.subscription,
            academicYear = info.academicYear,
            activeTerms = info.activeTerms
        )
        return api.updateSchoolInfo(dto).toDomain()
    }

    override suspend fun getStaff(role: String?, search: String?): List<StaffMember> {
        return api.getStaff(role, search).map { it.toDomain() }
    }

    override suspend fun getStaffMember(staffId: String): StaffMember {
        return api.getStaffMember(staffId).toDomain()
    }

    override suspend fun getClassRosters(): List<ClassRoster> {
        return api.getClassRosters().map { it.toDomain() }
    }

    override suspend fun getClassRoster(classId: String): ClassRoster {
        return api.getClassRoster(classId).toDomain()
    }

    override suspend fun addStudentToClass(classId: String, studentId: String) {
        api.addStudentToClass(classId, studentId)
    }

    override suspend fun removeStudentFromClass(classId: String, studentId: String) {
        api.removeStudentFromClass(classId, studentId)
    }

    override suspend fun getSchoolStats(): SchoolStats {
        return api.getSchoolStats().toDomain()
    }
}
