package org.hogwarts.android.feature.grades.ui

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.grades.domain.model.AssessmentType
import org.hogwarts.android.feature.grades.domain.model.GradeRecord
import org.hogwarts.android.feature.grades.domain.usecase.GetGradesUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GradesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getGradesUseCase: GetGradesUseCase
    private lateinit var tenantContext: TenantContext
    private lateinit var sessionManager: SessionManager

    private val testUser = CurrentUser(
        id = "student-1",
        email = "student@hogwarts.edu",
        schoolId = "school-1",
        role = UserRole.STUDENT,
        givenName = "Hermione",
        familyName = "Granger"
    )

    private val sampleGrades = listOf(
        GradeRecord(
            id = "grade-1",
            studentId = "student-1",
            subjectId = "subj-1",
            subjectName = "Defense Against the Dark Arts",
            assessmentType = AssessmentType.EXAM,
            assessmentName = "Patronus Exam",
            score = 95f,
            maxScore = 100f,
            grade = "A",
            date = LocalDate.of(2025, 1, 20),
            term = "Fall 2025"
        ),
        GradeRecord(
            id = "grade-2",
            studentId = "student-1",
            subjectId = "subj-2",
            subjectName = "Potions",
            assessmentType = AssessmentType.QUIZ,
            assessmentName = "Veritaserum Quiz",
            score = 88f,
            maxScore = 100f,
            grade = "B+",
            date = LocalDate.of(2025, 1, 18),
            term = "Fall 2025"
        ),
        GradeRecord(
            id = "grade-3",
            studentId = "student-1",
            subjectId = "subj-3",
            subjectName = "Charms",
            assessmentType = AssessmentType.ASSIGNMENT,
            assessmentName = "Levitation Essay",
            score = 45f,
            maxScore = 50f,
            grade = "A-",
            date = LocalDate.of(2025, 1, 15),
            term = "Fall 2025"
        ),
        GradeRecord(
            id = "grade-4",
            studentId = "student-1",
            subjectId = "subj-1",
            subjectName = "Defense Against the Dark Arts",
            assessmentType = AssessmentType.MIDTERM,
            assessmentName = "Midterm Practical",
            score = 92f,
            maxScore = 100f,
            grade = "A-",
            date = LocalDate.of(2025, 2, 1),
            term = "Fall 2025"
        ),
        GradeRecord(
            id = "grade-5",
            studentId = "student-1",
            subjectId = "subj-2",
            subjectName = "Potions",
            assessmentType = AssessmentType.FINAL,
            assessmentName = "Final Practical Exam",
            score = 97f,
            maxScore = 100f,
            grade = "A+",
            date = LocalDate.of(2025, 3, 15),
            term = "Fall 2025"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getGradesUseCase = mockk()
        sessionManager = mockk()
        every { sessionManager.currentUser } returns testUser
        every { sessionManager.isAuthenticated } returns true
        tenantContext = TenantContext(sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Initial loading state ---

    @Test
    fun `initial state emits Loading then Success with grade records`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flow {
            emit(Resource.Loading(null))
            emit(Resource.Success(sampleGrades))
        }

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)

        viewModel.uiState.test {
            // Default initial state (isLoading=true, records=empty)
            // Note: Loading(null) is de-duplicated by StateFlow since it produces the same state
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            assertTrue(initial.records.isEmpty())

            // After Success emission from use case
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals(5, success.records.size)
            assertNull(success.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Error state ---

    @Test
    fun `error state when repository returns error`() = runTest {
        val errorMessage = "Failed to fetch grades"
        every { getGradesUseCase(studentId = "student-1") } returns flow {
            emit(Resource.Loading(null))
            emit(Resource.Error<List<GradeRecord>>(Exception(errorMessage), emptyList()))
        }

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)

        viewModel.uiState.test {
            // Default initial state (Loading(null) de-duplicated by StateFlow)
            awaitItem()

            // Error state
            val error = awaitItem()
            assertFalse(error.isLoading)
            assertEquals(errorMessage, error.error)
            assertTrue(error.records.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state preserves cached grade data`() = runTest {
        val cachedGrades = sampleGrades.take(2)
        every { getGradesUseCase(studentId = "student-1") } returns flow {
            emit(Resource.Loading(cachedGrades))
            emit(Resource.Error(Exception("Connection lost"), cachedGrades))
        }

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)

        viewModel.uiState.test {
            // Default initial state
            awaitItem()

            // Loading with cached data
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            assertEquals(2, loading.records.size)

            // Error with cached data preserved
            val error = awaitItem()
            assertFalse(error.isLoading)
            assertEquals("Connection lost", error.error)
            assertEquals(2, error.records.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Filter change ---

    @Test
    fun `setFilter updates selectedFilter in ui state`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.EXAM)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(GradesFilter.EXAM, state.selectedFilter)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFilteredRecords returns only EXAM records when filter is EXAM`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.EXAM)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.assessmentType == AssessmentType.EXAM })
        assertEquals("Patronus Exam", filtered.first().assessmentName)
    }

    @Test
    fun `getFilteredRecords returns only QUIZ records when filter is QUIZ`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.QUIZ)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.assessmentType == AssessmentType.QUIZ })
    }

    @Test
    fun `getFilteredRecords returns only ASSIGNMENT records when filter is ASSIGNMENT`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.ASSIGNMENT)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.assessmentType == AssessmentType.ASSIGNMENT })
    }

    @Test
    fun `getFilteredRecords returns only MIDTERM records when filter is MIDTERM`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.MIDTERM)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.assessmentType == AssessmentType.MIDTERM })
    }

    @Test
    fun `getFilteredRecords returns only FINAL records when filter is FINAL`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.FINAL)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.assessmentType == AssessmentType.FINAL })
    }

    @Test
    fun `getFilteredRecords returns all records when filter is ALL`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.ALL)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(5, filtered.size)
    }

    // --- Empty list state ---

    @Test
    fun `empty list state when repository returns empty list`() = runTest {
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(emptyList())
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)

        viewModel.uiState.test {
            // Default initial state
            awaitItem()

            // Success with empty list
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertTrue(success.records.isEmpty())
            assertNull(success.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filtered records return empty when no matching assessment type`() = runTest {
        // Only EXAM and QUIZ grades
        val limitedGrades = sampleGrades.filter {
            it.assessmentType in listOf(AssessmentType.EXAM, AssessmentType.QUIZ)
        }
        every { getGradesUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(limitedGrades)
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(GradesFilter.ASSIGNMENT)
        val filtered = viewModel.getFilteredRecords()

        assertTrue(filtered.isEmpty())
    }

    // --- No user context ---

    @Test
    fun `no load when userId is null`() = runTest {
        every { sessionManager.currentUser } returns null

        every { getGradesUseCase(studentId = any()) } returns flowOf(
            Resource.Success(emptyList())
        )

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        // Use case should never be called when userId is null
        verify(exactly = 0) { getGradesUseCase(studentId = any()) }

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading) // Stays in default loading state
            assertTrue(state.records.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Retry ---

    @Test
    fun `retry reloads grades data`() = runTest {
        var callCount = 0
        every { getGradesUseCase(studentId = "student-1") } answers {
            callCount++
            if (callCount == 1) {
                flowOf(Resource.Error(Exception("Server unavailable"), emptyList()))
            } else {
                flowOf(Resource.Success(sampleGrades))
            }
        }

        val viewModel = GradesViewModel(getGradesUseCase, tenantContext)
        advanceUntilIdle()

        // First call should result in error
        assertEquals("Server unavailable", viewModel.uiState.value.error)

        // Retry should reload
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.records.size)
        assertNull(viewModel.uiState.value.error)
    }

    // --- Grade percentage calculation ---

    @Test
    fun `grade record percentage is calculated correctly`() {
        val record = sampleGrades.first() // score=95, maxScore=100
        assertEquals(95f, record.percentage)
    }

    @Test
    fun `grade record percentage handles zero maxScore`() {
        val record = GradeRecord(
            id = "grade-zero",
            studentId = "student-1",
            subjectId = "subj-1",
            subjectName = "Test",
            assessmentType = AssessmentType.QUIZ,
            assessmentName = "Zero Max",
            score = 50f,
            maxScore = 0f,
            date = LocalDate.of(2025, 1, 1)
        )
        assertEquals(0f, record.percentage)
    }
}
