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
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.stream.domain.usecase.GetLessonUseCase
import org.hogwarts.android.feature.stream.domain.usecase.UpdateLessonProgressUseCase
import javax.inject.Inject

data class TextLessonUiState(
    val lesson: Lesson? = null,
    val textContent: String = "",
    val isLoading: Boolean = false,
    val isMarking: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TextLessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLessonUseCase: GetLessonUseCase,
    private val updateLessonProgressUseCase: UpdateLessonProgressUseCase
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""
    val lessonId: String = savedStateHandle["lessonId"] ?: ""

    private val _uiState = MutableStateFlow(TextLessonUiState())
    val uiState: StateFlow<TextLessonUiState> = _uiState.asStateFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getLessonUseCase(courseId, lessonId)) {
                is Result.Success -> {
                    val lesson = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lesson = lesson,
                            textContent = lesson.contentUrl ?: "",
                            isCompleted = lesson.isCompleted
                        )
                    }
                    // Mark as in-progress
                    updateLessonProgressUseCase(courseId, lessonId, LessonProgressStatus.IN_PROGRESS)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun markComplete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMarking = true) }
            when (updateLessonProgressUseCase(courseId, lessonId, LessonProgressStatus.COMPLETED)) {
                is Result.Success -> _uiState.update { it.copy(isMarking = false, isCompleted = true) }
                is Result.Error -> _uiState.update { it.copy(isMarking = false) }
                is Result.Loading -> {}
            }
        }
    }
}
