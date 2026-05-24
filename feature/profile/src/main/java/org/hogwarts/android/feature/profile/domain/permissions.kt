package org.hogwarts.android.feature.profile.domain

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.profile.domain.model.ProfileRole
import org.hogwarts.android.feature.profile.domain.model.UserProfile

/**
 * Web reference: profile/detail/permissions.ts
 *
 * Profile visibility rules in the Hogwarts mobile app:
 * - Anyone can see their own profile
 * - Admin/super-admin: can see anyone in their school
 * - Teacher: can see other teachers + students in their school
 * - Guardian: can see their linked children + the children's teachers
 * - Student: can see their own teachers + classmates only
 *
 * The current target/viewer must share `schoolId` (enforced by backend tenant scoping).
 */
data class ProfilePermissions(
    val canView: Boolean,
    val canEdit: Boolean,
    val canMessage: Boolean
)

object ProfileAccess {

    fun resolve(
        viewerRole: UserRole?,
        viewerUserId: String?,
        target: UserProfile,
        sameSchool: Boolean
    ): ProfilePermissions {
        if (viewerUserId == null) return denied()
        val isOwner = viewerUserId == target.userId
        if (isOwner) return ProfilePermissions(canView = true, canEdit = true, canMessage = false)
        if (!sameSchool) return denied()

        val canView = when (viewerRole) {
            UserRole.ADMIN, UserRole.SUPER_ADMIN -> true
            UserRole.TEACHER -> target.role in setOf(ProfileRole.TEACHER, ProfileRole.STUDENT, ProfileRole.PARENT)
            UserRole.STUDENT -> target.role in setOf(ProfileRole.TEACHER, ProfileRole.STUDENT)
            UserRole.GUARDIAN -> target.role in setOf(ProfileRole.TEACHER, ProfileRole.STUDENT)
            null -> false
        }
        return ProfilePermissions(
            canView = canView,
            canEdit = false,
            canMessage = canView && viewerRole != UserRole.STUDENT
        )
    }

    private fun denied() = ProfilePermissions(canView = false, canEdit = false, canMessage = false)
}
