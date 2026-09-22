package org.hogwarts.android.feature.lumos.ui

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.feature.lumos.data.repository.LumosRepository
import org.hogwarts.android.feature.lumos.domain.model.CatalogCourse
import org.hogwarts.android.feature.lumos.domain.model.CourseSearch
import org.hogwarts.android.feature.lumos.domain.model.CourseTypeKey
import org.hogwarts.android.feature.lumos.domain.model.GradeShelf
import org.hogwarts.android.feature.lumos.domain.model.LumosCoursesPage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LumosCoursesViewModelTest {

    private lateinit var repository: LumosRepository

    private fun page(effectiveGrade: Int?, search: List<CatalogCourse> = emptyList(), total: Int = 0) =
        LumosCoursesPage(
            shelves = listOf(GradeShelf(12, listOf(course("a", 12)))),
            continueWatching = emptyList(),
            recommendedGrade = 12,
            effectiveGrade = effectiveGrade,
            startHere = null,
            search = CourseSearch(query = "", courses = search, total = total, page = 1),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        coEvery { repository.getCoursesPage(null, null, 1) } returns page(effectiveGrade = 12)
        coEvery { repository.getCoursesPage(5, null, 1) } returns page(effectiveGrade = 5)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opens on the grade the server chose, not one of its own`() = runTest {
        val state = LumosCoursesViewModel(repository).uiState.value
        coVerify { repository.getCoursesPage(null, null, 1) }
        assertEquals(12, state.page?.effectiveGrade)
    }

    @Test
    fun `picking a grade re-asks the server for that grade`() = runTest {
        val vm = LumosCoursesViewModel(repository)
        vm.onGradeSelected(5)
        coVerify { repository.getCoursesPage(5, null, 1) }
        assertEquals(5, vm.uiState.value.level)
        assertEquals(5, vm.uiState.value.page?.effectiveGrade)
    }

    @Test
    fun `a submitted search leaves the browse view and clearing returns to it`() = runTest {
        coEvery { repository.getCoursesPage(null, "math", 1) } returns
            page(effectiveGrade = null, search = listOf(course("m", 3)), total = 1)
        val vm = LumosCoursesViewModel(repository)

        vm.submitSearch("math")
        assertEquals("math", vm.uiState.value.searchQuery)
        assertEquals(listOf("m"), vm.uiState.value.searchResults.map { it.id })

        vm.clearSearch()
        assertTrue(vm.uiState.value.searchQuery.isEmpty())
        assertTrue(vm.uiState.value.searchResults.isEmpty())
    }

    @Test
    fun `course kind follows the web's chapter thresholds`() {
        assertEquals(CourseTypeKey.PROFESSIONAL_CERTIFICATE, course("x", 1, chapters = 10).typeKey)
        assertEquals(CourseTypeKey.SPECIALIZATION, course("x", 1, chapters = 5).typeKey)
        assertEquals(CourseTypeKey.COURSE, course("x", 1, chapters = 3).typeKey)
        assertEquals(CourseTypeKey.SHORT_COURSE, course("x", 1, chapters = 2).typeKey)
    }

    private fun course(id: String, grade: Int, chapters: Int = 4) = CatalogCourse(
        id = id,
        slug = id,
        title = id,
        imageUrl = null,
        color = null,
        grades = listOf(grade),
        chapters = chapters,
        enrollments = 0,
        totalLessons = 10,
        averageRating = 0.0,
    )
}
