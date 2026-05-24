package org.hogwarts.android.feature.stream.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.stream.domain.model.Chapter
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.usecase.GetChaptersUseCase
import org.hogwarts.android.feature.stream.domain.usecase.GetCourseDetailUseCase
import javax.inject.Inject

data class CourseProgressUiState(
    val course: Course? = null,
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val overallProgress: Float
        get() = course?.progress ?: 0f

    val completedLessons: Int
        get() = chapters.sumOf { it.completedLessons }

    val totalLessons: Int
        get() = chapters.sumOf { it.lessonCount }
}

@HiltViewModel
class CourseProgressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val getChaptersUseCase: GetChaptersUseCase
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""

    private val _uiState = MutableStateFlow(CourseProgressUiState())
    val uiState: StateFlow<CourseProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val courseResult = getCourseDetailUseCase(courseId)
            val chaptersResult = getChaptersUseCase(courseId)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    course = (courseResult as? Result.Success)?.data,
                    chapters = (chaptersResult as? Result.Success)?.data ?: emptyList(),
                    error = if (courseResult is Result.Error) courseResult.exception.message else null
                )
            }
        }
    }

    fun refresh() = loadProgress()
}
