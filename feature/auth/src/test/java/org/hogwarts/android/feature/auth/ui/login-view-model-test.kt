package org.hogwarts.android.feature.auth.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.hogwarts.android.core.security.BiometricHelper
import org.hogwarts.android.core.security.CredentialManager
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.AuthException
import org.hogwarts.android.feature.auth.domain.model.DemoAccounts
import org.hogwarts.android.feature.auth.domain.model.DemoRole
import org.hogwarts.android.feature.auth.domain.model.FieldError
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.hogwarts.android.feature.auth.domain.model.SocialLogin
import org.hogwarts.android.feature.auth.domain.usecase.GoogleAuthUseCase
import org.hogwarts.android.feature.auth.domain.usecase.LoginUseCase
import org.hogwarts.android.feature.auth.testing.fakeAuthResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var googleAuthUseCase: GoogleAuthUseCase
    private lateinit var biometricHelper: BiometricHelper
    private lateinit var credentialManager: CredentialManager

    private val schools = listOf(SchoolInfo("s1", "الملك فهد", "King Fahd"), SchoolInfo("s2", "نموذج"))

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        loginUseCase = mockk()
        googleAuthUseCase = mockk()
        biometricHelper = mockk()
        credentialManager = mockk(relaxed = true)
        every { biometricHelper.isBiometricAvailable } returns false
        every { credentialManager.hasSavedCredentials } returns false
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LoginViewModel(loginUseCase, googleAuthUseCase, biometricHelper, credentialManager)

    // Validation — createLoginSchema

    @Test
    fun `empty form shows both field messages and calls nothing`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.login()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(FieldError.IdentifierRequired, state.identifierError)
        assertEquals(FieldError.PasswordRequired, state.passwordError)
        coVerify(exactly = 0) { loginUseCase(any(), any()) }
    }

    @Test
    fun `a student username is a valid identifier`() = runTest(dispatcher) {
        coEvery { loginUseCase("stu.2026-01", "pw") } returns Result.Success(fakeAuthResult)
        val vm = viewModel()
        vm.onIdentifierChange(" stu.2026-01 ")
        vm.onPasswordChange("pw")
        vm.login()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.signedIn)
    }

    @Test
    fun `malformed identifier is rejected`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onIdentifierChange("admin@")
        vm.onPasswordChange("pw")
        vm.login()
        assertEquals(FieldError.InvalidIdentifier, vm.uiState.value.identifierError)
        assertNull(vm.uiState.value.passwordError)
    }

    @Test
    fun `typing clears the field message and the form error`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.login()
        vm.onIdentifierChange("a")
        assertNull(vm.uiState.value.identifierError)
    }

    // Server answers

    @Test
    fun `success goes Loading then signed in`() = runTest(dispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.Success(fakeAuthResult)
        val vm = viewModel()
        vm.onIdentifierChange("admin@balqalam.com")
        vm.onPasswordChange("1234")
        vm.uiState.test {
            assertFalse(awaitItem().isLoading)
            vm.login()
            assertTrue(awaitItem().isLoading)
            val done = awaitItem()
            assertFalse(done.isLoading)
            assertTrue(done.signedIn)
        }
    }

    @Test
    fun `bad password maps to invalid credentials and clears the password`() = runTest(dispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.Error(AuthException(AuthError.InvalidCredentials))
        val vm = viewModel()
        vm.onIdentifierChange("admin@balqalam.com")
        vm.onPasswordChange("nope")
        vm.login()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(AuthError.InvalidCredentials, state.error)
        assertEquals("", state.password)
        assertEquals("admin@balqalam.com", state.identifier)
        assertFalse(state.signedIn)
    }

    @Test
    fun `suspended, network and unknown failures keep their kind`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onIdentifierChange("admin@balqalam.com")
        listOf(
            AuthException(AuthError.Suspended) to AuthError.Suspended,
            java.io.IOException() to AuthError.Network,
            IllegalStateException() to AuthError.Generic,
        ).forEach { (thrown, expected) ->
            coEvery { loginUseCase(any(), any()) } returns Result.Error(thrown)
            vm.onPasswordChange("x")
            vm.login()
            advanceUntilIdle()
            assertEquals(expected, vm.uiState.value.error)
        }
    }

    @Test
    fun `credentials are remembered only when biometrics exist`() = runTest(dispatcher) {
        every { biometricHelper.isBiometricAvailable } returns true
        coEvery { loginUseCase(any(), any()) } returns Result.Success(fakeAuthResult)
        val vm = viewModel()
        vm.onIdentifierChange("admin@balqalam.com")
        vm.onPasswordChange("1234")
        vm.login()
        advanceUntilIdle()
        verify { credentialManager.saveCredentials("admin@balqalam.com", "1234") }
    }

    // Google + needs_school

    @Test
    fun `needs_school shows the picker, then the chosen school signs in`() = runTest(dispatcher) {
        coEvery { googleAuthUseCase("id-token", null) } returns Result.Success(SocialLogin.NeedsSchool(schools))
        coEvery { googleAuthUseCase("id-token", "s2") } returns Result.Success(SocialLogin.Authenticated(fakeAuthResult))
        val vm = viewModel()

        vm.signInWithGoogle("id-token")
        advanceUntilIdle()
        assertEquals(schools, vm.uiState.value.schools)
        assertFalse(vm.uiState.value.signedIn)

        vm.onSchoolSelected("s2")
        advanceUntilIdle()
        assertNull(vm.uiState.value.schools)
        assertTrue(vm.uiState.value.signedIn)
        coVerify { googleAuthUseCase("id-token", "s2") }
    }

    @Test
    fun `dismissing the picker forgets the token`() = runTest(dispatcher) {
        coEvery { googleAuthUseCase(any(), null) } returns Result.Success(SocialLogin.NeedsSchool(schools))
        val vm = viewModel()
        vm.signInWithGoogle("id-token")
        advanceUntilIdle()
        vm.dismissSchoolPicker()
        vm.onSchoolSelected("s1")
        advanceUntilIdle()
        assertNull(vm.uiState.value.schools)
        coVerify(exactly = 0) { googleAuthUseCase(any(), "s1") }
    }

    @Test
    fun `a suspended school membership closes the picker with an error`() = runTest(dispatcher) {
        coEvery { googleAuthUseCase(any(), null) } returns Result.Success(SocialLogin.NeedsSchool(schools))
        coEvery { googleAuthUseCase(any(), "s1") } returns Result.Error(AuthException(AuthError.Suspended))
        val vm = viewModel()
        vm.signInWithGoogle("id-token")
        advanceUntilIdle()
        vm.onSchoolSelected("s1")
        advanceUntilIdle()
        assertNull(vm.uiState.value.schools)
        assertEquals(AuthError.Suspended, vm.uiState.value.error)
    }

    @Test
    fun `a network failure keeps the picker for a retry`() = runTest(dispatcher) {
        coEvery { googleAuthUseCase(any(), null) } returns Result.Success(SocialLogin.NeedsSchool(schools))
        coEvery { googleAuthUseCase(any(), "s1") } returns Result.Error(AuthException(AuthError.Network))
        val vm = viewModel()
        vm.signInWithGoogle("id-token")
        advanceUntilIdle()
        vm.onSchoolSelected("s1")
        advanceUntilIdle()
        assertEquals(schools, vm.uiState.value.schools)
        assertEquals(AuthError.Network, vm.uiState.value.error)
    }

    @Test
    fun `no school for the email is an error`() = runTest(dispatcher) {
        coEvery { googleAuthUseCase(any(), null) } returns Result.Error(AuthException(AuthError.NoSchool))
        val vm = viewModel()
        vm.signInWithGoogle("id-token")
        advanceUntilIdle()
        assertEquals(AuthError.NoSchool, vm.uiState.value.error)
        assertNull(vm.uiState.value.schools)
    }

    // Demo picker (debug builds)

    @Test
    fun `demo picker preselects admin and signs in with the seeded password`() = runTest(dispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.Success(fakeAuthResult)
        val vm = viewModel()
        assertEquals(DemoRole.entries, vm.uiState.value.demoRoles)
        assertEquals(DemoRole.Admin, vm.uiState.value.demoRole)

        vm.showDemo()
        assertEquals(LoginMode.Demo, vm.uiState.value.mode)
        vm.onDemoRoleSelected(DemoRole.Guardian)
        vm.loginAsDemo()
        advanceUntilIdle()

        coVerify { loginUseCase("parent@balqalam.com", DemoAccounts.password) }
        verify(exactly = 0) { credentialManager.saveCredentials(any(), any()) }
        assertTrue(vm.uiState.value.signedIn)
    }

    // Biometric

    @Test
    fun `biometric success signs in with saved credentials`() = runTest(dispatcher) {
        every { credentialManager.getCredentials() } returns ("admin@balqalam.com" to "1234")
        coEvery { loginUseCase("admin@balqalam.com", "1234") } returns Result.Success(fakeAuthResult)
        val vm = viewModel()
        vm.onBiometricSuccess()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.signedIn)
    }

    @Test
    fun `stale biometric credentials are cleared`() = runTest(dispatcher) {
        every { credentialManager.getCredentials() } returns ("admin@balqalam.com" to "old")
        coEvery { loginUseCase(any(), any()) } returns Result.Error(AuthException(AuthError.InvalidCredentials))
        val vm = viewModel()
        vm.onBiometricSuccess()
        advanceUntilIdle()
        verify { credentialManager.clearCredentials() }
        assertEquals(AuthError.BiometricFailed, vm.uiState.value.error)
        assertFalse(vm.uiState.value.canUseBiometric)
    }

    @Test
    fun `cancelling the prompt is not an error`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onBiometricError("Authentication cancelled")
        assertNull(vm.uiState.value.error)
        vm.onBiometricError("Too many attempts. Try again later.")
        assertEquals(AuthError.BiometricFailed, vm.uiState.value.error)
    }

    @Test
    fun `biometric button needs hardware and saved credentials`() {
        every { biometricHelper.isBiometricAvailable } returns true
        every { credentialManager.hasSavedCredentials } returns true
        assertTrue(viewModel().uiState.value.canUseBiometric)
        every { credentialManager.hasSavedCredentials } returns false
        assertFalse(viewModel().uiState.value.canUseBiometric)
    }
}
