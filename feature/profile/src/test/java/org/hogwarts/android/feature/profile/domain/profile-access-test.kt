package org.hogwarts.android.feature.profile.domain

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.profile.domain.model.ProfileRole
import org.hogwarts.android.feature.profile.domain.model.UserProfile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileAccessTest {

    private fun profile(
        userId: String = "u1",
        schoolId: String = "s1",
        role: ProfileRole = ProfileRole.STUDENT
    ) = UserProfile(
        id = userId,
        userId = userId,
        schoolId = schoolId,
        email = "u@example.com",
        role = role
    )

    @Test
    fun `owner can always view and edit`() {
        val p = ProfileAccess.resolve(
            viewerRole = UserRole.STUDENT,
            viewerUserId = "u1",
            target = profile(userId = "u1"),
            sameSchool = true
        )
        assertTrue(p.canView)
        assertTrue(p.canEdit)
    }

    @Test
    fun `cross-school access denied even for admin`() {
        val p = ProfileAccess.resolve(
            viewerRole = UserRole.ADMIN,
            viewerUserId = "viewer",
            target = profile(userId = "other", schoolId = "s2"),
            sameSchool = false
        )
        assertFalse(p.canView)
    }

    @Test
    fun `admin can view anyone in same school`() {
        val p = ProfileAccess.resolve(
            viewerRole = UserRole.ADMIN,
            viewerUserId = "viewer",
            target = profile(userId = "other", role = ProfileRole.TEACHER),
            sameSchool = true
        )
        assertTrue(p.canView)
        assertFalse(p.canEdit)
    }

    @Test
    fun `student cannot view another student's parent`() {
        val p = ProfileAccess.resolve(
            viewerRole = UserRole.STUDENT,
            viewerUserId = "viewer",
            target = profile(userId = "other", role = ProfileRole.PARENT),
            sameSchool = true
        )
        assertFalse(p.canView)
    }

    @Test
    fun `teacher can view students and other teachers`() {
        val onStudent = ProfileAccess.resolve(
            viewerRole = UserRole.TEACHER,
            viewerUserId = "t1",
            target = profile(userId = "s1", role = ProfileRole.STUDENT),
            sameSchool = true
        )
        val onTeacher = ProfileAccess.resolve(
            viewerRole = UserRole.TEACHER,
            viewerUserId = "t1",
            target = profile(userId = "t2", role = ProfileRole.TEACHER),
            sameSchool = true
        )
        assertTrue(onStudent.canView)
        assertTrue(onTeacher.canView)
    }

    @Test
    fun `unauthenticated viewer is denied`() {
        val p = ProfileAccess.resolve(
            viewerRole = null,
            viewerUserId = null,
            target = profile(),
            sameSchool = true
        )
        assertFalse(p.canView)
    }
}
