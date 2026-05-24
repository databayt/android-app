package org.hogwarts.android.feature.auth.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.auth.domain.model.AuthResult
import org.hogwarts.android.feature.auth.domain.usecase.SignUpUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var viewModel: SignUpViewModel

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
        signUpUseCase = mockk()
        viewModel = SignUpViewModel(signUpUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ──────────────────────────────────────────────
    // Initial State
    // ──────────────────────────────────────────────

    @Test
    fun `initial state has empty fields and no errors`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        assertEquals("", state.firstName)
        assertEquals("", state.lastName)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.schoolId)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertTrue(state.fieldErrors.isEmpty())
        assertNull(state.generalError)
    }

    // ──────────────────────────────────────────────
    // Field Changes
    // ──────────────────────────────────────────────

    @Test
    fun `onFirstNameChange updates firstName`() = runTest(testDispatcher) {
        viewModel.onFirstNameChange("Harry")
        assertEquals("Harry", viewModel.uiState.value.firstName)
    }

    @Test
    fun `onLastNameChange updates lastName`() = runTest(testDispatcher) {
        viewModel.onLastNameChange("Potter")
        assertEquals("Potter", viewModel.uiState.value.lastName)
    }

    @Test
    fun `onEmailChange updates email`() = runTest(testDispatcher) {
        viewModel.onEmailChange("harry@hogwarts.edu")
        assertEquals("harry@hogwarts.edu", viewModel.uiState.value.email)
    }

    @Test
    fun `onPasswordChange updates password`() = runTest(testDispatcher) {
        viewModel.onPasswordChange("Password1")
        assertEquals("Password1", viewModel.uiState.value.password)
    }

    @Test
    fun `onSchoolIdChange updates schoolId`() = runTest(testDispatcher) {
        viewModel.onSchoolIdChange("school-1")
        assertEquals("school-1", viewModel.uiState.value.schoolId)
    }

    // ──────────────────────────────────────────────
    // Validation
    // ──────────────────────────────────────────────

    @Test
    fun `signUp with blank firstName shows field error`() = runTest(testDispatcher) {
        viewModel.onLastNameChange("Potter")
        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onPasswordChange("Password1")
        viewModel.onSchoolIdChange("school-1")
        viewModel.signUp()
        advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertTrue(errors.containsKey("firstName"))
        assertEquals("First name is required", errors["firstName"])
    }

    @Test
    fun `signUp with blank lastName shows field error`() = runTest(testDispatcher) {
        viewModel.onFirstNameChange("Harry")
        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onPasswordChange("Password1")
        viewModel.onSchoolIdChange("school-1")
        viewModel.signUp()
        advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertTrue(errors.containsKey("lastName"))
        assertEquals("Last name is required", errors["lastName"])
    }

    @Test
    fun `signUp with invalid email shows field error`() = runTest(testDispatcher) {
        viewModel.onFirstNameChange("Harry")
        viewModel.onLastNameChange("Potter")
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("Password1")
        viewModel.onSchoolIdChange("school-1")
        viewModel.signUp()
        advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertTrue(errors.containsKey("email"))
    }

    @Test
    fun `signUp with weak password shows field error`() = runTest(testDispatcher) {
        viewModel.onFirstNameChange("Harry")
        viewModel.onLastNameChange("Potter")
        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onPasswordChange("weak")
        viewModel.onSchoolIdChange("school-1")
        viewModel.signUp()
        advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertTrue(errors.containsKey("password"))
    }

    @Test
    fun `signUp with blank schoolId shows field error`() = runTest(testDispatcher) {
        viewModel.onFirstNameChange("Harry")
        viewModel.onLastNameChange("Potter")
        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onPasswordChange("Password1")
        viewModel.signUp()
        advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertTrue(errors.containsKey("schoolId"))
        assertEquals("School is required", errors["schoolId"])
    }

    @Test
    fun `signUp with multiple blank fields shows all errors`() = runTest(testDispatcher) {
        viewModel.signUp()
        advanceUntilIdle()

        val errors = viewModel.uiState.value.fieldErrors
        assertTrue(errors.containsKey("firstName"))
        assertTrue(errors.containsKey("lastName"))
        assertTrue(errors.containsKey("email"))
        assertTrue(errors.containsKey("password"))
        assertTrue(errors.containsKey("schoolId"))
    }

    @Test
    fun `validation failure does not call use case`() = runTest(testDispatcher) {
        viewModel.signUp()
        advanceUntilIdle()

        coVerify(exactly = 0) { signUpUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    // ──────────────────────────────────────────────
    // Error Clearing
    // ──────────────────────────────────────────────

    @Test
    fun `onFirstNameChange clears firstName error and generalError`() = runTest(testDispatcher) {
        viewModel.signUp()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.fieldErrors.containsKey("firstName"))

        viewModel.onFirstNameChange("H")
        assertFalse(viewModel.uiState.value.fieldErrors.containsKey("firstName"))
        assertNull(viewModel.uiState.value.generalError)
    }

    @Test
    fun `onLastNameChange clears lastName error`() = runTest(testDispatcher) {
        viewModel.signUp()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.fieldErrors.containsKey("lastName"))

        viewModel.onLastNameChange("P")
        assertFalse(viewModel.uiState.value.fieldErrors.containsKey("lastName"))
    }

    @Test
    fun `onEmailChange clears email error`() = runTest(testDispatcher) {
        viewModel.signUp()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.fieldErrors.containsKey("email"))

        viewModel.onEmailChange("h")
        assertFalse(viewModel.uiState.value.fieldErrors.containsKey("email"))
    }

    @Test
    fun `onPasswordChange clears password error`() = runTest(testDispatcher) {
        viewModel.signUp()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.fieldErrors.containsKey("password"))

        viewModel.onPasswordChange("P")
        assertFalse(viewModel.uiState.value.fieldErrors.containsKey("password"))
    }

    @Test
    fun `onSchoolIdChange clears schoolId error`() = runTest(testDispatcher) {
        viewModel.signUp()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.fieldErrors.containsKey("schoolId"))

        viewModel.onSchoolIdChange("s")
        assertFalse(viewModel.uiState.value.fieldErrors.containsKey("schoolId"))
    }

    // ──────────────────────────────────────────────
    // Successful Sign Up
    // ──────────────────────────────────────────────

    @Test
    fun `signUp success emits Loading then Success`() = runTest(testDispatcher) {
        coEvery { signUpUseCase.invoke(any(), any(), any(), any(), any()) } returns Result.Success(fakeAuthResult)

        viewModel.uiState.test {
            assertEquals(SignUpUiState(), awaitItem())

            fillValidForm()
            skipItems(5) // 5 field change emissions

            viewModel.signUp()

            // Loading state
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            assertNull(loading.generalError)

            // Success state
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertTrue(success.isSuccess)
        }
    }

    @Test
    fun `signUp success passes correct params to use case`() = runTest(testDispatcher) {
        coEvery { signUpUseCase.invoke(any(), any(), any(), any(), any()) } returns Result.Success(fakeAuthResult)

        fillValidForm()
        viewModel.signUp()
        advanceUntilIdle()

        coVerify {
            signUpUseCase.invoke("Harry", "Potter", "harry@hogwarts.edu", "Password1", "school-1")
        }
    }

    // ──────────────────────────────────────────────
    // Failed Sign Up
    // ──────────────────────────────────────────────

    @Test
    fun `signUp error emits Loading then generalError`() = runTest(testDispatcher) {
        coEvery { signUpUseCase.invoke(any(), any(), any(), any(), any()) } returns Result.Error(
            RuntimeException("Account already exists")
        )

        viewModel.uiState.test {
            assertEquals(SignUpUiState(), awaitItem())

            fillValidForm()
            skipItems(5) // 5 field change emissions

            viewModel.signUp()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val error = awaitItem()
            assertFalse(error.isLoading)
            assertEquals("Account already exists", error.generalError)
        }
    }

    @Test
    fun `signUp error with null message falls back to default`() = runTest(testDispatcher) {
        coEvery { signUpUseCase.invoke(any(), any(), any(), any(), any()) } returns Result.Error(
            RuntimeException()
        )

        fillValidForm()
        viewModel.signUp()
        advanceUntilIdle()

        assertEquals("Registration failed", viewModel.uiState.value.generalError)
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private fun fillValidForm() {
        viewModel.onFirstNameChange("Harry")
        viewModel.onLastNameChange("Potter")
        viewModel.onEmailChange("harry@hogwarts.edu")
        viewModel.onPasswordChange("Password1")
        viewModel.onSchoolIdChange("school-1")
    }
}
