package org.hogwarts.android.feature.announcements.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import org.hogwarts.android.feature.announcements.domain.usecase.GetAnnouncementDetailUseCase
import javax.inject.Inject

data class AnnouncementDetailUiState(
    val isLoading: Boolean = true,
    val announcement: Announcement? = null,
    val error: String? = null
)

@HiltViewModel
class AnnouncementDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAnnouncementDetailUseCase: GetAnnouncementDetailUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val announcementId: String = savedStateHandle["announcementId"] ?: ""

    private val _uiState = MutableStateFlow(AnnouncementDetailUiState())
    val uiState: StateFlow<AnnouncementDetailUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncementDetail()
    }

    private fun loadAnnouncementDetail() {
        if (announcementId.isBlank()) {
            _uiState.value = AnnouncementDetailUiState(isLoading = false, error = "Invalid announcement ID")
            return
        }

        viewModelScope.launch {
            getAnnouncementDetailUseCase(announcementId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> AnnouncementDetailUiState(
                        isLoading = true,
                        announcement = resource.data
                    )
                    is Resource.Success -> AnnouncementDetailUiState(
                        isLoading = false,
                        announcement = resource.data
                    )
                    is Resource.Error -> AnnouncementDetailUiState(
                        isLoading = false,
                        announcement = resource.data,
                        error = resource.error?.message ?: "Failed to load announcement"
                    )
                }
            }
        }
    }
}
