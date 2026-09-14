package org.hogwarts.android.feature.attendance.ui.overview

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStats
import org.hogwarts.android.feature.attendance.domain.model.ChildAttendance
import org.hogwarts.android.feature.attendance.domain.model.StudentAttendance
import org.hogwarts.android.feature.attendance.domain.model.TodayTotals
import org.hogwarts.android.feature.attendance.testing.FakeAttendanceRepository
import org.hogwarts.android.feature.attendance.testing.tenant
import org.hogwarts.android.feature.attendance.ui.AttendanceLanding
import org.hogwarts.android.feature.attendance.ui.AttendanceTab
import org.hogwarts.android.feature.attendance.ui.AttendanceUiState
import org.hogwarts.android.feature.attendance.ui.mine.MineUiState
import org.hogwarts.android.feature.attendance.ui.mine.MineViewModel
import org.hogwarts.android.feature.attendance.ui.tabsForRole
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class LandingViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-09-14T08:30:00Z"), ZoneOffset.UTC)
    private val repository = FakeAttendanceRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `each role lands where the web sends it`() {
        assertEquals(AttendanceLanding.Quick, AttendanceUiState(UserRole.TEACHER).landing)
        listOf(UserRole.ADMIN, UserRole.STAFF, UserRole.DEVELOPER).forEach {
            assertEquals(AttendanceLanding.StaffOverview, AttendanceUiState(it).landing)
        }
        listOf(UserRole.STUDENT, UserRole.GUARDIAN, UserRole.ACCOUNTANT, null).forEach {
            assertEquals(AttendanceLanding.Mine, AttendanceUiState(it).landing)
        }
    }

    @Test
    fun `tabs follow permissions, keeping only native screens`() {
        val staff = listOf(AttendanceTab.Overview, AttendanceTab.Interventions, AttendanceTab.Analytics)
        assertEquals(staff + AttendanceTab.Settings, tabsForRole(UserRole.ADMIN))
        assertEquals(staff, tabsForRole(UserRole.TEACHER))
        assertEquals(staff, tabsForRole(UserRole.STAFF))
        assertEquals(listOf(AttendanceTab.Overview), tabsForRole(UserRole.GUARDIAN))
        assertEquals(emptyList<AttendanceTab>(), tabsForRole(UserRole.ACCOUNTANT))
    }

    @Test
    fun `staff overview loads today's totals and retries after a failure`() = runTest(dispatcher) {
        repository.totalsError = java.io.IOException("offline")
        val vm = StaffOverviewViewModel(repository, tenant(UserRole.ADMIN), clock)
        vm.uiState.test {
            assertTrue(awaitItem().loading)
            awaitItem().let {
                assertTrue(it.loadFailed)
                assertTrue(it.isAdmin)
            }
            repository.totalsError = null
            repository.totals = TodayTotals(marked = 40, present = 36, absent = 2, late = 2)
            vm.load()
            assertTrue(awaitItem().loading)
            val loaded = awaitItem()
            assertFalse(loaded.loadFailed)
            assertEquals(95, loaded.totals?.rate) // (36 + 2) / 40, late counts as attended
        }
    }

    @Test
    fun `guardian with children wins over the student path`() = runTest(dispatcher) {
        val stats = AttendanceStats(totalDays = 20, present = 18, absent = 1, late = 1, excused = 0)
        repository.children = listOf(ChildAttendance("c1", "Sara", "Grade 5 · A", stats, emptyList()))
        repository.student = StudentAttendance(stats, emptyList())
        val vm = MineViewModel(repository)
        advanceUntilIdle()
        val state = vm.uiState.value as MineUiState.Guardian
        assertEquals("Sara", state.children.single().name)
        assertEquals(95, state.children.single().stats.rate)
    }

    @Test
    fun `no guardian link falls back to the student's own record, else unavailable`() = runTest(dispatcher) {
        repository.student = StudentAttendance(AttendanceStats(10, 9, 1, 0, 0), emptyList())
        val vm = MineViewModel(repository)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is MineUiState.Student)

        repository.student = null
        repository.guardianError = java.io.IOException("offline")
        vm.load()
        advanceUntilIdle()
        assertEquals(MineUiState.Unavailable(failed = true), vm.uiState.value)
    }
}
