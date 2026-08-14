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
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.lumos.domain.model.Lesson
import org.hogwarts.android.feature.lumos.domain.model.LessonProgress
import org.hogwarts.android.feature.lumos.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.lumos.domain.model.LessonType
import org.hogwarts.android.feature.lumos.domain.usecase.GetLessonUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.UpdateLessonProgressUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoLessonViewModelTest {

    private lateinit var getLessonUseCase: GetLessonUseCase
    private lateinit var updateLessonProgressUseCase: UpdateLessonProgressUseCase
    private lateinit var tenantContext: TenantContext

    private val demoLesson = Lesson(
        id = "l1",
        chapterId = "ch1",
        title = "Lesson 1",
        type = LessonType.VIDEO,
        duration = "10m",
        contentUrl = "https://cdn.databayt.org/media/story.mp4",
        orderIndex = 1
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getLessonUseCase = mockk()
        updateLessonProgressUseCase = mockk()
        tenantContext = mockk()

        io.mockk.every { tenantContext.userName } returns null
        coEvery { getLessonUseCase("c1", "l1") } returns Result.Success(demoLesson)
        coEvery {
            updateLessonProgressUseCase(any(), any(), any(), any(), any(), any())
        } returns Result.Success(
            LessonProgress(
                id = "p1",
                lessonId = "l1",
                enrollmentId = "e1",
                status = LessonProgressStatus.COMPLETED
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load fetches lesson successfully`() = runTest {
        val vm = VideoLessonViewModel(
            getLessonUseCase,
            updateLessonProgressUseCase,
            tenantContext,
            SavedStateHandle(mapOf("courseId" to "c1", "lessonId" to "l1"))
        )

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Lesson 1", state.lesson?.title)
    }

    @Test
    fun `mark completed updates state`() = runTest {
        val vm = VideoLessonViewModel(
            getLessonUseCase,
            updateLessonProgressUseCase,
            tenantContext,
            SavedStateHandle(mapOf("courseId" to "c1", "lessonId" to "l1"))
        )

        vm.markAsCompleted()
        assertTrue(vm.uiState.value.isCompleted)
    }

    @Test
    fun `playback speed change updates state`() = runTest {
        val vm = VideoLessonViewModel(
            getLessonUseCase,
            updateLessonProgressUseCase,
            tenantContext,
            SavedStateHandle(mapOf("courseId" to "c1", "lessonId" to "l1"))
        )

        vm.setPlaybackSpeed(1.5f)
        assertEquals(1.5f, vm.uiState.value.playbackSpeed)
    }
}
