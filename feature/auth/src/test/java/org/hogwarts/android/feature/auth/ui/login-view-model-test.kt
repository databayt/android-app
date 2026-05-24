package org.hogwarts.android.feature.auth.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.core.security.CredentialManager
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import org.hogwarts.android.feature.auth.domain.usecase.FacebookAuthUseCase
import org.hogwarts.android.feature.auth.domain.usecase.GoogleAuthUseCase
import org.hogwarts.android.feature.auth.domain.usecase.LoginUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var googleAuthUseCase: GoogleAuthUseCase
    private lateinit var facebookAuthUseCase: FacebookAuthUseCase
    private lateinit var biometricHelper: BiometricHelper
    private lateinit var credentialManager: CredentialManager
    private lateinit var viewModel: LoginViewModel

    private val fakeAuthResult = AuthResult(
        userId = "user-1",
        email = "student@hogwarts.edu",
        schoolId = "school-1",
        role = UserRole.STUDENT,
        givenName = "Harry",
        familyName = "Potter",
        accessToken = "fake-token",
        refreshToken = "fake-refresh",
        expiresAt = System.currentTimeMillis() + 3600_000
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        googleAuthUseCase = mockk()
        facebookAuthUseCase = mockk()
        biometricHelper = mockk()
        credentialManager = mockk(relaxed = true)
        every { biometricHelper.isBiometricAvailable } returns false
        every { credentialManager.hasSavedCredentials } returns false
        viewModel = LoginViewModel(loginUseCase, googleAuthUseCase, facebookAuthUseCase, biometricHelper, credentialManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ──────────────────────────────────────────────
    // Initial State
    // ──────────────────────────────────────────────

    @Test
    fun `initial state is Idle`() = runTest(testDispatcher) {
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    // ──────────────────────────────────────────────
    // Input Validation
    // ──────────────────────────────────────────────

    @Test
    fun `login with blank email emits Error`() = runTest(testDispatcher) {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("password123")
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Email is required", (state as LoginUiState.Error).message)
    }

    @Test
    fun `login with invalid email format emits Error`() = runTest(testDispatcher) {
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("password123")
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Invalid email format", (state as LoginUiState.Error).message)
    }

    @Test
    fun `login with blank password emits Error`() = runTest(testDispatcher) {
        viewModel.onEmailChange("student@hogwarts.edu")
        viewModel.onPasswordChange("")
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Password is required", (state as LoginUiState.Error).message)
    }

    // ──────────────────────────────────────────────
    // Successful Login
    // ──────────────────────────────────────────────

    @Test
    fun `login success emits Loading then Success`() = runTest(testDispatcher) {
        coEvery { loginUseCase.invoke(any(), any()) } returns Result.Success(fakeAuthResult)

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())

            viewModel.onEmailChange("student@hogwarts.edu")
            viewModel.onPasswordChange("password123")
            viewModel.login()

            assertEquals(LoginUiState.Loading, awaitItem())
            assertEquals(LoginUiState.Success, awaitItem())
        }
    }

    @Test
    fun `login success saves credentials when biometric is available`() = runTest(testDispatcher) {
        every { biometricHelper.isBiometricAvailable } returns true
        coEvery { loginUseCase.invoke(any(), any()) } returns Result.Success(fakeAuthResult)

        // Re-create viewModel to pick up updated biometric mock
        viewModel = LoginViewModel(loginUseCase, googleAuthUseCase, facebookAuthUseCase, biometricHelper, credentialManager)

        viewModel.onEmailChange("student@hogwarts.edu")
        viewModel.onPasswordChange("password123")
        viewModel.login()
        advanceUntilIdle()

        verify { credentialManager.saveCredentials("student@hogwarts.edu", "password123") }
        assertEquals(LoginUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `login success does not save credentials when biometric is unavailable`() = runTest(testDispatcher) {
        every { biometricHelper.isBiometricAvailable } returns false
        coEvery { loginUseCase.invoke(any(), any()) } returns Result.Success(fakeAuthResult)

        viewModel.onEmailChange("student@hogwarts.edu")
        viewModel.onPasswordChange("password123")
        viewModel.login()
        advanceUntilIdle()

        verify(exactly = 0) { credentialManager.saveCredentials(any(), any()) }
        assertEquals(LoginUiState.Success, viewModel.uiState.value)
    }

    // ──────────────────────────────────────────────
    // Failed Login
    // ──────────────────────────────────────────────

    @Test
    fun `login error emits Loading then Error`() = runTest(testDispatcher) {
        coEvery { loginUseCase.invoke(any(), any()) } returns Result.Error(
            RuntimeException("Invalid credentials")
        )

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())

            viewModel.onEmailChange("student@hogwarts.edu")
            viewModel.onPasswordChange("wrongpassword")
            viewModel.login()

            assertEquals(LoginUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is LoginUiState.Error)
            assertEquals("Invalid credentials", (error as LoginUiState.Error).message)
        }
    }

    @Test
    fun `login error with null message falls back to default`() = runTest(testDispatcher) {
        coEvery { loginUseCase.invoke(any(), any()) } returns Result.Error(
            RuntimeException()
        )

        viewModel.onEmailChange("student@hogwarts.edu")
        viewModel.onPasswordChange("password123")
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Login failed", (state as LoginUiState.Error).message)
    }

    // ──────────────────────────────────────────────
    // Error Clearing
    // ──────────────────────────────────────────────

    @Test
    fun `onEmailChange clears error state`() = runTest(testDispatcher) {
        // Trigger an error first
        viewModel.onEmailChange("")
        viewModel.login()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoginUiState.Error)

        // Typing should clear the error
        viewModel.onEmailChange("s")
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `onPasswordChange clears error state`() = runTest(testDispatcher) {
        // Trigger an error first
        viewModel.onEmailChange("")
        viewModel.login()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoginUiState.Error)

        // Typing password should also clear the error
        viewModel.onPasswordChange("p")
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    // ──────────────────────────────────────────────
    // Biometric Login
    // ──────────────────────────────────────────────

    @Test
    fun `biometric login success emits Loading then Success`() = runTest(testDispatcher) {
        every { credentialManager.getCredentials() } returns ("student@hogwarts.edu" to "password123")
        coEvery { loginUseCase.invoke("student@hogwarts.edu", "password123") } returns Result.Success(fakeAuthResult)

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())

            viewModel.onBiometricSuccess()

            assertEquals(LoginUiState.Loading, awaitItem())
            assertEquals(LoginUiState.Success, awaitItem())
        }
    }

    @Test
    fun `biometric login with no saved credentials emits Error`() = runTest(testDispatcher) {
        every { credentialManager.getCredentials() } returns null

        viewModel.onBiometricSuccess()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals(
            "No saved credentials. Please login with email and password.",
            (state as LoginUiState.Error).message
        )
    }

    @Test
    fun `biometric login failure clears credentials and emits Error`() = runTest(testDispatcher) {
        every { credentialManager.getCredentials() } returns ("student@hogwarts.edu" to "old-password")
        coEvery { loginUseCase.invoke(any(), any()) } returns Result.Error(RuntimeException("Token expired"))

        viewModel.onBiometricSuccess()
        advanceUntilIdle()

        verify { credentialManager.clearCredentials() }
        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals(
            "Biometric login failed. Please login with email and password.",
            (state as LoginUiState.Error).message
        )
    }

    @Test
    fun `onBiometricError with non-cancel message emits Error`() = runTest(testDispatcher) {
        viewModel.onBiometricError("Too many attempts. Try again later.")
        assertEquals(
            LoginUiState.Error("Too many attempts. Try again later."),
            viewModel.uiState.value
        )
    }

    @Test
    fun `onBiometricError with cancel message does not emit Error`() = runTest(testDispatcher) {
        viewModel.onBiometricError("Authentication cancelled")
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    // ──────────────────────────────────────────────
    // Biometric Availability Properties
    // ──────────────────────────────────────────────

    @Test
    fun `isBiometricAvailable delegates to BiometricHelper`() {
        every { biometricHelper.isBiometricAvailable } returns true
        viewModel = LoginViewModel(loginUseCase, googleAuthUseCase, facebookAuthUseCase, biometricHelper, credentialManager)
        assertTrue(viewModel.isBiometricAvailable)
    }

    @Test
    fun `canUseBiometricLogin requires both biometric and saved credentials`() {
        every { biometricHelper.isBiometricAvailable } returns true
        every { credentialManager.hasSavedCredentials } returns true
        viewModel = LoginViewModel(loginUseCase, googleAuthUseCase, facebookAuthUseCase, biometricHelper, credentialManager)
        assertTrue(viewModel.canUseBiometricLogin)
    }

    @Test
    fun `canUseBiometricLogin is false when biometric is unavailable`() {
        every { biometricHelper.isBiometricAvailable } returns false
        every { credentialManager.hasSavedCredentials } returns true
        viewModel = LoginViewModel(loginUseCase, googleAuthUseCase, facebookAuthUseCase, biometricHelper, credentialManager)
        assertFalse(viewModel.canUseBiometricLogin)
    }

    @Test
    fun `canUseBiometricLogin is false when no saved credentials`() {
        every { biometricHelper.isBiometricAvailable } returns true
        every { credentialManager.hasSavedCredentials } returns false
        viewModel = LoginViewModel(loginUseCase, googleAuthUseCase, facebookAuthUseCase, biometricHelper, credentialManager)
        assertFalse(viewModel.canUseBiometricLogin)
    }
}
