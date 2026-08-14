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
import org.hogwarts.android.feature.lumos.domain.model.Lesson
import org.hogwarts.android.feature.lumos.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.lumos.domain.usecase.GetLessonUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.UpdateLessonProgressUseCase
import javax.inject.Inject

data class TextLessonUiState(
    val isLoading: Boolean = false,
    val lesson: Lesson? = null,
    val isCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TextLessonViewModel @Inject constructor(
    private val getLessonUseCase: GetLessonUseCase,
    private val updateLessonProgressUseCase: UpdateLessonProgressUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val courseId: String = checkNotNull(savedStateHandle["courseId"])
    val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _uiState = MutableStateFlow(TextLessonUiState(isLoading = true))
    val uiState: StateFlow<TextLessonUiState> = _uiState.asStateFlow()

    init {
        loadLesson()
    }

    fun markAsCompleted() {
        _uiState.update { it.copy(isCompleted = true) }
        viewModelScope.launch {
            updateLessonProgressUseCase(
                courseId = courseId,
                lessonId = lessonId,
                status = LessonProgressStatus.COMPLETED
            )
        }
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getLessonUseCase(courseId, lessonId)
            if (result is Result.Success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lesson = result.data,
                        isCompleted = result.data.isCompleted
                    )
                }
            } else if (result is Result.Error) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
            }
        }
    }
}
