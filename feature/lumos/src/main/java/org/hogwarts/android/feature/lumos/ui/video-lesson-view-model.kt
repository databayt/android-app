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
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.lumos.domain.model.Lesson
import org.hogwarts.android.feature.lumos.domain.model.LessonProgressStatus
import org.hogwarts.android.feature.lumos.domain.usecase.GetLessonUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.UpdateLessonProgressUseCase
import javax.inject.Inject

data class VideoLessonUiState(
    val isLoading: Boolean = false,
    val lesson: Lesson? = null,
    val isCompleted: Boolean = false,
    val resumePositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val userIdentifier: String? = null,
    val error: String? = null
)

@HiltViewModel
class VideoLessonViewModel @Inject constructor(
    private val getLessonUseCase: GetLessonUseCase,
    private val updateLessonProgressUseCase: UpdateLessonProgressUseCase,
    private val tenantContext: TenantContext,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val courseId: String = checkNotNull(savedStateHandle["courseId"])
    val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _uiState = MutableStateFlow(
        VideoLessonUiState(
            isLoading = true,
            userIdentifier = tenantContext.userName ?: "Hogwarts Student"
        )
    )
    val uiState: StateFlow<VideoLessonUiState> = _uiState.asStateFlow()

    init {
        loadLesson()
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun updateResumePosition(positionMs: Long) {
        _uiState.update { it.copy(resumePositionMs = positionMs) }
        viewModelScope.launch {
            updateLessonProgressUseCase(
                courseId = courseId,
                lessonId = lessonId,
                status = if (_uiState.value.isCompleted) LessonProgressStatus.COMPLETED else LessonProgressStatus.IN_PROGRESS,
                watchedSeconds = positionMs / 1000
            )
        }
    }

    fun onVideoEnded() {
        markAsCompleted()
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
                val lesson = result.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lesson = lesson,
                        isCompleted = lesson.isCompleted
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
