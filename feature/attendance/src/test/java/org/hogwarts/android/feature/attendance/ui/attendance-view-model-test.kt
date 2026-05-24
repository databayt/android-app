package org.hogwarts.android.feature.attendance.ui

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
import org.hogwarts.android.feature.attendance.domain.model.AttendanceRecord
import org.hogwarts.android.feature.attendance.domain.model.AttendanceStatus
import org.hogwarts.android.feature.attendance.domain.usecase.GetAttendanceUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAttendanceUseCase: GetAttendanceUseCase
    private lateinit var tenantContext: TenantContext
    private lateinit var sessionManager: SessionManager

    private val testUser = CurrentUser(
        id = "student-1",
        email = "student@hogwarts.edu",
        schoolId = "school-1",
        role = UserRole.STUDENT,
        givenName = "Harry",
        familyName = "Potter"
    )

    private val sampleRecords = listOf(
        AttendanceRecord(
            id = "att-1",
            studentId = "student-1",
            studentName = "Harry Potter",
            classId = "class-1",
            className = "Defense Against the Dark Arts",
            date = LocalDate.of(2025, 1, 15),
            status = AttendanceStatus.PRESENT
        ),
        AttendanceRecord(
            id = "att-2",
            studentId = "student-1",
            studentName = "Harry Potter",
            classId = "class-2",
            className = "Potions",
            date = LocalDate.of(2025, 1, 15),
            status = AttendanceStatus.ABSENT,
            note = "Quidditch practice"
        ),
        AttendanceRecord(
            id = "att-3",
            studentId = "student-1",
            studentName = "Harry Potter",
            classId = "class-3",
            className = "Charms",
            date = LocalDate.of(2025, 1, 16),
            status = AttendanceStatus.LATE
        ),
        AttendanceRecord(
            id = "att-4",
            studentId = "student-1",
            studentName = "Harry Potter",
            classId = "class-4",
            className = "Transfiguration",
            date = LocalDate.of(2025, 1, 16),
            status = AttendanceStatus.EXCUSED
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAttendanceUseCase = mockk()
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
    fun `initial state emits Loading then Success with records`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flow {
            emit(Resource.Loading(null))
            emit(Resource.Success(sampleRecords))
        }

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)

        viewModel.uiState.test {
            // Default initial state (isLoading=true, records=empty)
            // Note: Loading(null) is de-duplicated by StateFlow since it produces the same state
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            assertTrue(initial.records.isEmpty())

            // After Success emission from use case
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals(4, success.records.size)
            assertNull(success.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Error state ---

    @Test
    fun `error state when repository returns error`() = runTest {
        val errorMessage = "Network connection failed"
        every { getAttendanceUseCase(studentId = "student-1") } returns flow {
            emit(Resource.Loading(null))
            emit(Resource.Error<List<AttendanceRecord>>(Exception(errorMessage), emptyList()))
        }

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)

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
    fun `error state preserves cached data`() = runTest {
        val cachedRecords = sampleRecords.take(2)
        every { getAttendanceUseCase(studentId = "student-1") } returns flow {
            emit(Resource.Loading(cachedRecords))
            emit(Resource.Error(Exception("Server error"), cachedRecords))
        }

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)

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
            assertEquals("Server error", error.error)
            assertEquals(2, error.records.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Filter change ---

    @Test
    fun `setFilter updates selectedFilter in ui state`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleRecords)
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(AttendanceFilter.ABSENT)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AttendanceFilter.ABSENT, state.selectedFilter)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFilteredRecords returns only PRESENT records when filter is PRESENT`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleRecords)
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(AttendanceFilter.PRESENT)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.status == AttendanceStatus.PRESENT })
    }

    @Test
    fun `getFilteredRecords returns only ABSENT records when filter is ABSENT`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleRecords)
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(AttendanceFilter.ABSENT)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.status == AttendanceStatus.ABSENT })
    }

    @Test
    fun `getFilteredRecords returns only LATE records when filter is LATE`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleRecords)
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(AttendanceFilter.LATE)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.status == AttendanceStatus.LATE })
    }

    @Test
    fun `getFilteredRecords returns only EXCUSED records when filter is EXCUSED`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleRecords)
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(AttendanceFilter.EXCUSED)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(1, filtered.size)
        assertTrue(filtered.all { it.status == AttendanceStatus.EXCUSED })
    }

    @Test
    fun `getFilteredRecords returns all records when filter is ALL`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(sampleRecords)
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        viewModel.setFilter(AttendanceFilter.ALL)
        val filtered = viewModel.getFilteredRecords()

        assertEquals(4, filtered.size)
    }

    // --- Empty list state ---

    @Test
    fun `empty list state when repository returns empty list`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(emptyList())
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)

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

    // --- Teacher role detection ---

    @Test
    fun `isTeacher is true when user has TEACHER role`() = runTest {
        val teacherUser = testUser.copy(role = UserRole.TEACHER)
        every { sessionManager.currentUser } returns teacherUser

        every { getAttendanceUseCase(studentId = teacherUser.id) } returns flowOf(
            Resource.Success(emptyList())
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)

        viewModel.uiState.test {
            // Skip initial
            awaitItem()

            // After loadAttendance sets isTeacher
            val state = awaitItem()
            assertTrue(state.isTeacher)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isTeacher is false when user has STUDENT role`() = runTest {
        every { getAttendanceUseCase(studentId = "student-1") } returns flowOf(
            Resource.Success(emptyList())
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)

        viewModel.uiState.test {
            // Skip initial
            awaitItem()

            val state = awaitItem()
            assertFalse(state.isTeacher)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- No user context ---

    @Test
    fun `no load when userId is null`() = runTest {
        every { sessionManager.currentUser } returns null

        every { getAttendanceUseCase(studentId = any()) } returns flowOf(
            Resource.Success(emptyList())
        )

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        // Use case should never be called when userId is null
        verify(exactly = 0) { getAttendanceUseCase(studentId = any()) }

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading) // Stays in default loading state
            assertTrue(state.records.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Retry ---

    @Test
    fun `retry reloads attendance data`() = runTest {
        var callCount = 0
        every { getAttendanceUseCase(studentId = "student-1") } answers {
            callCount++
            if (callCount == 1) {
                flowOf(Resource.Error(Exception("First failure"), emptyList()))
            } else {
                flowOf(Resource.Success(sampleRecords))
            }
        }

        val viewModel = AttendanceViewModel(getAttendanceUseCase, tenantContext)
        advanceUntilIdle()

        // First call should result in error
        assertEquals("First failure", viewModel.uiState.value.error)

        // Retry should reload
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(4, viewModel.uiState.value.records.size)
        assertNull(viewModel.uiState.value.error)
    }
}
