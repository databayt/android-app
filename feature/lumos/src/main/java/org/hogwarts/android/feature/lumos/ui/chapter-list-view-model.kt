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
import org.hogwarts.android.feature.lumos.domain.usecase.GetChaptersUseCase
import javax.inject.Inject

data class ChapterListUiState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ChapterListViewModel @Inject constructor(
    private val getChaptersUseCase: GetChaptersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val courseId: String = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow(ChapterListUiState(isLoading = true))
    val uiState: StateFlow<ChapterListUiState> = _uiState.asStateFlow()

    init {
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getChaptersUseCase(courseId)
            if (result is Result.Success) {
                _uiState.update {
                    it.copy(isLoading = false, chapters = result.data)
                }
            } else if (result is Result.Error) {
                _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }
}
