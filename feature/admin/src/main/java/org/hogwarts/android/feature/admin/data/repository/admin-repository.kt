package org.hogwarts.android.feature.admin.data.repository

import org.hogwarts.android.feature.admin.domain.model.ClassRoster
import org.hogwarts.android.feature.admin.domain.model.SchoolInfo
import org.hogwarts.android.feature.admin.domain.model.SchoolStats
import org.hogwarts.android.feature.admin.domain.model.StaffMember

/**
 * Repository interface for school administration data.
 */
interface AdminRepository {
    suspend fun getSchoolInfo(): SchoolInfo
    suspend fun updateSchoolInfo(info: SchoolInfo): SchoolInfo
    suspend fun getStaff(role: String? = null, search: String? = null): List<StaffMember>
    suspend fun getStaffMember(staffId: String): StaffMember
    suspend fun getClassRosters(): List<ClassRoster>
    suspend fun getClassRoster(classId: String): ClassRoster
    suspend fun addStudentToClass(classId: String, studentId: String)
    suspend fun removeStudentFromClass(classId: String, studentId: String)
    suspend fun getSchoolStats(): SchoolStats
}
