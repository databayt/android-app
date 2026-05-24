package org.hogwarts.android.feature.subjects.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.feature.subjects.domain.model.MySubjectSummary
import org.hogwarts.android.feature.subjects.domain.usecase.GetMySubjectsUseCase
import javax.inject.Inject

data class MySubjectsUiState(
    val isLoading: Boolean = true,
    val subjects: List<MySubjectSummary> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MySubjectsViewModel @Inject constructor(
    private val getMySubjectsUseCase: GetMySubjectsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MySubjectsUiState())
    val uiState: StateFlow<MySubjectsUiState> = _uiState.asStateFlow()

    init {
        loadMySubjects()
    }

    private fun loadMySubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val subjects = getMySubjectsUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        subjects = subjects,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load my subjects"
                    )
                }
            }
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadMySubjects()
    }
}
