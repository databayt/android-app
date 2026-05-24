package org.hogwarts.android.feature.subjects.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.subjects.domain.model.SubjectDetail
import org.hogwarts.android.feature.subjects.domain.usecase.GetSubjectDetailUseCase
import javax.inject.Inject

data class SubjectDetailUiState(
    val subjectDetail: SubjectDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSubjectDetailUseCase: GetSubjectDetailUseCase
) : ViewModel() {

    private val subjectId: String = checkNotNull(savedStateHandle["subjectId"])

    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadSubjectDetail()
    }

    private fun loadSubjectDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val detail = getSubjectDetailUseCase(subjectId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        subjectDetail = detail,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load subject"
                    )
                }
            }
        }
    }

    fun onRefresh() {
        loadSubjectDetail()
    }
}
