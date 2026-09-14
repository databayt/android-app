package org.hogwarts.android.feature.exams.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.exams.domain.model.Child
import org.hogwarts.android.feature.exams.domain.model.Listing
import org.hogwarts.android.feature.exams.domain.model.ProfileRecords
import org.hogwarts.android.feature.exams.domain.model.TeacherClass
import org.hogwarts.android.feature.exams.navigation.ExamsUpcoming
import org.hogwarts.android.feature.exams.navigation.QuestionBank
import org.hogwarts.android.feature.exams.testing.FakeExamsRepository
import org.hogwarts.android.feature.exams.testing.exam
import org.hogwarts.android.feature.exams.testing.grade
import org.hogwarts.android.feature.exams.ui.landing.AdminLandingViewModel
import org.hogwarts.android.feature.exams.ui.landing.GuardianLandingViewModel
import org.hogwarts.android.feature.exams.ui.landing.StudentLandingViewModel
import org.hogwarts.android.feature.exams.ui.landing.TeacherLandingViewModel
import org.hogwarts.android.feature.exams.ui.upcoming.UpcomingViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class ExamsViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-09-14T08:30:00Z"), ZoneOffset.UTC)
    private val repository = FakeExamsRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `each role lands where exams content sends it`() {
        assertEquals(ExamsLanding.Student, landingFor(UserRole.STUDENT))
        assertEquals(ExamsLanding.Guardian, landingFor(UserRole.GUARDIAN))
        assertEquals(ExamsLanding.Teacher, landingFor(UserRole.TEACHER))
        listOf(UserRole.ADMIN, UserRole.DEVELOPER, UserRole.ACCOUNTANT, UserRole.STAFF, null).forEach {
            assertEquals(ExamsLanding.Admin, landingFor(it))
        }
    }

    @Test
    fun `tabs follow getExamTabsForRole`() {
        assertEquals(10, tabsForRole(UserRole.ADMIN).size)
        assertEquals(tabsForRole(UserRole.ADMIN), tabsForRole(UserRole.TEACHER))
        assertEquals(
            listOf(ExamsTab.Overview, ExamsTab.Practice, ExamsTab.Results, ExamsTab.Quiz, ExamsTab.Mock, ExamsTab.Upcoming),
            tabsForRole(UserRole.STUDENT),
        )
        assertEquals(listOf(ExamsTab.Overview, ExamsTab.Results, ExamsTab.Upcoming), tabsForRole(UserRole.GUARDIAN))
        assertEquals(listOf(ExamsTab.Overview, ExamsTab.Results), tabsForRole(UserRole.ACCOUNTANT))
        assertEquals(listOf(ExamsTab.Overview, ExamsTab.Upcoming), tabsForRole(UserRole.STAFF))
    }

    @Test
    fun `native routes only where a mobile route answers the role`() {
        assertEquals(ExamsUpcoming, nativeRouteFor("/exams/upcoming", UserRole.STUDENT))
        assertEquals(QuestionBank, nativeRouteFor("/exams/qbank", UserRole.TEACHER))
        assertNull(nativeRouteFor("/exams/qbank", UserRole.STUDENT))
        assertNull(nativeRouteFor("/exams/new", UserRole.ADMIN))
        val opened = mutableListOf<String>()
        ExamsLinks(UserRole.STUDENT, onNavigate = {}, onOpenHref = { opened += it }).open("/exams/quiz")
        assertEquals(listOf("/exams/quiz"), opened)
    }

    @Test
    fun `admin figures come from the API, missing ones stay null`() = runTest(dispatcher) {
        val soon = exam("ex-1", "Midterm A", "2026-09-16T00:00:00Z")
        val later = exam("ex-2", "Final B", "2026-09-30T00:00:00Z")
        val done = exam("ex-3", "Old C", "2026-09-20T00:00:00Z", status = "COMPLETED")
        val overdue = exam("ex-4", "Overdue D", "2026-09-10T00:00:00Z", status = "IN_PROGRESS")
        repository.examsByQuery = { status, upcoming ->
            when {
                upcoming -> Listing(listOf(later, done, soon), 3)
                status == "COMPLETED" -> Listing(listOf(done), 4)
                status == "IN_PROGRESS" -> Listing(listOf(overdue), 1)
                else -> Listing(listOf(soon), 40)
            }
        }
        repository.questionTotal = 120
        repository.students = null // 403 for this role
        val vm = AdminLandingViewModel(repository, clock)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.loading)
        assertEquals("ex-1", state.nextExam?.id)
        assertEquals(40, state.total)
        assertEquals(2, state.upcoming)
        assertEquals(120, state.questionBank)
        assertNull(state.students)
        assertEquals(4, state.completed)
        assertEquals(1, state.pendingMarking)
        assertEquals(10, state.completionRate)
        assertEquals(80, state.markingProgress)
        assertFalse(state.loadFailed)
    }

    @Test
    fun `admin load failure is reported and retried`() = runTest(dispatcher) {
        repository.failAll = java.io.IOException("offline")
        val vm = AdminLandingViewModel(repository, clock)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.loadFailed)
        repository.failAll = null
        repository.examsByQuery = { _, _ -> Listing(emptyList(), 0) }
        vm.load()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.loadFailed)
        assertEquals(0, vm.uiState.value.total)
        assertEquals(100, vm.uiState.value.markingProgress)
    }

    @Test
    fun `teacher without a record sees the no-record card, with one sees their students`() = runTest(dispatcher) {
        val vm = TeacherLandingViewModel(repository)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.noRecord)

        repository.profile = ProfileRecords(studentId = null, teacherId = "t-1")
        repository.teacherClasses = listOf(TeacherClass("sec-1", 20), TeacherClass("sec-1", 20), TeacherClass("sec-2", 15))
        vm.load()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.noRecord)
        assertEquals(2, vm.uiState.value.classes)
        assertEquals(35, vm.uiState.value.students)
    }

    @Test
    fun `student lists open upcoming exams and their graded results`() = runTest(dispatcher) {
        repository.profile = ProfileRecords(studentId = "s-1", teacherId = null)
        repository.examsByQuery = { _, _ ->
            Listing(listOf(exam("ex-2", "Later", "2026-09-20T00:00:00Z"), exam("ex-1", "Sooner", "2026-09-15T00:00:00Z"), exam("ex-3", "Cancelled", "2026-09-15T00:00:00Z", status = "CANCELLED")), 3)
        }
        repository.studentGrades = listOf(grade("g-1", "Quiz one", 92.0, "2026-09-01T10:00:00Z"))
        val vm = StudentLandingViewModel(repository, clock)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(listOf("ex-1", "ex-2"), state.upcoming.map { it.id })
        assertEquals(1, state.results.size)
        assertFalse(state.noRecord)
    }

    @Test
    fun `guardian merges the children's results newest first and averages each child`() = runTest(dispatcher) {
        repository.children = listOf(Child("c-1", "Child One"), Child("c-2", "Child Two"))
        repository.childGrades = mapOf(
            "c-1" to listOf(grade("g-1", "Test", 90.0, "2026-09-02T10:00:00Z"), grade("g-2", "Test", 70.0, "2026-08-02T10:00:00Z")),
            "c-2" to listOf(grade("g-3", "Test", 40.0, "2026-09-05T10:00:00Z")),
        )
        val vm = GuardianLandingViewModel(repository, clock)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(listOf("g-3", "g-1", "g-2"), state.results.map { it.row.id })
        assertEquals(80, state.averageOf("c-1"))
        assertEquals(40, state.averageOf("c-2"))
        assertFalse(state.noChildren)

        repository.children = emptyList()
        vm.load()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.noChildren)
    }

    @Test
    fun `upcoming keeps open exams, soonest first`() = runTest(dispatcher) {
        repository.examsByQuery = { _, upcoming ->
            assertTrue(upcoming)
            Listing(listOf(exam("b", "B", "2026-09-22T00:00:00Z"), exam("a", "A", "2026-09-14T00:00:00Z", status = "IN_PROGRESS"), exam("c", "C", "2026-09-15T00:00:00Z", status = "COMPLETED")), 3)
        }
        val vm = UpcomingViewModel(repository, clock)
        advanceUntilIdle()
        assertEquals(listOf("a", "b"), vm.uiState.value.exams.map { it.id })
    }
}
