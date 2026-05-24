package org.hogwarts.android.feature.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.admin.domain.model.SchoolStats
import org.hogwarts.android.feature.admin.domain.usecase.GetSchoolStatsUseCase
import javax.inject.Inject

data class SchoolStatsUiState(
    val stats: SchoolStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SchoolStatsViewModel @Inject constructor(
    private val getSchoolStatsUseCase: GetSchoolStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolStatsUiState())
    val uiState: StateFlow<SchoolStatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getSchoolStatsUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, stats = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh() = loadStats()
}
