package org.hogwarts.android.feature.attendance.ui.quick

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.feature.attendance.domain.model.MarkStatus
import org.hogwarts.android.feature.attendance.domain.model.QuickContext
import org.hogwarts.android.feature.attendance.domain.model.QuickSummary
import org.hogwarts.android.feature.attendance.domain.model.SaveOutcome
import org.hogwarts.android.feature.attendance.testing.FakeAttendanceRepository
import org.hogwarts.android.feature.attendance.testing.roster
import org.hogwarts.android.feature.attendance.testing.section
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
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class QuickAttendanceViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-09-14T08:30:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 9, 14)
    private lateinit var repository: FakeAttendanceRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAttendanceRepository().apply {
            val a = roster("sec-a", "Amal", "Bashir", "Dalia")
            context = QuickContext(
                today = today,
                isSchoolDay = true,
                sections = listOf(section("sec-a", "5A", current = true, start = "08:00"), section("sec-b", "5B")),
                rosters = mapOf("sec-a" to a),
            )
            rosters["sec-a"] = a
            rosters["sec-b"] = roster("sec-b", "Hiba", "Omar")
        }
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.loaded(): QuickAttendanceViewModel =
        QuickAttendanceViewModel(repository, clock).also { advanceUntilIdle() }

    @Test
    fun `loads sections and selects the current one with its roster`() = runTest(dispatcher) {
        val vm = loaded()
        val state = vm.uiState.value
        assertEquals("sec-a", state.selectedSectionId)
        assertEquals(listOf("Amal", "Bashir", "Dalia"), state.roster?.map { it.name })
        assertEquals(3, state.counts.present)
        assertTrue(state.showSaveBar)
    }

    @Test
    fun `tapping a student cycles present, absent, late, present`() = runTest(dispatcher) {
        val vm = loaded()
        val id = "sec-a-s1"
        vm.uiState.test {
            assertEquals(MarkStatus.Present, awaitItem().roster!!.first { it.studentId == id }.status)
            vm.cycle(id)
            awaitItem().let {
                assertEquals(MarkStatus.Absent, it.roster!!.first { s -> s.studentId == id }.status)
                assertEquals(MarkCounts(present = 2, absent = 1, late = 0), it.counts)
            }
            vm.cycle(id)
            awaitItem().let {
                assertEquals(MarkStatus.Late, it.roster!!.first { s -> s.studentId == id }.status)
                assertEquals(MarkCounts(present = 2, absent = 0, late = 1), it.counts)
            }
            vm.cycle(id)
            assertEquals(MarkStatus.Present, awaitItem().roster!!.first { it.studentId == id }.status)
        }
    }

    @Test
    fun `search narrows the visible roster, not the counts`() = runTest(dispatcher) {
        val vm = loaded()
        vm.search("da")
        val state = vm.uiState.value
        assertEquals(listOf("Dalia"), state.visibleRoster.map { it.name })
        assertEquals(3, state.counts.present)
    }

    @Test
    fun `save sends the exceptions and shows the server summary`() = runTest(dispatcher) {
        val vm = loaded()
        repository.saveGate = CompletableDeferred()
        repository.outcome = SaveOutcome.Saved(QuickSummary(total = 3, present = 1, absent = 1, late = 1, guardiansNotified = 1))
        vm.cycle("sec-a-s0") // Amal → absent
        vm.cycle("sec-a-s2"); vm.cycle("sec-a-s2") // Dalia → late

        vm.uiState.test {
            skipItems(1)
            vm.save()
            assertTrue(awaitItem().saving)
            repository.saveGate!!.complete(Unit)
            val done = awaitItem()
            assertFalse(done.saving)
            val saved = done.saved!!
            assertEquals(1, saved.guardiansNotified)
            assertEquals(listOf("Amal"), saved.absentNames)
            assertFalse(saved.queued)
            assertFalse(done.showSaveBar)
            assertEquals(3, done.sections!!.first { it.id == "sec-a" }.markedCount)
        }
        val mark = repository.submitted.single()
        assertEquals("sec-a", mark.sectionId)
        assertEquals(today, mark.date)
        assertEquals(listOf("sec-a-s0"), mark.absentStudentIds)
        assertEquals(listOf("sec-a-s2"), mark.lateStudentIds)
        assertEquals("attendance:sec-a:2026-09-14", mark.coalesceKey)
    }

    @Test
    fun `offline save is shown as saved on this device`() = runTest(dispatcher) {
        val vm = loaded()
        repository.outcome = SaveOutcome.Queued
        vm.cycle("sec-a-s1")
        vm.save()
        advanceUntilIdle()
        val saved = vm.uiState.value.saved!!
        assertTrue(saved.queued)
        assertNull(saved.guardiansNotified)
        assertEquals(listOf("Bashir"), saved.absentNames)
        assertEquals(2, saved.present)
    }

    @Test
    fun `rejected save keeps the marks and reports the failure`() = runTest(dispatcher) {
        val vm = loaded()
        repository.outcome = SaveOutcome.Rejected("FORBIDDEN")
        vm.cycle("sec-a-s1")
        vm.save()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertNull(state.saved)
        assertEquals("FORBIDDEN", state.saveError?.code)
        assertEquals(1, state.counts.absent)
        assertTrue(state.showSaveBar)
    }

    @Test
    fun `mark another returns to the roster and a new section loads its own`() = runTest(dispatcher) {
        val vm = loaded()
        vm.save()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.saved)
        vm.markAnother()
        assertNull(vm.uiState.value.saved)

        vm.selectSection("sec-b")
        assertNull(vm.uiState.value.roster)
        advanceUntilIdle()
        assertEquals(listOf("Hiba", "Omar"), vm.uiState.value.roster?.map { it.name })
    }

    @Test
    fun `a failed load offers a retry`() = runTest(dispatcher) {
        repository.contextError = java.io.IOException("offline")
        val vm = loaded()
        assertTrue(vm.uiState.value.loadFailed)
        repository.contextError = null
        vm.load()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.loadFailed)
        assertEquals(2, vm.uiState.value.sections?.size)
    }
}
