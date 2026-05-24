package org.hogwarts.android.feature.lessons.ui

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
import org.hogwarts.android.feature.lessons.domain.model.LessonPlan
import org.hogwarts.android.feature.lessons.domain.usecase.GetLessonDetailUseCase
import javax.inject.Inject

data class LessonDetailUiState(
    val lesson: LessonPlan? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLessonDetailUseCase: GetLessonDetailUseCase
) : ViewModel() {

    private val lessonId: String = savedStateHandle["lessonId"] ?: ""

    private val _uiState = MutableStateFlow(LessonDetailUiState())
    val uiState: StateFlow<LessonDetailUiState> = _uiState.asStateFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getLessonDetailUseCase(lessonId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, lesson = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}
