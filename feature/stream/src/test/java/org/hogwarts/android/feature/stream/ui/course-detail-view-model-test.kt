package org.hogwarts.android.feature.stream.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.stream.domain.model.Chapter
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.model.CourseStatus
import org.hogwarts.android.feature.stream.domain.model.Enrollment
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonType
import org.hogwarts.android.feature.stream.domain.usecase.EnrollCourseUseCase
import org.hogwarts.android.feature.stream.domain.usecase.GetChaptersUseCase
import org.hogwarts.android.feature.stream.domain.usecase.GetCourseDetailUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailViewModelTest {

    private val courseId = "demo_math"
    private val firstLesson = Lesson(
        id = "l1",
        chapterId = "ch1",
        title = "Intro",
        type = LessonType.VIDEO,
        duration = "15m",
        contentUrl = "https://example/video.mp4",
        orderIndex = 1
    )
    private val chapter = Chapter(
        id = "ch1",
        courseId = courseId,
        title = "Chapter 1",
        orderIndex = 1,
        lessonCount = 1,
        lessons = listOf(firstLesson)
    )
    private val course = Course(
        id = courseId,
        schoolId = "s",
        title = "Math",
        description = "",
        instructorName = "",
        category = "",
        status = CourseStatus.PUBLISHED
    )

    private lateinit var getCourseDetail: GetCourseDetailUseCase
    private lateinit var getChapters: GetChaptersUseCase
    private lateinit var enroll: EnrollCourseUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getCourseDetail = mockk()
        getChapters = mockk()
        enroll = mockk()
        coEvery { getCourseDetail(courseId) } returns Result.Success(course)
        coEvery { getChapters(courseId) } returns Result.Success(listOf(chapter))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state exposes first lesson derived from chapters`() = runTest {
        val vm = build()
        val state = vm.uiState.value
        assertEquals(firstLesson, state.firstLesson)
        assertEquals(1, state.totalLessons)
    }

    @Test
    fun `enroll emits OpenLesson with first lesson after success`() = runTest {
        coEvery { enroll(courseId) } returns Result.Success(
            Enrollment(
                id = "e1",
                courseId = courseId,
                userId = "u",
                startedAt = "",
                lastAccessedAt = ""
            )
        )
        val vm = build()
        vm.events.test {
            vm.enroll()
            val event = awaitItem()
            assertTrue(event is CourseDetailEvent.OpenLesson)
            val open = event as CourseDetailEvent.OpenLesson
            assertEquals(courseId, open.courseId)
            assertEquals(firstLesson, open.lesson)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `continueLearning emits OpenLesson`() = runTest {
        val vm = build()
        vm.events.test {
            vm.continueLearning()
            val event = awaitItem() as CourseDetailEvent.OpenLesson
            assertEquals(firstLesson, event.lesson)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun build(): CourseDetailViewModel = CourseDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("courseId" to courseId)),
        getCourseDetailUseCase = getCourseDetail,
        getChaptersUseCase = getChapters,
        enrollCourseUseCase = enroll
    )
}
