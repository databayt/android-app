package org.hogwarts.android.feature.stream.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.stream.domain.model.Chapter
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonType
import org.hogwarts.android.feature.stream.domain.usecase.EnrollCourseUseCase
import org.hogwarts.android.feature.stream.domain.usecase.GetChaptersUseCase
import org.hogwarts.android.feature.stream.domain.usecase.GetCourseDetailUseCase
import javax.inject.Inject

data class CourseDetailUiState(
    val course: Course? = null,
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = false,
    val isEnrolling: Boolean = false,
    val isEnrolled: Boolean = false,
    val error: String? = null
) {
    /** First lesson id across all chapters — used for "Start Learning" navigation. */
    val firstLesson: Lesson?
        get() = chapters.asSequence()
            .mapNotNull { it.lessons }
            .flatten()
            .firstOrNull()

    val totalLessons: Int
        get() = chapters.sumOf { it.lessons?.size ?: it.lessonCount }
}

sealed interface CourseDetailEvent {
    /** Emitted after a successful enrollment so the screen can jump to the first lesson. */
    data class OpenLesson(val courseId: String, val lesson: Lesson) : CourseDetailEvent
}

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val enrollCourseUseCase: EnrollCourseUseCase
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CourseDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CourseDetailEvent> = _events.asSharedFlow()

    init {
        loadCourseDetail()
    }

    private fun loadCourseDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val courseResult = getCourseDetailUseCase(courseId)
            val chaptersResult = getChaptersUseCase(courseId)

            val course = (courseResult as? Result.Success)?.data
            val chapters = (chaptersResult as? Result.Success)?.data ?: emptyList()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    course = course,
                    chapters = chapters,
                    isEnrolled = (course?.progress ?: 0f) > 0f,
                    error = if (course == null) "Failed to load course" else null
                )
            }
        }
    }

    /**
     * Mirrors the web catalog-enrollment-button flow: enroll → auto-open the first lesson.
     */
    fun enroll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEnrolling = true) }
            when (val result = enrollCourseUseCase(courseId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isEnrolling = false, isEnrolled = true) }
                    _uiState.value.firstLesson?.let { lesson ->
                        _events.tryEmit(CourseDetailEvent.OpenLesson(courseId, lesson))
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isEnrolling = false, error = result.exception.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun continueLearning() {
        _uiState.value.firstLesson?.let { lesson ->
            _events.tryEmit(CourseDetailEvent.OpenLesson(courseId, lesson))
        }
    }

    fun refresh() = loadCourseDetail()
}

/** Convenience used by the screen to route to the right lesson type. */
fun Lesson.routeType(): LessonType = type
