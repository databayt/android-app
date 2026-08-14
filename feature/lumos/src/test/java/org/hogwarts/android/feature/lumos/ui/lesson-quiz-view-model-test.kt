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
import org.hogwarts.android.feature.lumos.domain.model.QuizQuestion
import org.hogwarts.android.feature.lumos.domain.usecase.GetQuizQuestionsUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.SubmitQuizUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonQuizViewModelTest {

    private lateinit var getQuizQuestionsUseCase: GetQuizQuestionsUseCase
    private lateinit var submitQuizUseCase: SubmitQuizUseCase

    private val demoQuestions = listOf(
        QuizQuestion(
            id = "q1",
            question = "What is 2 + 2?",
            options = listOf("3", "4", "5"),
            correctAnswer = 1
        ),
        QuizQuestion(
            id = "q2",
            question = "What is 3 + 3?",
            options = listOf("6", "7", "8"),
            correctAnswer = 0
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getQuizQuestionsUseCase = mockk()
        submitQuizUseCase = mockk()

        coEvery { getQuizQuestionsUseCase("c1", "l1") } returns Result.Success(demoQuestions)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads questions and starts at index 0`() = runTest {
        val vm = LessonQuizViewModel(
            getQuizQuestionsUseCase,
            submitQuizUseCase,
            SavedStateHandle(mapOf("courseId" to "c1", "lessonId" to "l1"))
        )

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.questions.size)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `selecting option and checking answer`() = runTest {
        val vm = LessonQuizViewModel(
            getQuizQuestionsUseCase,
            submitQuizUseCase,
            SavedStateHandle(mapOf("courseId" to "c1", "lessonId" to "l1"))
        )

        vm.selectOption("q1", 1)
        assertEquals(1, vm.uiState.value.selectedAnswers["q1"])

        vm.checkAnswer()
        assertTrue(vm.uiState.value.isAnswerChecked)
    }

    @Test
    fun `finishing quiz submits answers and grades correctly`() = runTest {
        coEvery { submitQuizUseCase("c1", "l1", any()) } returns Result.Success(Pair(2, true))

        val vm = LessonQuizViewModel(
            getQuizQuestionsUseCase,
            submitQuizUseCase,
            SavedStateHandle(mapOf("courseId" to "c1", "lessonId" to "l1"))
        )

        vm.selectOption("q1", 1)
        vm.selectOption("q2", 0)
        vm.finishQuiz()

        val state = vm.uiState.value
        assertTrue(state.isFinished)
        assertEquals(2, state.score)
        assertTrue(state.isPassed)
    }
}
