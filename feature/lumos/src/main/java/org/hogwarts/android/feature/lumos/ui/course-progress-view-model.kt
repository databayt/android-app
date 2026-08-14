package org.hogwarts.android.feature.lumos.ui

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
import org.hogwarts.android.feature.lumos.domain.model.Chapter
import org.hogwarts.android.feature.lumos.domain.model.Course
import org.hogwarts.android.feature.lumos.domain.usecase.GetChaptersUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.GetCourseDetailUseCase
import javax.inject.Inject

data class CourseProgressUiState(
    val isLoading: Boolean = false,
    val course: Course? = null,
    val chapters: List<Chapter> = emptyList(),
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val progress: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class CourseProgressViewModel @Inject constructor(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val courseId: String = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow(CourseProgressUiState(isLoading = true))
    val uiState: StateFlow<CourseProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val courseRes = getCourseDetailUseCase(courseId)
            val chaptersRes = getChaptersUseCase(courseId)

            if (courseRes is Result.Success) {
                val course = courseRes.data
                val chapters = if (chaptersRes is Result.Success) chaptersRes.data else emptyList()
                val total = chapters.sumOf { it.lessons?.size ?: it.lessonCount }
                val completed = chapters.sumOf { it.completedLessons }
                val prog = if (total > 0) completed.toFloat() / total else course.progress

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        course = course,
                        chapters = chapters,
                        totalLessons = total,
                        completedLessons = completed,
                        progress = prog
                    )
                }
            } else if (courseRes is Result.Error) {
                _uiState.update {
                    it.copy(isLoading = false, error = courseRes.exception.message)
                }
            }
        }
    }
}
