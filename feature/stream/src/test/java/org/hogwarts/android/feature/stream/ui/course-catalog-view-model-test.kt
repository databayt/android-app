package org.hogwarts.android.feature.stream.ui

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
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.usecase.GetCoursesUseCase
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
        coEvery { getCoursesUseCase(any(), any()) } returns Result.Success(demoCourses)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default active grade is 1 and filters to grade 1 plus ungraded`() = runTest {
        val state = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle()).uiState.value
        assertEquals(1, state.activeGrade)
        // "a", "b" = grade 1, "d" = ungraded (universal).
        assertEquals(listOf("a", "b", "d"), state.filteredCourses.map { it.id })
    }

    @Test
    fun `selecting grade 2 shows grade 2 plus multi-grade plus ungraded`() = runTest {
        val vm = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle())
        vm.selectGrade(2)
        val state = vm.uiState.value
        assertEquals(2, state.activeGrade)
        // "b" covers grades [1,2], "c" = grade 2, "d" = ungraded (universal).
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
    fun `ungraded courses pass every grade filter so API-sourced rows stay visible`() = runTest {
        val vm = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle())
        // Course "d" has an empty grades list — it should render under grade 1 (default)…
        assertTrue(vm.uiState.value.filteredCourses.any { it.id == "d" })

        // …and under an arbitrary grade like 5, even though no demo course covers grade 5.
        vm.selectGrade(5)
        val atGrade5 = vm.uiState.value.filteredCourses
        assertTrue(atGrade5.any { it.id == "d" })
        // And only ungraded rows pass, not the graded ones.
        assertTrue(atGrade5.none { it.grades.isNotEmpty() })
    }

    @Test
    fun `all-courses-ungraded regression — grade pill never empties the catalog`() = runTest {
        // This mirrors the production bug: the backend returns courses without
        // grades, and tapping a grade pill used to wipe the grid.
        val ungraded = listOf(
            course(id = "x", title = "API Course 1", grades = emptyList()),
            course(id = "y", title = "API Course 2", grades = emptyList())
        )
        coEvery { getCoursesUseCase(any(), any()) } returns Result.Success(ungraded)

        val vm = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle())
        // default (grade = 1) — both survive
        assertEquals(2, vm.uiState.value.filteredCourses.size)

        // cycle through a few grades — still never empty
        listOf(2, 3, 7, 12).forEach { g ->
            vm.selectGrade(g)
            assertEquals(
                "grade $g should keep ungraded rows visible",
                2,
                vm.uiState.value.filteredCourses.size
            )
        }
    }

    @Test
    fun `locked grade entry pre-applies the grade and hides the picker`() = runTest {
        // Mirrors the home-tile-as-student path: nav host pushes lockGrade=true
        // with the student's grade. The picker must stay hidden and the filter
        // apply to that grade.
        val savedState = SavedStateHandle(
            mapOf("initialGrade" to 2, "lockGrade" to true)
        )
        val state = CourseCatalogViewModel(getCoursesUseCase, savedState).uiState.value
        assertEquals(2, state.activeGrade)
        assertTrue(state.gradeFilterLocked)
        // grade 2 + multi-grade [1,2] + ungraded
        assertEquals(listOf("b", "c", "d"), state.filteredCourses.map { it.id })
    }

    @Test
    fun `locked entry with null grade shows all courses but still hides the picker`() = runTest {
        // Backend hasn't supplied the student's grade yet — degrade to "all
        // courses" while still hiding the picker (the screen is meant to be
        // hands-off for students).
        val savedState = SavedStateHandle(
            mapOf("initialGrade" to null, "lockGrade" to true)
        )
        val state = CourseCatalogViewModel(getCoursesUseCase, savedState).uiState.value
        assertNull(state.activeGrade)
        assertTrue(state.gradeFilterLocked)
        assertEquals(4, state.filteredCourses.size)
    }

    @Test
    fun `selectGrade is ignored when locked`() = runTest {
        val savedState = SavedStateHandle(
            mapOf("initialGrade" to 2, "lockGrade" to true)
        )
        val vm = CourseCatalogViewModel(getCoursesUseCase, savedState)
        vm.selectGrade(7) // would switch grade if not locked
        val state = vm.uiState.value
        assertEquals(2, state.activeGrade)
        assertTrue(state.gradeFilterLocked)
    }

    @Test
    fun `unlocked entry preserves the existing default behavior`() = runTest {
        // The browse-from-StreamHome path: lockGrade=false, picker is visible
        // and grade 1 is the default.
        val savedState = SavedStateHandle(
            mapOf("initialGrade" to null, "lockGrade" to false)
        )
        val state = CourseCatalogViewModel(getCoursesUseCase, savedState).uiState.value
        assertEquals(1, state.activeGrade)
        assertFalse(state.gradeFilterLocked)
    }

    @Test
    fun `multi-grade course appears under every grade in its list`() = runTest {
        // Regression for the API-returns-grades-array bug: a subject with
        // grades=[3,5,7] must show under 3, 5, and 7 — and NOT under 4.
        val multi = listOf(course(id = "m", title = "Multi", grades = listOf(3, 5, 7)))
        coEvery { getCoursesUseCase(any(), any()) } returns Result.Success(multi)

        val vm = CourseCatalogViewModel(getCoursesUseCase, SavedStateHandle())

        listOf(3, 5, 7).forEach { g ->
            vm.selectGrade(g)
            assertEquals(
                "grade $g should match because it's in the course's grades list",
                listOf("m"),
                vm.uiState.value.filteredCourses.map { it.id }
            )
        }

        vm.selectGrade(4)
        assertTrue(
            "grade 4 is not in [3,5,7] so the course must be filtered out",
            vm.uiState.value.filteredCourses.isEmpty()
        )
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
