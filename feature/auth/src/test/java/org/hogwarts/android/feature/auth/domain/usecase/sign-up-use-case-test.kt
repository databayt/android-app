package org.hogwarts.android.feature.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.auth.data.repository.AuthException
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignUpUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignUpUseCase

    private val fakeAuthResult = AuthResult(
        userId = "user-1",
        email = "student@hogwarts.edu",
        schoolId = "school-1",
        role = UserRole.STUDENT,
        givenName = "Harry",
        familyName = "Potter",
        accessToken = "token",
        refreshToken = "refresh",
        expiresAt = System.currentTimeMillis() + 3600_000
    )

    @Before
    fun setup() {
        authRepository = mockk()
        useCase = SignUpUseCase(authRepository)
    }

    @Test
    fun `successful registration returns Success`() = runTest {
        coEvery {
            authRepository.register("Harry", "Potter", "harry@hogwarts.edu", "Password1", "school-1")
        } returns fakeAuthResult

        val result = useCase("Harry", "Potter", "harry@hogwarts.edu", "Password1", "school-1")

        assertTrue(result is Result.Success)
        assertEquals(fakeAuthResult, (result as Result.Success).data)
    }

    @Test
    fun `registration passes all params to repository`() = runTest {
        coEvery {
            authRepository.register(any(), any(), any(), any(), any())
        } returns fakeAuthResult

        useCase("Ahmed", "Mohammed", "ahmed@school.edu", "Pass1234", "school-2")

        coVerify {
            authRepository.register("Ahmed", "Mohammed", "ahmed@school.edu", "Pass1234", "school-2")
        }
    }

    @Test
    fun `repository exception returns Error`() = runTest {
        coEvery {
            authRepository.register(any(), any(), any(), any(), any())
        } throws AuthException("Account already exists")

        val result = useCase("Harry", "Potter", "harry@hogwarts.edu", "Password1", "school-1")

        assertTrue(result is Result.Error)
        assertEquals("Account already exists", (result as Result.Error).exception.message)
    }

    @Test
    fun `network exception returns Error`() = runTest {
        coEvery {
            authRepository.register(any(), any(), any(), any(), any())
        } throws java.io.IOException("Network error")

        val result = useCase("Harry", "Potter", "harry@hogwarts.edu", "Password1", "school-1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is java.io.IOException)
    }
}
