package org.hogwarts.android.feature.lumos.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lumos.domain.model.ApprovalStatus
import org.hogwarts.android.feature.lumos.domain.model.VideoItem
import org.hogwarts.android.feature.lumos.domain.usecase.GetTeacherVideosUseCase
import org.hogwarts.android.feature.lumos.domain.usecase.ProposeVideoUseCase
import javax.inject.Inject

data class LumosTeacherVideosUiState(
    val isLoading: Boolean = false,
    val videos: List<VideoItem> = emptyList(),
    val filteredVideos: List<VideoItem> = emptyList(),
    val selectedFilter: String = "ALL", // ALL, APPROVED, PENDING, REJECTED
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LumosTeacherVideosViewModel @Inject constructor(
    private val getTeacherVideosUseCase: GetTeacherVideosUseCase,
    private val proposeVideoUseCase: ProposeVideoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LumosTeacherVideosUiState(isLoading = true))
    val uiState: StateFlow<LumosTeacherVideosUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilter()
    }

    fun uploadVideo(lessonId: String, title: String, videoUrl: String, duration: Long, visibility: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            val res = proposeVideoUseCase(lessonId, title, videoUrl, duration, visibility)
            if (res is Result.Success) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadSuccess = true,
                        videos = listOf(res.data) + it.videos
                    )
                }
                applyFilter()
            } else {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun resetUploadSuccess() {
        _uiState.update { it.copy(uploadSuccess = false) }
    }

    fun refresh() {
        loadVideos()
    }

    private fun loadVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = getTeacherVideosUseCase()
            if (res is Result.Success) {
                _uiState.update {
                    it.copy(isLoading = false, videos = res.data)
                }
                applyFilter()
            } else if (res is Result.Error) {
                _uiState.update {
                    it.copy(isLoading = false, error = res.exception.message)
                }
            }
        }
    }

    private fun applyFilter() {
        val state = _uiState.value
        val filtered = when (state.selectedFilter) {
            "APPROVED" -> state.videos.filter { it.approvalStatus == ApprovalStatus.APPROVED }
            "PENDING" -> state.videos.filter { it.approvalStatus == ApprovalStatus.PENDING }
            "REJECTED" -> state.videos.filter { it.approvalStatus == ApprovalStatus.REJECTED }
            else -> state.videos
        }
        _uiState.update { it.copy(filteredVideos = filtered) }
    }
}
