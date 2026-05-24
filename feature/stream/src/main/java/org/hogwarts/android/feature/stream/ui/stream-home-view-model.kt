package org.hogwarts.android.feature.stream.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.stream.domain.model.Course
import org.hogwarts.android.feature.stream.domain.usecase.GetCoursesUseCase
import javax.inject.Inject

data class StreamHomeUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = true,
    val isAdmin: Boolean = false,
    val continueWatching: List<Course> = emptyList(),
    val hotReleases: List<Course> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StreamHomeViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamHomeUiState())
    val uiState: StateFlow<StreamHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getCoursesUseCase()) {
                is Result.Success -> {
                    val all = result.data
                    val inProgress = all.filter { it.progress > 0f && it.progress < 1f }
                    val top = all.sortedByDescending { it.enrollmentCount }.take(4)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            continueWatching = inProgress.take(5),
                            hotReleases = top
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun refresh() = load()
}
