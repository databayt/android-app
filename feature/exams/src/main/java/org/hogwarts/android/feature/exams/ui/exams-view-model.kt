package org.hogwarts.android.feature.exams.ui

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
import org.hogwarts.android.feature.exams.domain.usecase.GetExamsUseCase
import javax.inject.Inject

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val getExamsUseCase: GetExamsUseCase,
    val localeFormatter: LocaleFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamsUiState())
    val uiState: StateFlow<ExamsUiState> = _uiState.asStateFlow()

    init {
        loadExams()
    }

    private fun loadExams() {
        val status = _uiState.value.selectedStatusFilter
        viewModelScope.launch {
            getExamsUseCase(status).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, exams = resource.data ?: it.exams)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, exams = resource.data ?: emptyList(), error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, exams = resource.data ?: it.exams, error = resource.error?.message)
                    }
                }
            }
        }
    }

    fun onStatusFilterChanged(status: String?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadExams()
    }
}
