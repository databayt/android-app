package org.hogwarts.android.feature.timetable.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.timetable.data.remote.dto.TimetableBundle
import org.hogwarts.android.feature.timetable.data.repository.TimetableResult
import org.hogwarts.android.feature.timetable.data.repository.TimetableScope
import org.hogwarts.android.feature.timetable.domain.model.RangeMode
import org.hogwarts.android.feature.timetable.testing.FakeTimetableRepository
import org.hogwarts.android.feature.timetable.testing.Fixtures
import org.hogwarts.android.feature.timetable.testing.tenant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    // Monday 2026-09-14, 08:30 on the device.
    private val clock = Clock.fixed(Instant.parse("2026-09-14T08:30:00Z"), ZoneOffset.UTC)
    private val repository = FakeTimetableRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(role: UserRole) = TimetableViewModel(repository, tenant(role), clock)

    @Test
    fun `each role gets the web's view`() {
        assertEquals(TimetableSurface.Student, surfaceFor(UserRole.STUDENT))
        assertEquals(TimetableSurface.Student, surfaceFor(UserRole.USER))
        assertEquals(TimetableSurface.Teacher, surfaceFor(UserRole.TEACHER))
        assertEquals(TimetableSurface.Guardian, surfaceFor(UserRole.GUARDIAN))
        listOf(UserRole.ADMIN, UserRole.DEVELOPER, UserRole.ACCOUNTANT, UserRole.STAFF).forEach {
            assertEquals(TimetableSurface.Admin, surfaceFor(it))
        }
    }

    @Test
    fun `tabs follow the layout's permissions`() {
        val adminTabs = listOf(TimetableTab.All, TimetableTab.Analytics, TimetableTab.Generate, TimetableTab.Conflicts, TimetableTab.Settings)
        assertEquals(adminTabs, tabsFor(UserRole.ADMIN))
        assertEquals(listOf(TimetableTab.Analytics, TimetableTab.Today, TimetableTab.Full), tabsFor(UserRole.TEACHER))
        assertEquals(listOf(TimetableTab.Today, TimetableTab.Full), tabsFor(UserRole.GUARDIAN))
        assertEquals(listOf(TimetableTab.Today, TimetableTab.Full), tabsFor(UserRole.STAFF))
        assertEquals(emptyList<TimetableTab>(), tabsFor(UserRole.STUDENT))
        // Every admin sub-page opens on the web.
        adminTabs.drop(1).forEach { assertNotNull(it.href) }
        // The page opens on its first native tab, whichever the role has.
        assertEquals(TimetableTab.All, TimetableUiState(UserRole.ADMIN).tab)
        assertEquals(TimetableTab.Today, TimetableUiState(UserRole.STAFF).tab)
        assertEquals(TimetableTab.Today, TimetableUiState(UserRole.ACCOUNTANT).tab)
    }

    @Test
    fun `a student's week loads with the day's periods and the phone opens on the day`() = runTest(dispatcher) {
        repository.mine = TimetableResult.Fresh(Fixtures.studentWeek(arabic = false, closure = "Founders Day"))
        val vm = viewModel(UserRole.STUDENT)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, repository.mineCalls)
        assertEquals(4, state.week!!.periods.size)
        assertEquals("Founders Day", state.week!!.closure?.title)
        assertEquals(RangeMode.Day, state.range)
        assertEquals(8 * 60 + 30, state.nowMinutes)
        vm.pickRange(RangeMode.Week)
        assertEquals(RangeMode.Week, vm.uiState.value.range)
    }

    @Test
    fun `the cache shows first and stays when the network fails`() = runTest(dispatcher) {
        repository.cache[TimetableScope.Mine] = Fixtures.teacherWeek(arabic = false)
        repository.mine = TimetableResult.Cached(Fixtures.teacherWeek(arabic = false))
        val vm = viewModel(UserRole.TEACHER)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isOffline)
        assertNotNull(vm.uiState.value.week)
    }

    @Test
    fun `nothing loaded and nothing cached is a failure the reader can retry`() = runTest(dispatcher) {
        repository.mine = TimetableResult.Failed("offline")
        val vm = viewModel(UserRole.STUDENT)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.failed)
        repository.mine = TimetableResult.Fresh(Fixtures.studentWeek(arabic = false))
        vm.refresh()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.failed)
        assertEquals(2, repository.mineCalls)
    }

    @Test
    fun `a guardian switches between children`() = runTest(dispatcher) {
        val children = Fixtures.children(arabic = false)
        repository.guardian = { id ->
            TimetableResult.Fresh(Fixtures.studentWeek(arabic = false).copy(today = null, children = children, childId = id ?: children.first().id))
        }
        val vm = viewModel(UserRole.GUARDIAN)
        advanceUntilIdle()
        assertEquals("child-1", vm.uiState.value.selectedChildId)
        assertEquals("Layan Haddad", vm.uiState.value.selectedChild?.name)
        vm.selectChild("child-2")
        advanceUntilIdle()
        assertEquals(listOf(null, "child-2"), repository.guardianCalls)
        assertEquals("child-2", vm.uiState.value.selectedChildId)
        assertNotNull(vm.uiState.value.week)
    }

    @Test
    fun `an admin fetches nothing and web tabs never become the page's tab`() = runTest(dispatcher) {
        val vm = viewModel(UserRole.ADMIN)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(0, repository.mineCalls)
        assertEquals(TimetableTab.All, vm.uiState.value.tab)
        vm.selectTab(TimetableTab.Generate)
        assertEquals(TimetableTab.All, vm.uiState.value.tab)
    }

    @Test
    fun `teacher tabs, filters and the inspected slot`() = runTest(dispatcher) {
        repository.mine = TimetableResult.Fresh(Fixtures.teacherWeek(arabic = false))
        val vm = viewModel(UserRole.TEACHER)
        advanceUntilIdle()
        assertEquals(TimetableTab.Today, vm.uiState.value.tab)
        vm.selectTab(TimetableTab.Full)
        vm.filterClassroom("Grade 6 B")
        val slotId = vm.uiState.value.week!!.slots.first().id
        vm.inspect(slotId)
        assertEquals(TimetableTab.Full, vm.uiState.value.tab)
        assertEquals("Grade 6 B", vm.uiState.value.classroomFilter)
        assertEquals(slotId, vm.uiState.value.inspectedSlot?.id)
        vm.inspect(null)
        assertNull(vm.uiState.value.inspectedSlot)
    }

    @Test
    fun `an empty bundle still lays out`() = runTest(dispatcher) {
        repository.mine = TimetableResult.Fresh(TimetableBundle())
        val vm = viewModel(UserRole.STUDENT)
        advanceUntilIdle()
        assertEquals(emptyList<Int>(), vm.uiState.value.week!!.workingDays)
    }
}
