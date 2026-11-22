package org.hogwarts.android.feature.dashboard.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.time.SchoolZone
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.dashboard.data.repository.DashboardResult
import org.hogwarts.android.feature.dashboard.data.repository.RefreshFailure
import org.hogwarts.android.feature.dashboard.data.repository.SectionsResult
import org.hogwarts.android.feature.dashboard.testing.FakeConnectivityObserver
import org.hogwarts.android.feature.dashboard.testing.FakeDashboardRepository
import org.hogwarts.android.feature.dashboard.testing.FakeSectionsRepository
import org.hogwarts.android.feature.dashboard.testing.Fixtures
import org.hogwarts.android.feature.dashboard.testing.tenant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        role: UserRole = UserRole.TEACHER,
        dashboard: DashboardResult = DashboardResult.Fresh(Fixtures.teacher),
        sections: SectionsResult = SectionsResult.Unavailable,
        repository: FakeDashboardRepository = FakeDashboardRepository(dashboard),
        connectivity: FakeConnectivityObserver = FakeConnectivityObserver(),
    ) = DashboardViewModel(
        repository,
        FakeSectionsRepository(sections),
        tenant(role),
        connectivity,
        SchoolZone("Africa/Khartoum"),
    )

    @Test
    fun `a fresh read carries the server's role and clears the offline flag`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(UserRole.TEACHER, state.role)
        assertFalse(state.isLoading)
        assertFalse(state.isOffline)
        assertNull(state.error)
    }

    @Test
    fun `a connectivity failure is flagged offline and keeps the session role`() = runTest(dispatcher) {
        val vm = viewModel(
            role = UserRole.STUDENT,
            dashboard = DashboardResult.Cached(Fixtures.student, RefreshFailure.Connectivity),
        )
        runCurrent()
        assertTrue(vm.uiState.value.isOffline)
        assertEquals(RefreshNotice.Offline, vm.uiState.value.notice)
        assertEquals(UserRole.STUDENT, vm.uiState.value.role)
        assertEquals(Fixtures.student, vm.uiState.value.data)
    }

    @Test
    fun `a server error on an online phone keeps the content and is not offline`() = runTest(dispatcher) {
        val vm = viewModel(dashboard = DashboardResult.Cached(Fixtures.student, RefreshFailure.Server))
        runCurrent()
        assertFalse(vm.uiState.value.isOffline)
        assertEquals(RefreshNotice.CouldNotRefresh, vm.uiState.value.notice)
        assertEquals(Fixtures.student, vm.uiState.value.data)
    }

    @Test
    fun `a timeout on an online phone is only a neutral note`() = runTest(dispatcher) {
        val vm = viewModel(dashboard = DashboardResult.Cached(Fixtures.student, RefreshFailure.Timeout))
        runCurrent()
        assertEquals(RefreshNotice.CouldNotRefresh, vm.uiState.value.notice)
    }

    @Test
    fun `a failure with no validated network is offline whatever the cause`() = runTest(dispatcher) {
        val vm = viewModel(
            dashboard = DashboardResult.Cached(Fixtures.student, RefreshFailure.Timeout),
            connectivity = FakeConnectivityObserver(online = false),
        )
        runCurrent()
        assertEquals(RefreshNotice.Offline, vm.uiState.value.notice)
    }

    @Test
    fun `the notice disappears as soon as a refresh succeeds`() = runTest(dispatcher) {
        val repo = FakeDashboardRepository(DashboardResult.Cached(Fixtures.student, RefreshFailure.Connectivity))
        val vm = viewModel(repository = repo)
        runCurrent()
        assertEquals(RefreshNotice.Offline, vm.uiState.value.notice)
        repo.result = DashboardResult.Fresh(Fixtures.student)
        vm.refresh()
        runCurrent()
        assertEquals(RefreshNotice.None, vm.uiState.value.notice)
        assertFalse(vm.uiState.value.isOffline)
    }

    @Test
    fun `a failed refresh retries by itself after 5, 15 and 45 seconds, then stops`() = runTest(dispatcher) {
        val repo = FakeDashboardRepository(DashboardResult.Cached(Fixtures.student, RefreshFailure.Server))
        viewModel(repository = repo)
        runCurrent()
        assertEquals(1, repo.refreshes)
        advanceTimeBy(4_999); runCurrent()
        assertEquals(1, repo.refreshes)
        advanceTimeBy(1); runCurrent()
        assertEquals(2, repo.refreshes)
        advanceTimeBy(15_000); runCurrent()
        assertEquals(3, repo.refreshes)
        advanceTimeBy(45_000); runCurrent()
        assertEquals(4, repo.refreshes)
        advanceTimeBy(300_000); runCurrent()
        assertEquals(4, repo.refreshes)
    }

    @Test
    fun `a retry that succeeds clears the notice and ends the back-off`() = runTest(dispatcher) {
        val repo = FakeDashboardRepository(DashboardResult.Cached(Fixtures.student, RefreshFailure.Server))
        val vm = viewModel(repository = repo)
        runCurrent()
        repo.result = DashboardResult.Fresh(Fixtures.student)
        advanceTimeBy(5_000); runCurrent()
        assertEquals(RefreshNotice.None, vm.uiState.value.notice)
        advanceTimeBy(300_000); runCurrent()
        assertEquals(2, repo.refreshes)
    }

    @Test
    fun `resuming re-fetches, but not twice in a row`() = runTest(dispatcher) {
        var now = 0L
        val repo = FakeDashboardRepository(DashboardResult.Fresh(Fixtures.teacher))
        val vm = viewModel(repository = repo)
        vm.clock = { now }
        runCurrent()
        assertEquals(1, repo.refreshes)
        now = 10_000; vm.onResume(); runCurrent()
        assertEquals(1, repo.refreshes)
        now = 31_000; vm.onResume(); runCurrent()
        assertEquals(2, repo.refreshes)
    }

    @Test
    fun `the network coming back re-fetches`() = runTest(dispatcher) {
        val connectivity = FakeConnectivityObserver(online = false)
        val repo = FakeDashboardRepository(DashboardResult.Fresh(Fixtures.teacher))
        viewModel(repository = repo, connectivity = connectivity)
        runCurrent()
        assertEquals(1, repo.refreshes)
        connectivity.onlineFlow.value = true
        runCurrent()
        assertEquals(2, repo.refreshes)
    }

    @Test
    fun `a missing sections route leaves both sections off the page`() = runTest(dispatcher) {
        val vm = viewModel(sections = SectionsResult.Unavailable)
        advanceUntilIdle()
        assertNull(vm.uiState.value.sections)
        // The dashboard itself still arrived: the tables are a second read.
        assertEquals(Fixtures.teacher, vm.uiState.value.data)
    }

    @Test
    fun `sections that answer are kept`() = runTest(dispatcher) {
        val vm = viewModel(role = UserRole.ADMIN, dashboard = DashboardResult.Fresh(Fixtures.admin), sections = SectionsResult.Ready(Fixtures.adminSections))
        advanceUntilIdle()
        val sections = vm.uiState.value.sections
        assertEquals(3, sections?.resourceUsage?.size)
        assertEquals(3, sections?.invoices?.size)
    }

    @Test
    fun `an answer that is empty is still an answer`() = runTest(dispatcher) {
        // Empty is not the same as absent: the tables render and show their own
        // "no resources" / "no invoices yet" rows, where a 404 hides them both.
        val vm = viewModel(sections = SectionsResult.Ready(Fixtures.emptySections))
        advanceUntilIdle()
        val sections = vm.uiState.value.sections
        assertNotNull(sections)
        assertEquals(emptyList<Any>(), sections?.resourceUsage)
        assertEquals(emptyList<Any>(), sections?.invoices)
    }

    @Test
    fun `acknowledging a next action drops it from the banner`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        val first = vm.uiState.value.nextActions.first()
        vm.acknowledge(first)
        assertFalse(vm.uiState.value.nextActions.contains(first))
        assertEquals(1, vm.uiState.value.nextActions.size)
    }

    @Test
    fun `a failed read reports the error and shows nothing`() = runTest(dispatcher) {
        val vm = viewModel(dashboard = DashboardResult.Failed("HTTP 500", RefreshFailure.Server))
        advanceUntilIdle()
        assertEquals("HTTP 500", vm.uiState.value.error)
        assertNull(vm.uiState.value.data)
    }

    @Test
    fun `the resolved day is today when the server says so, whatever the device weekday`() {
        assertTrue(Fixtures.teacherDay.isTodayOr(weekday = 3))
        assertFalse(Fixtures.studentDay.isTodayOr(weekday = 1))
    }

    @Test
    fun `without is_today the resolved day is today only when it IS the device weekday`() {
        val day = Fixtures.teacherDay.copy(isToday = null, dayOfWeek = 2)
        assertTrue(day.isTodayOr(weekday = 2))
        assertFalse(day.isTodayOr(weekday = 5))
    }
}
