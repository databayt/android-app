package org.hogwarts.android.feature.lessons.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.lessons.domain.model.CurriculumMap
import org.hogwarts.android.feature.lessons.domain.usecase.GetCurriculumMapUseCase
import javax.inject.Inject

data class CurriculumMapUiState(
    val terms: List<CurriculumMap> = emptyList(),
    val selectedTermId: String? = null,
    val selectedTerm: CurriculumMap? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CurriculumMapViewModel @Inject constructor(
    private val getCurriculumMapUseCase: GetCurriculumMapUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurriculumMapUiState())
    val uiState: StateFlow<CurriculumMapUiState> = _uiState.asStateFlow()

    init {
        loadCurriculum()
    }

    private fun loadCurriculum() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getCurriculumMapUseCase()) {
                is Result.Success -> {
                    val terms = result.data
                    val selected = terms.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            terms = terms,
                            selectedTermId = selected?.termId,
                            selectedTerm = selected
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun selectTerm(termId: String) {
        _uiState.update { state ->
            val term = state.terms.find { it.termId == termId }
            state.copy(selectedTermId = termId, selectedTerm = term)
        }
    }

    fun refresh() = loadCurriculum()
}
