package org.hogwarts.android.feature.auth.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.domain.usecase.RequestPasswordResetUseCase
import org.hogwarts.android.feature.auth.domain.usecase.SetNewPasswordUseCase
import org.hogwarts.android.feature.auth.domain.usecase.VerifyOtpUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var requestPasswordResetUseCase: RequestPasswordResetUseCase
    private lateinit var verifyOtpUseCase: VerifyOtpUseCase
    private lateinit var setNewPasswordUseCase: SetNewPasswordUseCase
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        requestPasswordResetUseCase = mockk()
        verifyOtpUseCase = mockk()
        setNewPasswordUseCase = mockk()
        viewModel = ForgotPasswordViewModel(
            requestPasswordResetUseCase,
            verifyOtpUseCase,
            setNewPasswordUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ──────────────────────────────────────────────
    // Initial State
    // ──────────────────────────────────────────────

    @Test
    fun `initial state is EMAIL step with empty fields`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        assertEquals(ResetStep.EMAIL, state.step)
        assertEquals("", state.email)
        assertEquals("", state.otp)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // ──────────────────────────────────────────────
    // Field Changes
    // ──────────────────────────────────────────────

    @Test
    fun `onEmailChange updates email and clears error`() = runTest(testDispatcher) {
        viewModel.onEmailChange("test@mail.com")
        assertEquals("test@mail.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onOtpChange accepts digits up to 6 chars`() = runTest(testDispatcher) {
        viewModel.onOtpChange("123456")
        assertEquals("123456", viewModel.uiState.value.otp)
    }

    @Test
    fun `onOtpChange rejects more than 6 digits`() = runTest(testDispatcher) {
        viewModel.onOtpChange("123456")
        viewModel.onOtpChange("1234567")
        assertEquals("123456", viewModel.uiState.value.otp)
    }

    @Test
    fun `onOtpChange rejects non-digit characters`() = runTest(testDispatcher) {
        viewModel.onOtpChange("12ab34")
        assertEquals("", viewModel.uiState.value.otp)
    }

    @Test
    fun `onNewPasswordChange updates newPassword`() = runTest(testDispatcher) {
        viewModel.onNewPasswordChange("NewPass1")
        assertEquals("NewPass1", viewModel.uiState.value.newPassword)
    }

    @Test
    fun `onConfirmPasswordChange updates confirmPassword`() = runTest(testDispatcher) {
        viewModel.onConfirmPasswordChange("NewPass1")
        assertEquals("NewPass1", viewModel.uiState.value.confirmPassword)
    }

    // ──────────────────────────────────────────────
    // Step 1: Request Reset
    // ──────────────────────────────────────────────

    @Test
    fun `requestReset with blank email shows error`() = runTest(testDispatcher) {
        viewModel.requestReset()
        advanceUntilIdle()

        assertEquals("Please enter a valid email", viewModel.uiState.value.error)
        assertEquals(ResetStep.EMAIL, viewModel.uiState.value.step)
    }

    @Test
    fun `requestReset with email missing @ shows error`() = runTest(testDispatcher) {
        viewModel.onEmailChange("invalidemail")
        viewModel.requestReset()
        advanceUntilIdle()

        assertEquals("Please enter a valid email", viewModel.uiState.value.error)
    }

    @Test
    fun `requestReset success moves to OTP step`() = runTest(testDispatcher) {
        coEvery { requestPasswordResetUseCase.invoke(any()) } returns Result.Success(Unit)

        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.requestReset()
        advanceUntilIdle()

        assertEquals(ResetStep.OTP, viewModel.uiState.value.step)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `requestReset failure shows error and stays on EMAIL step`() = runTest(testDispatcher) {
        coEvery { requestPasswordResetUseCase.invoke(any()) } returns Result.Error(
            RuntimeException("Email not found")
        )

        viewModel.onEmailChange("unknown@hogwarts.edu")
        viewModel.requestReset()
        advanceUntilIdle()

        assertEquals(ResetStep.EMAIL, viewModel.uiState.value.step)
        assertEquals("Email not found", viewModel.uiState.value.error)
    }

    @Test
    fun `requestReset error with null message shows default`() = runTest(testDispatcher) {
        coEvery { requestPasswordResetUseCase.invoke(any()) } returns Result.Error(RuntimeException())

        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.requestReset()
        advanceUntilIdle()

        assertEquals("Failed to send reset email", viewModel.uiState.value.error)
    }

    // ──────────────────────────────────────────────
    // Step 2: Verify OTP
    // ──────────────────────────────────────────────

    @Test
    fun `verifyOtp with short code shows error`() = runTest(testDispatcher) {
        viewModel.onOtpChange("123")
        viewModel.verifyOtp()
        advanceUntilIdle()

        assertEquals("Please enter the 6-digit code", viewModel.uiState.value.error)
    }

    @Test
    fun `verifyOtp success moves to NEW_PASSWORD step`() = runTest(testDispatcher) {
        coEvery { verifyOtpUseCase.invoke(any(), any()) } returns Result.Success(Unit)

        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onOtpChange("123456")
        viewModel.verifyOtp()
        advanceUntilIdle()

        assertEquals(ResetStep.NEW_PASSWORD, viewModel.uiState.value.step)
    }

    @Test
    fun `verifyOtp failure shows error`() = runTest(testDispatcher) {
        coEvery { verifyOtpUseCase.invoke(any(), any()) } returns Result.Error(
            RuntimeException("Invalid or expired OTP")
        )

        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onOtpChange("000000")
        viewModel.verifyOtp()
        advanceUntilIdle()

        assertEquals("Invalid or expired OTP", viewModel.uiState.value.error)
    }

    // ──────────────────────────────────────────────
    // Step 3: Set New Password
    // ──────────────────────────────────────────────

    @Test
    fun `setNewPassword with weak password shows validation error`() = runTest(testDispatcher) {
        viewModel.onNewPasswordChange("weak")
        viewModel.setNewPassword()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error != null)
    }

    @Test
    fun `setNewPassword with blank confirm shows error`() = runTest(testDispatcher) {
        viewModel.onNewPasswordChange("Password1")
        viewModel.onConfirmPasswordChange("")
        viewModel.setNewPassword()
        advanceUntilIdle()

        assertEquals("Please confirm your password", viewModel.uiState.value.error)
    }

    @Test
    fun `setNewPassword with mismatched passwords shows error`() = runTest(testDispatcher) {
        viewModel.onNewPasswordChange("Password1")
        viewModel.onConfirmPasswordChange("Password2")
        viewModel.setNewPassword()
        advanceUntilIdle()

        assertEquals("Passwords do not match", viewModel.uiState.value.error)
    }

    @Test
    fun `setNewPassword success moves to SUCCESS step`() = runTest(testDispatcher) {
        coEvery { setNewPasswordUseCase.invoke(any(), any(), any()) } returns Result.Success(Unit)

        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onOtpChange("123456")
        viewModel.onNewPasswordChange("Password1")
        viewModel.onConfirmPasswordChange("Password1")
        viewModel.setNewPassword()
        advanceUntilIdle()

        assertEquals(ResetStep.SUCCESS, viewModel.uiState.value.step)
    }

    @Test
    fun `setNewPassword failure shows error`() = runTest(testDispatcher) {
        coEvery { setNewPasswordUseCase.invoke(any(), any(), any()) } returns Result.Error(
            RuntimeException("Invalid or expired OTP")
        )

        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onOtpChange("123456")
        viewModel.onNewPasswordChange("Password1")
        viewModel.onConfirmPasswordChange("Password1")
        viewModel.setNewPassword()
        advanceUntilIdle()

        assertEquals("Invalid or expired OTP", viewModel.uiState.value.error)
    }

    // ──────────────────────────────────────────────
    // Resend OTP
    // ──────────────────────────────────────────────

    @Test
    fun `resendOtp does nothing during cooldown`() = runTest(testDispatcher) {
        coEvery { requestPasswordResetUseCase.invoke(any()) } returns Result.Success(Unit)

        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.requestReset()
        // Advance just enough for the request to complete, but not the full 60s cooldown
        advanceTimeBy(100)
        testDispatcher.scheduler.runCurrent()

        assertEquals(ResetStep.OTP, viewModel.uiState.value.step)
        assertTrue(viewModel.uiState.value.resendCooldown > 0)

        // Cooldown is active, resend should be a no-op
        viewModel.resendOtp()
        advanceTimeBy(100)
        testDispatcher.scheduler.runCurrent()

        // requestReset called only once (the initial call)
        coVerify(exactly = 1) { requestPasswordResetUseCase.invoke(any()) }
    }

    // ──────────────────────────────────────────────
    // Full Flow with Turbine
    // ──────────────────────────────────────────────

    @Test
    fun `requestReset emits loading then moves to OTP`() = runTest(testDispatcher) {
        coEvery { requestPasswordResetUseCase.invoke(any()) } returns Result.Success(Unit)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(ResetStep.EMAIL, initial.step)

            viewModel.onEmailChange("harry@hogwarts.edu")
            skipItems(1) // email change emission

            viewModel.requestReset()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val otp = awaitItem()
            assertEquals(ResetStep.OTP, otp.step)
            assertFalse(otp.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
