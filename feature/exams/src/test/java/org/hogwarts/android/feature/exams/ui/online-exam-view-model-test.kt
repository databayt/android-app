package org.hogwarts.android.feature.exams.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.feature.exams.data.repository.ExamsApiException
import org.hogwarts.android.feature.exams.domain.model.OnlineExam
import org.hogwarts.android.feature.exams.domain.model.OnlineQuestion
import org.hogwarts.android.feature.exams.testing.FakeExamsRepository
import org.hogwarts.android.feature.exams.ui.take.OnlineExamViewModel
import org.hogwarts.android.feature.exams.ui.take.TakeProblem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric: the ViewModel reads its type-safe route, which decodes through a Bundle. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OnlineExamViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeExamsRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `online exam submits the answers once and names a closed session`() = runTest(dispatcher) {
        repository.online = OnlineExam(
            sessionId = "sess-1", examId = "ex-1", title = "Quiz", durationMinutes = 10, totalMarks = 2,
            instructions = null, secondsRemaining = 600,
            questions = listOf(OnlineQuestion("q-1", "One?", "TRUE_FALSE", emptyList(), 1.0), OnlineQuestion("q-2", "Two?", "SHORT_ANSWER", emptyList(), 1.0)),
        )
        val vm = OnlineExamViewModel(SavedStateHandle(mapOf("examId" to "ex-1")), repository)
        advanceTimeBy(10)
        assertEquals(600, vm.uiState.value.secondsLeft)
        vm.answer("q-1", "true")
        vm.answer("q-2", " ")
        assertEquals(1, vm.uiState.value.answeredCount)
        vm.submit()
        vm.submit()
        advanceTimeBy(10)
        assertEquals(listOf(mapOf("q-1" to "true")), repository.submitted)
        assertEquals(1, vm.uiState.value.outcome?.answered)

        repository.onlineError = ExamsApiException(400, "Exam already submitted")
        val closed = OnlineExamViewModel(SavedStateHandle(mapOf("examId" to "ex-1")), repository)
        advanceUntilIdle()
        assertEquals(TakeProblem.AlreadySubmitted, closed.uiState.value.problem)
    }
}
