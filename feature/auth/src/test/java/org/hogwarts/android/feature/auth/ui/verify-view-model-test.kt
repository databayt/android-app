package org.hogwarts.android.feature.auth.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.auth.domain.usecase.VerifyOtpUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var verifyOtpUseCase: VerifyOtpUseCase
    private lateinit var viewModel: VerifyViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        verifyOtpUseCase = mockk()
        viewModel = VerifyViewModel(verifyOtpUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ──────────────────────────────────────────────
    // Initial State
    // ──────────────────────────────────────────────

    @Test
    fun `initial state has empty fields and cooldown at 60`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.otp)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertEquals(60, state.resendCooldown)
    }

    // ──────────────────────────────────────────────
    // Field Changes
    // ──────────────────────────────────────────────

    @Test
    fun `setEmail updates email`() = runTest(testDispatcher) {
        viewModel.setEmail("harry@hogwarts.edu")
        assertEquals("harry@hogwarts.edu", viewModel.uiState.value.email)
    }

    @Test
    fun `onOtpChange updates otp and clears error`() = runTest(testDispatcher) {
        viewModel.onOtpChange("1234")
        assertEquals("1234", viewModel.uiState.value.otp)
        assertNull(viewModel.uiState.value.error)
    }

    // ──────────────────────────────────────────────
    // Verify
    // ──────────────────────────────────────────────

    @Test
    fun `verify with short otp does nothing`() = runTest(testDispatcher) {
        viewModel.setEmail("harry@hogwarts.edu")
        viewModel.onOtpChange("123")
        viewModel.verify()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `verify success sets isSuccess`() = runTest(testDispatcher) {
        coEvery { verifyOtpUseCase.invoke(any(), any()) } returns Result.Success(Unit)

        viewModel.setEmail("harry@hogwarts.edu")
        viewModel.onOtpChange("1234")
        viewModel.verify()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `verify failure shows error and clears otp`() = runTest(testDispatcher) {
        coEvery { verifyOtpUseCase.invoke(any(), any()) } returns Result.Error(
            RuntimeException("Invalid or expired OTP")
        )

        viewModel.setEmail("harry@hogwarts.edu")
        viewModel.onOtpChange("1234")
        viewModel.verify()
        advanceUntilIdle()

        assertEquals("Invalid or expired OTP", viewModel.uiState.value.error)
        assertEquals("", viewModel.uiState.value.otp)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `verify error with null message falls back to default`() = runTest(testDispatcher) {
        coEvery { verifyOtpUseCase.invoke(any(), any()) } returns Result.Error(RuntimeException())

        viewModel.setEmail("harry@hogwarts.edu")
        viewModel.onOtpChange("1234")
        viewModel.verify()
        advanceUntilIdle()

        assertEquals("Verification failed", viewModel.uiState.value.error)
    }

    @Test
    fun `verify emits loading then success via Turbine`() = runTest(testDispatcher) {
        coEvery { verifyOtpUseCase.invoke(any(), any()) } returns Result.Success(Unit)

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.setEmail("harry@hogwarts.edu")
            skipItems(1) // email set

            viewModel.onOtpChange("1234")
            skipItems(1) // otp change

            viewModel.verify()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val success = awaitItem()
            assertTrue(success.isSuccess)
            assertFalse(success.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────────────────────────
    // Resend
    // ──────────────────────────────────────────────

    @Test
    fun `resendOtp does nothing during cooldown`() = runTest(testDispatcher) {
        // Cooldown starts at 60 in init, so resend should be a no-op
        viewModel.resendOtp()
        advanceUntilIdle()

        // OTP should remain unchanged
        assertEquals("", viewModel.uiState.value.otp)
    }
}
