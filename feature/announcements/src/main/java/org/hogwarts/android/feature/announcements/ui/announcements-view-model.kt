package org.hogwarts.android.feature.announcements.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.utils.LocaleFormatter
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementType
import org.hogwarts.android.feature.announcements.domain.usecase.GetAnnouncementsUseCase
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val getAnnouncementsUseCase: GetAnnouncementsUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        viewModelScope.launch {
            val typeFilter = _uiState.value.selectedFilter?.name
            getAnnouncementsUseCase(type = typeFilter).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, announcements = resource.data ?: it.announcements)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, announcements = resource.data ?: emptyList(), error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, announcements = resource.data ?: it.announcements, error = resource.error?.message)
                    }
                }
            }
        }
    }

    fun onFilterChanged(type: AnnouncementType?) {
        _uiState.update { it.copy(selectedFilter = type) }
        loadAnnouncements()
    }
}
