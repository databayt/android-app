package org.hogwarts.android.feature.auth.ui

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.AuthException
import org.hogwarts.android.feature.auth.domain.model.FieldError
import org.hogwarts.android.feature.auth.domain.usecase.RequestPasswordResetUseCase
import org.hogwarts.android.feature.auth.domain.usecase.SetNewPasswordUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** The reset flow: email → code → new password. */
@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var requestReset: RequestPasswordResetUseCase
    private lateinit var setNewPassword: SetNewPasswordUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        requestReset = mockk()
        setNewPassword = mockk()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    // Email step

    @Test
    fun `invalid email never calls the server`() = runTest(dispatcher) {
        val vm = ForgotPasswordViewModel(requestReset)
        vm.onEmailChange("parent")
        vm.submit()
        advanceUntilIdle()
        assertEquals(FieldError.InvalidEmail, vm.uiState.value.emailError)
        coVerify(exactly = 0) { requestReset(any()) }
    }

    @Test
    fun `sent code moves on with the normalized email`() = runTest(dispatcher) {
        coEvery { requestReset("parent@balqalam.com") } returns Result.Success(Unit)
        val vm = ForgotPasswordViewModel(requestReset)
        vm.onEmailChange(" Parent@Balqalam.com ")
        vm.submit()
        advanceUntilIdle()
        assertEquals("parent@balqalam.com", vm.uiState.value.codeSentTo)
        vm.onCodeStepOpened()
        assertNull(vm.uiState.value.codeSentTo)
    }

    @Test
    fun `rate limit is shown`() = runTest(dispatcher) {
        coEvery { requestReset(any()) } returns Result.Error(AuthException(AuthError.TooManyRequests))
        val vm = ForgotPasswordViewModel(requestReset)
        vm.onEmailChange("a@b.co")
        vm.submit()
        advanceUntilIdle()
        assertEquals(AuthError.TooManyRequests, vm.uiState.value.error)
        assertNull(vm.uiState.value.codeSentTo)
    }

    // Code step

    private fun otpViewModel() = VerifyOtpViewModel(SavedStateHandle(mapOf("email" to "parent@balqalam.com")), requestReset)

    @Test
    fun `code keeps six digits only and confirms without calling verify-otp`() = runTest(dispatcher) {
        val vm = otpViewModel()
        vm.onOtpChange("12a34-5678")
        assertEquals("123456", vm.uiState.value.otp)
        vm.submit()
        assertEquals("123456", vm.uiState.value.confirmedOtp)
        vm.onNewPasswordOpened()
        assertNull(vm.uiState.value.confirmedOtp)
        coVerify(exactly = 0) { requestReset(any()) }
    }

    @Test
    fun `short code is invalid`() = runTest(dispatcher) {
        val vm = otpViewModel()
        vm.onOtpChange("123")
        vm.submit()
        assertEquals(AuthError.InvalidCode, vm.uiState.value.error)
        assertNull(vm.uiState.value.confirmedOtp)
    }

    @Test
    fun `resend waits out the 90 second cooldown`() = runTest(dispatcher) {
        coEvery { requestReset("parent@balqalam.com") } returns Result.Success(Unit)
        val vm = otpViewModel()
        runCurrent()
        assertEquals(90, vm.uiState.value.resendCooldown)

        vm.resend()
        runCurrent()
        coVerify(exactly = 0) { requestReset(any()) }

        advanceTimeBy(90_001)
        assertEquals(0, vm.uiState.value.resendCooldown)
        vm.onOtpChange("12")
        vm.resend()
        runCurrent()
        coVerify(exactly = 1) { requestReset("parent@balqalam.com") }
        assertTrue(vm.uiState.value.resent)
        assertEquals("", vm.uiState.value.otp)
        assertEquals(90, vm.uiState.value.resendCooldown)
    }

    // New password step

    private fun newPasswordViewModel() =
        NewPasswordViewModel(SavedStateHandle(mapOf("email" to "parent@balqalam.com", "otp" to "123456")), setNewPassword)

    @Test
    fun `new password needs six characters`() = runTest(dispatcher) {
        val vm = newPasswordViewModel()
        vm.onPasswordChange("12345")
        vm.submit()
        advanceUntilIdle()
        assertEquals(FieldError.PasswordTooShort, vm.uiState.value.passwordError)
        coVerify(exactly = 0) { setNewPassword(any(), any(), any()) }
    }

    @Test
    fun `new password sends email, code and password`() = runTest(dispatcher) {
        coEvery { setNewPassword("parent@balqalam.com", "123456", "secret1") } returns Result.Success(Unit)
        val vm = newPasswordViewModel()
        vm.onPasswordChange("secret1")
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.updated)
        assertEquals("", vm.uiState.value.password)
    }

    @Test
    fun `a consumed or wrong code comes back as invalid code`() = runTest(dispatcher) {
        coEvery { setNewPassword(any(), any(), any()) } returns Result.Error(AuthException(AuthError.InvalidCode))
        val vm = newPasswordViewModel()
        vm.onPasswordChange("secret1")
        vm.submit()
        advanceUntilIdle()
        assertEquals(AuthError.InvalidCode, vm.uiState.value.error)
        assertFalse(vm.uiState.value.updated)
    }
}
