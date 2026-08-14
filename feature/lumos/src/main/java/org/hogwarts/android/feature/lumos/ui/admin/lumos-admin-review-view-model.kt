package org.hogwarts.android.feature.lumos.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lumos.domain.model.VideoItem
import org.hogwarts.android.feature.lumos.domain.usecase.GetPendingVideosUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.ReviewVideoUseCase
import javax.inject.Inject

data class LumosAdminReviewUiState(
    val isLoading: Boolean = false,
    val pendingVideos: List<VideoItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LumosAdminReviewViewModel @Inject constructor(
    private val getPendingVideosUseCase: GetPendingVideosUseCase,
    private val reviewVideoUseCase: ReviewVideoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LumosAdminReviewUiState(isLoading = true))
    val uiState: StateFlow<LumosAdminReviewUiState> = _uiState.asStateFlow()

    init {
        loadPendingVideos()
    }

    fun approveVideo(videoId: String) {
        viewModelScope.launch {
            val res = reviewVideoUseCase(videoId = videoId, action = "APPROVE", feedback = null)
            if (res is Result.Success) {
                _uiState.update {
                    it.copy(pendingVideos = it.pendingVideos.filter { v -> v.id != videoId })
                }
            }
        }
    }

    fun rejectVideo(videoId: String, feedback: String) {
        viewModelScope.launch {
            val res = reviewVideoUseCase(videoId = videoId, action = "REJECT", feedback = feedback)
            if (res is Result.Success) {
                _uiState.update {
                    it.copy(pendingVideos = it.pendingVideos.filter { v -> v.id != videoId })
                }
            }
        }
    }

    fun refresh() {
        loadPendingVideos()
    }

    private fun loadPendingVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = getPendingVideosUseCase()
            if (res is Result.Success) {
                _uiState.update {
                    it.copy(isLoading = false, pendingVideos = res.data)
                }
            } else if (res is Result.Error) {
                _uiState.update {
                    it.copy(isLoading = false, error = res.exception.message)
                }
            }
        }
    }
}
