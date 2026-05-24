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
import org.hogwarts.android.feature.stream.domain.model.Lesson
import org.hogwarts.android.feature.stream.domain.usecase.GetChaptersUseCase
import org.hogwarts.android.feature.stream.domain.usecase.GetLessonUseCase
import javax.inject.Inject

data class ChapterWithLessons(
    val chapter: Chapter,
    val lessons: List<Lesson> = emptyList(),
    val isExpanded: Boolean = false,
    val isLoadingLessons: Boolean = false
)

data class ChapterListUiState(
    val chapters: List<ChapterWithLessons> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChapterListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChaptersUseCase: GetChaptersUseCase
) : ViewModel() {

    val courseId: String = savedStateHandle["courseId"] ?: ""

    private val _uiState = MutableStateFlow(ChapterListUiState())
    val uiState: StateFlow<ChapterListUiState> = _uiState.asStateFlow()

    init {
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getChaptersUseCase(courseId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapters = result.data.map { chapter ->
                                ChapterWithLessons(chapter = chapter)
                            }
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun toggleChapter(chapterId: String) {
        _uiState.update { state ->
            state.copy(
                chapters = state.chapters.map { cwl ->
                    if (cwl.chapter.id == chapterId) {
                        cwl.copy(isExpanded = !cwl.isExpanded)
                    } else {
                        cwl
                    }
                }
            )
        }
    }

    fun refresh() = loadChapters()
}
