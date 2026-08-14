package org.hogwarts.android.feature.lumos.ui

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.usecase.GetCoursesUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseCatalogViewModelTest {

    private lateinit var getCoursesUseCase: GetCoursesUseCase
    private val demoCourses = listOf(
        course(id = "a", title = "Math 101", grades = listOf(1)),
        course(id = "b", title = "Arabic Basics", grades = listOf(1, 2)),
        course(id = "c", title = "Physics", grades = listOf(2)),
        course(id = "d", title = "Chemistry", grades = emptyList())
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getCoursesUseCase = mockk()
        coEvery { getCoursesUseCase(any(), any(), any()) } returns Result.Success(demoCourses)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default active grade is 1 and filters to grade 1 plus ungraded`() = runTest {
        val state = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle()).uiState.value
        assertEquals(1, state.activeGrade)
        assertEquals(listOf("a", "b", "d"), state.filteredCourses.map { it.id })
    }

    @Test
    fun `selecting grade 2 shows grade 2 plus multi-grade plus ungraded`() = runTest {
        val vm = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle())
        vm.selectGrade(2)
        val state = vm.uiState.value
        assertEquals(2, state.activeGrade)
        assertEquals(listOf("b", "c", "d"), state.filteredCourses.map { it.id })
    }

    @Test
    fun `clearing grade (null) returns all courses including ungraded`() = runTest {
        val vm = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle())
        vm.selectGrade(null)
        val state = vm.uiState.value
        assertNull(state.activeGrade)
        assertEquals(4, state.filteredCourses.size)
    }

    @Test
    fun `search filters by title while respecting active grade`() = runTest {
        val vm = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle())
        vm.onSearchQueryChange("arabic")
        val state = vm.uiState.value
        assertEquals(1, state.activeGrade)
        assertEquals(listOf("b"), state.filteredCourses.map { it.id })
    }

    @Test
    fun `locked grade entry pre-applies the grade and hides the picker`() = runTest {
        val savedState = SavedStateHandle(
            mapOf("initialGrade" to 2, "lockGrade" to true)
        )
        val state = CourseCatalogViewModel(getCoursesUseCase, savedState).uiState.value
        assertEquals(2, state.activeGrade)
        assertTrue(state.gradeFilterLocked)
        assertEquals(listOf("b", "c", "d"), state.filteredCourses.map { it.id })
    }

    @Test
    fun `selectGrade is ignored when locked`() = runTest {
        val savedState = SavedStateHandle(
            mapOf("initialGrade" to 2, "lockGrade" to true)
        )
        val vm = CourseCatalogViewModel(getCoursesUseCase, savedState)
        vm.selectGrade(7)
        val state = vm.uiState.value
        assertEquals(2, state.activeGrade)
        assertTrue(state.gradeFilterLocked)
    }

    private fun course(id: String, title: String, grades: List<Int>) = Course(
        id = id,
        schoolId = "s",
        title = title,
        description = "",
        instructorName = "",
        thumbnailUrl = null,
        category = "",
        enrollmentCount = 0,
        lessonCount = 0,
        totalDuration = "",
        grades = grades
    )
}
