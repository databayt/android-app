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

class AuthUseCasesTest {

    private lateinit var authRepository: AuthRepository

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
        authRepository = mockk(relaxed = true)
    }

    // ──────────────────────────────────────────────
    // LoginUseCase
    // ──────────────────────────────────────────────

    @Test
    fun `LoginUseCase success returns Success`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns fakeAuthResult
        val useCase = LoginUseCase(authRepository)

        val result = useCase("student@hogwarts.edu", "password")

        assertTrue(result is Result.Success)
        assertEquals(fakeAuthResult, (result as Result.Success).data)
    }

    @Test
    fun `LoginUseCase exception returns Error`() = runTest {
        coEvery { authRepository.login(any(), any()) } throws AuthException("Invalid credentials")
        val useCase = LoginUseCase(authRepository)

        val result = useCase("student@hogwarts.edu", "wrong")

        assertTrue(result is Result.Error)
        assertEquals("Invalid credentials", (result as Result.Error).exception.message)
    }

    @Test
    fun `LoginUseCase passes correct params`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns fakeAuthResult
        val useCase = LoginUseCase(authRepository)

        useCase("test@mail.com", "Pass1234")

        coVerify { authRepository.login("test@mail.com", "Pass1234") }
    }

    // ──────────────────────────────────────────────
    // GoogleAuthUseCase
    // ──────────────────────────────────────────────

    @Test
    fun `GoogleAuthUseCase success returns Success`() = runTest {
        coEvery { authRepository.loginWithGoogle(any()) } returns fakeAuthResult
        val useCase = GoogleAuthUseCase(authRepository)

        val result = useCase("google-id-token")

        assertTrue(result is Result.Success)
        assertEquals(fakeAuthResult, (result as Result.Success).data)
    }

    @Test
    fun `GoogleAuthUseCase exception returns Error`() = runTest {
        coEvery { authRepository.loginWithGoogle(any()) } throws AuthException("Invalid token")
        val useCase = GoogleAuthUseCase(authRepository)

        val result = useCase("bad-token")

        assertTrue(result is Result.Error)
        assertEquals("Invalid token", (result as Result.Error).exception.message)
    }

    @Test
    fun `GoogleAuthUseCase passes idToken to repository`() = runTest {
        coEvery { authRepository.loginWithGoogle(any()) } returns fakeAuthResult
        val useCase = GoogleAuthUseCase(authRepository)

        useCase("my-google-token")

        coVerify { authRepository.loginWithGoogle("my-google-token") }
    }

    // ──────────────────────────────────────────────
    // FacebookAuthUseCase
    // ──────────────────────────────────────────────

    @Test
    fun `FacebookAuthUseCase success returns Success`() = runTest {
        coEvery { authRepository.loginWithFacebook(any()) } returns fakeAuthResult
        val useCase = FacebookAuthUseCase(authRepository)

        val result = useCase("fb-access-token")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `FacebookAuthUseCase exception returns Error`() = runTest {
        coEvery { authRepository.loginWithFacebook(any()) } throws AuthException("Facebook auth failed")
        val useCase = FacebookAuthUseCase(authRepository)

        val result = useCase("bad-token")

        assertTrue(result is Result.Error)
        assertEquals("Facebook auth failed", (result as Result.Error).exception.message)
    }

    // ──────────────────────────────────────────────
    // LogoutUseCase
    // ──────────────────────────────────────────────

    @Test
    fun `LogoutUseCase calls repository logout`() = runTest {
        val useCase = LogoutUseCase(authRepository)

        useCase()

        coVerify { authRepository.logout() }
    }

    @Test
    fun `LogoutUseCase propagates exception`() = runTest {
        coEvery { authRepository.logout() } throws RuntimeException("Network error")
        val useCase = LogoutUseCase(authRepository)

        var thrown = false
        try {
            useCase()
        } catch (e: RuntimeException) {
            thrown = true
            assertEquals("Network error", e.message)
        }
        assertTrue(thrown)
    }

    // ──────────────────────────────────────────────
    // RequestPasswordResetUseCase
    // ──────────────────────────────────────────────

    @Test
    fun `RequestPasswordResetUseCase success returns Success`() = runTest {
        val useCase = RequestPasswordResetUseCase(authRepository)

        val result = useCase("harry@hogwarts.edu")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `RequestPasswordResetUseCase exception returns Error`() = runTest {
        coEvery { authRepository.requestPasswordReset(any()) } throws AuthException("Email not found")
        val useCase = RequestPasswordResetUseCase(authRepository)

        val result = useCase("unknown@mail.com")

        assertTrue(result is Result.Error)
        assertEquals("Email not found", (result as Result.Error).exception.message)
    }

    @Test
    fun `RequestPasswordResetUseCase passes email to repository`() = runTest {
        val useCase = RequestPasswordResetUseCase(authRepository)

        useCase("harry@hogwarts.edu")

        coVerify { authRepository.requestPasswordReset("harry@hogwarts.edu") }
    }

    // ──────────────────────────────────────────────
    // VerifyOtpUseCase
    // ──────────────────────────────────────────────

    @Test
    fun `VerifyOtpUseCase success returns Success`() = runTest {
        val useCase = VerifyOtpUseCase(authRepository)

        val result = useCase("harry@hogwarts.edu", "123456")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `VerifyOtpUseCase exception returns Error`() = runTest {
        coEvery { authRepository.verifyOtp(any(), any()) } throws AuthException("Invalid OTP")
        val useCase = VerifyOtpUseCase(authRepository)

        val result = useCase("harry@hogwarts.edu", "000000")

        assertTrue(result is Result.Error)
        assertEquals("Invalid OTP", (result as Result.Error).exception.message)
    }

    @Test
    fun `VerifyOtpUseCase passes email and otp to repository`() = runTest {
        val useCase = VerifyOtpUseCase(authRepository)

        useCase("test@mail.com", "999999")

        coVerify { authRepository.verifyOtp("test@mail.com", "999999") }
    }

    // ──────────────────────────────────────────────
    // SetNewPasswordUseCase
    // ──────────────────────────────────────────────

    @Test
    fun `SetNewPasswordUseCase success returns Success`() = runTest {
        val useCase = SetNewPasswordUseCase(authRepository)

        val result = useCase("harry@hogwarts.edu", "123456", "NewPass1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `SetNewPasswordUseCase exception returns Error`() = runTest {
        coEvery { authRepository.setNewPassword(any(), any(), any()) } throws AuthException("Expired OTP")
        val useCase = SetNewPasswordUseCase(authRepository)

        val result = useCase("harry@hogwarts.edu", "000000", "NewPass1")

        assertTrue(result is Result.Error)
        assertEquals("Expired OTP", (result as Result.Error).exception.message)
    }

    @Test
    fun `SetNewPasswordUseCase passes all params to repository`() = runTest {
        val useCase = SetNewPasswordUseCase(authRepository)

        useCase("test@mail.com", "123456", "MyNewPassword1")

        coVerify { authRepository.setNewPassword("test@mail.com", "123456", "MyNewPassword1") }
    }
}
