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
import org.hogwarts.android.feature.lumos.domain.usecase.EnrollCourseUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.GetChaptersUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.GetCourseDetailUseCase
import javax.inject.Inject

data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val isEnrolling: Boolean = false,
    val course: Course? = null,
    val chapters: List<Chapter> = emptyList(),
    val isEnrolled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val enrollCourseUseCase: EnrollCourseUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val courseId: String = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow(CourseDetailUiState(isLoading = true))
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourseDetail()
    }

    fun enroll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEnrolling = true) }
            val res = enrollCourseUseCase(courseId)
            if (res is Result.Success) {
                _uiState.update { it.copy(isEnrolling = false, isEnrolled = true) }
            } else {
                _uiState.update { it.copy(isEnrolling = false) }
            }
        }
    }

    fun refresh() {
        loadCourseDetail()
    }

    private fun loadCourseDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val courseRes = getCourseDetailUseCase(courseId)
            val chaptersRes = getChaptersUseCase(courseId)

            if (courseRes is Result.Success) {
                val course = courseRes.data
                val chapters = if (chaptersRes is Result.Success) chaptersRes.data else emptyList()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        course = course,
                        chapters = chapters,
                        isEnrolled = course.isEnrolled || course.progress > 0f
                    )
                }
            } else if (courseRes is Result.Error) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = courseRes.exception.message
                    )
                }
            }
        }
    }
}
