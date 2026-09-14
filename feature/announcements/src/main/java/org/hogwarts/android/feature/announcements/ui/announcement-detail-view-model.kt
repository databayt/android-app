package org.hogwarts.android.feature.announcements.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.announcements.data.repository.AnnouncementsRepository
import org.hogwarts.android.feature.announcements.data.repository.DetailResult
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import javax.inject.Inject

sealed interface AnnouncementDetailUiState {
    data object Loading : AnnouncementDetailUiState
    data class Ready(val announcement: Announcement, val isOffline: Boolean = false) : AnnouncementDetailUiState
    /** `detail.tsx`'s error branch: "Announcement not found". */
    data object NotFound : AnnouncementDetailUiState
    data class Failed(val message: String?) : AnnouncementDetailUiState
}

@HiltViewModel
class AnnouncementDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnnouncementsRepository,
) : ViewModel() {

    private val announcementId: String = savedStateHandle.get<String>("announcementId").orEmpty()

    private val _uiState = MutableStateFlow<AnnouncementDetailUiState>(AnnouncementDetailUiState.Loading)
    val uiState: StateFlow<AnnouncementDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (announcementId.isBlank()) {
            _uiState.value = AnnouncementDetailUiState.NotFound
            return
        }
        viewModelScope.launch {
            if (_uiState.value !is AnnouncementDetailUiState.Ready) _uiState.value = AnnouncementDetailUiState.Loading
            _uiState.value = when (val result = repository.detail(announcementId)) {
                is DetailResult.Fresh -> AnnouncementDetailUiState.Ready(result.announcement)
                is DetailResult.Cached -> AnnouncementDetailUiState.Ready(result.announcement, isOffline = true)
                DetailResult.NotFound -> AnnouncementDetailUiState.NotFound
                is DetailResult.Failed -> AnnouncementDetailUiState.Failed(result.message)
            }
        }
    }
}
