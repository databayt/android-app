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
import org.hogwarts.android.feature.lumos.domain.model.Chapter
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.model.Enrollment
import org.hogwarts.android.feature.lumos.domain.usecase.EnrollCourseUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.GetChaptersUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.GetCourseDetailUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailViewModelTest {

    private lateinit var getCourseDetailUseCase: GetCourseDetailUseCase
    private lateinit var getChaptersUseCase: GetChaptersUseCase
    private lateinit var enrollCourseUseCase: EnrollCourseUseCase

    private val demoCourse = Course(
        id = "c1",
        schoolId = "s1",
        title = "Math 101",
        description = "Intro to Math",
        instructorName = "Mr. Test",
        category = "Math"
    )

    private val demoChapters = listOf(
        Chapter(
            id = "ch1",
            courseId = "c1",
            title = "Chapter 1",
            orderIndex = 1,
            lessonCount = 2,
            completedLessons = 0
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getCourseDetailUseCase = mockk()
        getChaptersUseCase = mockk()
        enrollCourseUseCase = mockk()

        coEvery { getCourseDetailUseCase("c1") } returns Result.Success(demoCourse)
        coEvery { getChaptersUseCase("c1") } returns Result.Success(demoChapters)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates course and chapters`() = runTest {
        val vm = CourseDetailViewModel(
            getCourseDetailUseCase,
            getChaptersUseCase,
            enrollCourseUseCase,
            SavedStateHandle(mapOf("courseId" to "c1"))
        )

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.course)
        assertEquals("Math 101", state.course?.title)
        assertEquals(1, state.chapters.size)
    }

    @Test
    fun `enroll updates enrolled state`() = runTest {
        coEvery { enrollCourseUseCase("c1") } returns Result.Success(
            Enrollment(
                id = "e1",
                courseId = "c1",
                userId = "u1",
                progress = 0f,
                startedAt = "2026-08-14",
                lastAccessedAt = "2026-08-14"
            )
        )

        val vm = CourseDetailViewModel(
            getCourseDetailUseCase,
            getChaptersUseCase,
            enrollCourseUseCase,
            SavedStateHandle(mapOf("courseId" to "c1"))
        )

        vm.enroll()
        val state = vm.uiState.value
        assertTrue(state.isEnrolled)
    }
}
