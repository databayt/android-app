package org.hogwarts.android.feature.profile.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hogwarts.android.core.data.tenant.NoTenantContextException
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.profile.data.remote.ProfileApi
import org.hogwarts.android.feature.profile.data.remote.dto.ProfileDto
import org.hogwarts.android.feature.profile.data.remote.dto.SchoolDto
import org.hogwarts.android.feature.profile.data.remote.dto.StudentDto
import org.hogwarts.android.feature.profile.data.remote.dto.SectionDto
import org.hogwarts.android.feature.profile.domain.model.ProfileRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ProfileRepositoryImplTest {

    private val api: ProfileApi = mockk()
    private val tenantContext: TenantContext = mockk()

    private fun repo() = ProfileRepositoryImpl(api, tenantContext)

    private val sampleDto = ProfileDto(
        id = "u1",
        email = "j@example.com",
        username = "jane",
        avatarUrl = null,
        role = "STUDENT",
        bio = "hi",
        school = SchoolDto(id = "s1", name = "Hogwarts"),
        student = StudentDto(
            id = "stu1",
            givenName = "Jane",
            familyName = "Doe",
            section = SectionDto(id = "sec1", name = "A", grade = "10")
        )
    )

    @Test
    fun `getOwnProfile fails when no tenant context`() = runTest {
        every { tenantContext.requireSchoolId() } throws NoTenantContextException()
        assertThrows(NoTenantContextException::class.java) {
            kotlinx.coroutines.runBlocking { repo().getOwnProfile() }
        }
    }

    @Test
    fun `getOwnProfile maps DTO to domain`() = runTest {
        every { tenantContext.requireSchoolId() } returns "s1"
        coEvery { api.getProfile() } returns Response.success(sampleDto)

        val profile = repo().getOwnProfile()

        assertEquals("u1", profile.id)
        assertEquals(ProfileRole.STUDENT, profile.role)
        assertEquals("Jane Doe", profile.student?.fullName)
        assertEquals("10", profile.student?.gradeName)
        coVerify { api.getProfile() }
    }

    @Test
    fun `getContributions falls back to empty when API throws`() = runTest {
        every { tenantContext.requireSchoolId() } returns "s1"
        every { tenantContext.userRole } returns UserRole.STUDENT
        coEvery { api.getContributions(any(), any()) } throws RuntimeException("boom")

        val data = repo().getContributions()

        assertEquals(0, data.totalActivities)
        assertTrue(data.contributions.isEmpty())
    }

    @Test
    fun `updateProfile passes username and bio through`() = runTest {
        every { tenantContext.requireSchoolId() } returns "s1"
        coEvery { api.updateProfile(any()) } returns Response.success(sampleDto)

        repo().updateProfile(username = "jane2", bio = "new bio")

        coVerify {
            api.updateProfile(match { it.username == "jane2" && it.bio == "new bio" })
        }
    }
}
