package org.hogwarts.android.feature.guardian.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.guardian.domain.model.Child
import org.hogwarts.android.feature.guardian.domain.usecase.GetChildrenUseCase
import javax.inject.Inject

data class ChildrenUiState(
    val children: List<Child> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChildrenViewModel @Inject constructor(
    private val getChildrenUseCase: GetChildrenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildrenUiState())
    val uiState: StateFlow<ChildrenUiState> = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getChildrenUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, children = result.data) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message ?: "Failed to load children")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh() {
        loadChildren()
    }
}
