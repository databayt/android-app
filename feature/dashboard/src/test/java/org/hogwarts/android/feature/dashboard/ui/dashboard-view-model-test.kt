package org.hogwarts.android.feature.dashboard.ui

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.flowOf
import org.hogwarts.android.core.data.preferences.AppPreferences
import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.repository.DashboardRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var sessionManager: SessionManager
    private lateinit var tenantContext: TenantContext
    private lateinit var repository: DashboardRepository
    private lateinit var appPreferences: AppPreferences

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sessionManager = mockk()
        repository = mockk(relaxed = true)
        appPreferences = mockk<AppPreferences>(relaxed = true).also {
            every { it.wallpaper } returns flowOf("aurora")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTenantContext(user: CurrentUser? = null): TenantContext {
        every { sessionManager.currentUser } returns user
        every { sessionManager.isAuthenticated } returns (user != null)
        return TenantContext(sessionManager)
    }

    private fun createViewModel(tenantCtx: TenantContext = tenantContext): DashboardViewModel {
        return DashboardViewModel(tenantCtx, repository, appPreferences)
    }

    private fun fakeUser(
        role: UserRole = UserRole.STUDENT,
        id: String = "user-1",
        email: String = "student@hogwarts.edu",
        schoolId: String = "school-1",
        givenName: String? = "Harry",
        familyName: String? = "Potter"
    ) = CurrentUser(
        id = id,
        email = email,
        schoolId = schoolId,
        role = role,
        givenName = givenName,
        familyName = familyName
    )

    // ──────────────────────────────────────────────
    // Initial / Default State
    // ──────────────────────────────────────────────

    @Test
    fun `default DashboardUiState has expected defaults`() {
        val state = DashboardUiState()
        assertEquals("", state.userName)
        assertEquals(UserRole.STUDENT, state.userRole)
        assertEquals("Hogwarts Academy", state.schoolName)
        assertTrue(state.isLoading)
        assertFalse(state.isOffline)
        assertEquals(0, state.todayClasses)
        assertEquals(0, state.pendingAssignments)
        assertEquals(0f, state.attendancePercentage)
        assertEquals(0, state.unreadNotifications)
        assertEquals(0, state.upcomingExams)
        assertEquals(0, state.childrenCount)
        assertEquals(0, state.pendingAttendance)
        assertEquals(0, state.totalStudents)
    }

    // ──────────────────────────────────────────────
    // Student Dashboard
    // ──────────────────────────────────────────────

    @Test
    fun `student dashboard loads with correct role`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser(role = UserRole.STUDENT))
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserRole.STUDENT, state.userRole)
        assertFalse(state.isLoading)
    }

    @Test
    fun `student dashboard populates placeholder data`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser(role = UserRole.STUDENT))
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.todayClasses)
        assertEquals(3, state.pendingAssignments)
        assertEquals(92.5f, state.attendancePercentage)
        assertEquals(7, state.unreadNotifications)
        assertEquals(2, state.upcomingExams)
    }

    // ──────────────────────────────────────────────
    // Teacher Dashboard
    // ──────────────────────────────────────────────

    @Test
    fun `teacher dashboard loads with correct role`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser(role = UserRole.TEACHER))
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserRole.TEACHER, state.userRole)
        assertEquals(4, state.pendingAttendance)
        assertEquals(32, state.totalStudents)
        assertFalse(state.isLoading)
    }

    // ──────────────────────────────────────────────
    // Guardian Dashboard
    // ──────────────────────────────────────────────

    @Test
    fun `guardian dashboard loads with correct role`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser(role = UserRole.GUARDIAN))
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserRole.GUARDIAN, state.userRole)
        assertEquals(2, state.childrenCount)
        assertFalse(state.isLoading)
    }

    // ──────────────────────────────────────────────
    // Admin Dashboard
    // ──────────────────────────────────────────────

    @Test
    fun `admin dashboard loads with correct role`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser(role = UserRole.ADMIN))
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserRole.ADMIN, state.userRole)
        assertFalse(state.isLoading)
    }

    // ──────────────────────────────────────────────
    // Unauthenticated / Guest User
    // ──────────────────────────────────────────────

    @Test
    fun `unauthenticated user defaults to STUDENT role`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(user = null)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserRole.STUDENT, state.userRole)
        assertFalse(state.isLoading)
    }

    @Test
    fun `unauthenticated user gets Guest as userName`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(user = null)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Guest", state.userName)
    }

    // ──────────────────────────────────────────────
    // Authenticated User Name
    // ──────────────────────────────────────────────

    @Test
    fun `authenticated user gets User as userName`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser())
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("User", state.userName)
    }

    // ──────────────────────────────────────────────
    // Loading State Transition (Turbine)
    // ──────────────────────────────────────────────

    @Test
    fun `uiState transitions from loading to loaded`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser(role = UserRole.STUDENT))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Initial emission: the default DashboardUiState(isLoading = true) or the
            // already-updated state depending on timing with StandardTestDispatcher.
            // With StandardTestDispatcher, the init coroutine has not run yet.
            val first = awaitItem()
            assertTrue(first.isLoading)

            // Advance coroutines to let loadDashboard() complete
            advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(UserRole.STUDENT, loaded.userRole)
        }
    }

    // ──────────────────────────────────────────────
    // School Name (default)
    // ──────────────────────────────────────────────

    @Test
    fun `schoolName defaults to Hogwarts Academy`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser())
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Hogwarts Academy", viewModel.uiState.value.schoolName)
    }

    // ──────────────────────────────────────────────
    // Super Admin Role
    // ──────────────────────────────────────────────

    @Test
    fun `super admin dashboard loads with correct role`() = runTest(testDispatcher) {
        tenantContext = createTenantContext(fakeUser(role = UserRole.SUPER_ADMIN))
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserRole.SUPER_ADMIN, state.userRole)
        assertFalse(state.isLoading)
    }
}
