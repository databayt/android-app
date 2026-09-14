package org.hogwarts.android.feature.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.AuthException
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.hogwarts.android.feature.auth.domain.model.SocialLogin
import org.hogwarts.android.feature.auth.domain.model.toAuthError
import org.hogwarts.android.feature.auth.testing.fakeAuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthUseCasesTest {

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
    }

    @Test
    fun `LoginUseCase success returns Success`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns fakeAuthResult
        val result = LoginUseCase(authRepository)("admin@balqalam.com", "1234")
        assertEquals(fakeAuthResult, (result as Result.Success).data)
        coVerify { authRepository.login("admin@balqalam.com", "1234") }
    }

    @Test
    fun `LoginUseCase keeps the error kind`() = runTest {
        coEvery { authRepository.login(any(), any()) } throws AuthException(AuthError.InvalidCredentials)
        val result = LoginUseCase(authRepository)("admin@balqalam.com", "wrong")
        assertEquals(AuthError.InvalidCredentials, (result as Result.Error).exception.toAuthError())
    }

    @Test
    fun `GoogleAuthUseCase passes token and school`() = runTest {
        coEvery { authRepository.loginWithGoogle("token", "school-1") } returns SocialLogin.Authenticated(fakeAuthResult)
        val result = GoogleAuthUseCase(authRepository)("token", "school-1")
        assertEquals(SocialLogin.Authenticated(fakeAuthResult), (result as Result.Success).data)
    }

    @Test
    fun `GoogleAuthUseCase surfaces needs school`() = runTest {
        val schools = listOf(SchoolInfo("s1", "الملك فهد", "King Fahd"))
        coEvery { authRepository.loginWithGoogle("token", null) } returns SocialLogin.NeedsSchool(schools)
        val result = GoogleAuthUseCase(authRepository)("token")
        assertEquals(SocialLogin.NeedsSchool(schools), (result as Result.Success).data)
    }

    @Test
    fun `LogoutUseCase calls repository logout`() = runTest {
        LogoutUseCase(authRepository)()
        coVerify { authRepository.logout() }
    }

    @Test
    fun `RequestPasswordResetUseCase success and failure`() = runTest {
        assertTrue(RequestPasswordResetUseCase(authRepository)("a@b.co") is Result.Success)
        coEvery { authRepository.requestPasswordReset(any()) } throws AuthException(AuthError.TooManyRequests)
        val result = RequestPasswordResetUseCase(authRepository)("a@b.co")
        assertEquals(AuthError.TooManyRequests, (result as Result.Error).exception.toAuthError())
    }

    @Test
    fun `SetNewPasswordUseCase passes all params`() = runTest {
        SetNewPasswordUseCase(authRepository)("a@b.co", "123456", "secret1")
        coVerify { authRepository.setNewPassword("a@b.co", "123456", "secret1") }
    }

    @Test
    fun `an IOException is a network error`() {
        assertEquals(AuthError.Network, java.io.IOException("offline").toAuthError())
        assertEquals(AuthError.Generic, IllegalStateException().toAuthError())
    }
}
